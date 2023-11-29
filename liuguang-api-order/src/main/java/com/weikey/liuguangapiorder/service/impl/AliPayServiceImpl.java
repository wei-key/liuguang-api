package com.weikey.liuguangapiorder.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.dto.pay.TradeSubmitRequest;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoActivateRequest;
import com.weikey.liuguangapicommon.model.entity.OrderInfo;
import com.weikey.liuguangapicommon.model.enums.AliPayTradeStatus;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.model.enums.OrderStatus;
import com.weikey.liuguangapicommon.model.enums.PayType;
import com.weikey.liuguangapicommon.service.InterfaceFeignClient;
import com.weikey.liuguangapicommon.service.UserFeignClient;
import com.weikey.liuguangapicommon.utils.JWTUtils;
import com.weikey.liuguangapiorder.mq.MessageService;
import com.weikey.liuguangapiorder.service.AliPayService;
import com.weikey.liuguangapiorder.service.OrderInfoService;
import com.weikey.liuguangapiorder.service.PaymentInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AliPayServiceImpl implements AliPayService {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private Environment config;

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private MessageService messageService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private InterfaceFeignClient interfaceFeignClient;

    /**
     * 下单
     * @param tradeSubmitRequest
     * @param request
     * @return
     */
    // todo prettyCode 事务
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String submitTrade(TradeSubmitRequest tradeSubmitRequest, HttpServletRequest request) {

        try {
            // 参数校验
            if (tradeSubmitRequest == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }

            Long interfaceId = tradeSubmitRequest.getInterfaceId();
            Integer totalFee = tradeSubmitRequest.getTotalFee();
            Integer amount = tradeSubmitRequest.getAmount();

            if (interfaceId == null || interfaceId <= 0
                    || totalFee == null || totalFee < 0
                    || amount == null || amount <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }

            // 获取当前登录用户
            Long userId = JWTUtils.getUidFromToken(request);

            // 1.创建订单
            OrderInfo orderInfo = orderInfoService.createOrder(tradeSubmitRequest, PayType.ALIPAY.getValue(), userId);

            // 2.发送订单消息
            messageService.sendMessage(orderInfo);

            // 3.调用支付宝接口

            AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();

            // 配置需要的公共请求参数
            // 支付完成后，支付宝异步通知回调的接口地址
            alipayTradePagePayRequest.setNotifyUrl(config.getProperty("alipay.notify-url"));
            // 支付完成后，页面同步跳转的地址
            alipayTradePagePayRequest.setReturnUrl(config.getProperty("alipay.return-url"));

            // 组装当前业务方法的请求参数
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderInfo.getOrderNo()); // 商户订单号
            // todo prettyCode 为避免浮点数精度丢失，金额的计算使用BigDecimal
            BigDecimal total = new BigDecimal(orderInfo.getTotalFee().toString()).divide(new BigDecimal("100")); // orderInfo的totalFee 订单金额，单位是分，所以要除以100
            bizContent.put("total_amount", total); // 订单总金额，单位为元
            bizContent.put("subject", orderInfo.getTitle()); // 订单标题
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY"); // 销售产品码。目前电脑支付场景下仅支持FAST_INSTANT_TRADE_PAY

            alipayTradePagePayRequest.setBizContent(bizContent.toString());

            //执行请求，调用支付宝接口
            AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayTradePagePayRequest);

            if (response.isSuccess()) {
                log.info("调用支付宝下单接口成功");
                return response.getBody();
            } else {
                log.info("调用支付宝下单接口失败，返回码 ===> " + response.getCode() + ", 返回描述 ===> " + response.getMsg());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "调用支付宝下单接口失败");
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下单失败");
        }
    }

    /**
     * 处理订单
     *
     * @param params
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processOrder(Map<String, String> params) {

        // 获取订单号
        String orderNo = params.get("out_trade_no");

        // todo 分布式
        // todo prettyCode 使用悲观锁解决一次支付多个通知的并发问题
        synchronized (orderNo.intern()) {
            // todo prettyCode 接口调用的幂等性：无论接口被调用多少次，以下业务执行一次
            // 可能因为网络等原因，支付宝没有接收到商户发送的 success，这样支付宝就会重复发送通知
            // 因此，通过判断订单状态是否为未支付，来过滤掉重复通知
            OrderInfo orderInfo = orderInfoService.getOrderByOrderNo(orderNo);
            // 只有在未支付的情况下才继续执行后续业务，否则直接返回
            if (OrderStatus.NOTPAY.getValue() != orderInfo.getOrderStatus()) {
                return;
            }

            // 1.更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);

            // 2.记录支付日志
            paymentInfoService.createPaymentInfo(params);

            // 3.分配接口的调用次数
            interfaceFeignClient.addCount(new UserInterfaceInfoActivateRequest(orderInfo.getInterfaceId(), orderInfo.getAmount(), orderInfo.getUserId()));
        }

    }

    /**
     * 查询订单
     *
     * @param orderNo
     * @return 返回订单查询结果，如果返回null则表示支付宝端尚未创建订单
     */
    @Override
    public String queryOrder(String orderNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderNo);
            request.setBizContent(bizContent.toString());

            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                return response.getBody();
            } else {
                log.error("查单接口调用失败，返回码 ===> " + response.getCode() + ", 返回描述 ===> " + response.getMsg()
                        + ", 业务码: ===> " + response.getSubCode()  + ", 业务描述: ===> " + response.getSubMsg());
                return null; // 调用失败，说明查询的订单不存在，直接返回null
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查单接口的调用失败");
        }
    }

    /**
     * 根据订单号调用支付宝查单接口，核实订单状态
     * 如果订单未创建，则更新商户端订单状态
     * 如果订单未支付，则调用关单接口关闭订单，并更新商户端订单状态
     * 如果订单已支付，则更新商户端订单状态，并记录支付日志
     *
     * @param orderInfo
     */
    @Override
    public void checkOrderStatus(OrderInfo orderInfo) {
        String orderNo = orderInfo.getOrderNo();

        // 调用支付宝查单接口
        String result = this.queryOrder(orderNo);

        // 1.订单未创建
        if (result == null) {
            // 更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
            return;
        }

        // 解析查单响应结果
        Gson gson = new Gson();
        HashMap<String, LinkedTreeMap> resultMap = gson.fromJson(result, HashMap.class);
        LinkedTreeMap alipayTradeQueryResponse = resultMap.get("alipay_trade_query_response");

        // 支付宝端订单状态
        String tradeStatus = (String) alipayTradeQueryResponse.get("trade_status");

        // 2.订单未支付
        if (AliPayTradeStatus.NOTPAY.getType().equals(tradeStatus)) {
            // 调用关单接口关闭订单
            this.closeOrder(orderNo);
            // 更新商户端订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
        }

        // 3.订单已支付
        if (AliPayTradeStatus.SUCCESS.getType().equals(tradeStatus)) {
            // 更新商户端订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
            // 记录支付日志
            paymentInfoService.createPaymentInfo(alipayTradeQueryResponse);
            // 分配接口的调用次数
            interfaceFeignClient.addCount(new UserInterfaceInfoActivateRequest(orderInfo.getInterfaceId(), orderInfo.getAmount(), orderInfo.getUserId()));
        }

    }

    /**
     * 关单接口的调用
     *
     * @param orderNo 订单号
     */
    public void closeOrder(String orderNo) {
        try {
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderNo);
            request.setBizContent(bizContent.toString());
            AlipayTradeCloseResponse response = alipayClient.execute(request);

            if (!response.isSuccess()) {
                log.error("关单接口调用失败，返回码 ===> " + response.getCode() + ", 返回描述 ===> " + response.getMsg());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new RuntimeException("关单接口的调用失败");
        }
    }


}
