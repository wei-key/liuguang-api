package com.weikey.liuguangapiorder.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.weikey.liuguangapicommon.model.dto.pay.TradeSubmitRequest;
import com.weikey.liuguangapicommon.model.entity.OrderInfo;
import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.utils.ResultUtils;
import com.weikey.liuguangapiorder.service.AliPayService;
import com.weikey.liuguangapiorder.service.OrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付
 *
 * @author wei-key
 */
@CrossOrigin
@RestController
@RequestMapping("/pay/alipay")
@Slf4j
public class AliPayController {

    @Resource
    private AliPayService aliPayService;

    @Resource
    private Environment config;

    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 下单接口
     *
     * @return
     */
    @PostMapping("/trade/submit")
    public BaseResponse<String> submitTrade(@RequestBody TradeSubmitRequest tradeSubmitRequest, HttpServletRequest request) {

        String formStr = aliPayService.submitTrade(tradeSubmitRequest, request);

        // 调用支付宝接口后返回一段 form表单代码，将这段代码返回给前端，前端调用代码进行表单提交，即可跳转到支付页面
        return ResultUtils.success(formStr);
    }

    /**
     * 支付宝异步通知接口
     *
     * @param params 请求参数
     * @return
     */
    // todo prettyCode @RequestParam Map<String, String> params 会将请求参数封装为map
    @PostMapping("/trade/notify")
    public String tradeNotify(@RequestParam Map<String, String> params) {

        log.info("支付宝异步通知接口回调...");

        String result = "failure";

        // 异步通知验签
        boolean signVerified = false;
        try {
            // 调用SDK验证签名
            signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    config.getProperty("alipay.alipay-public-key"),
                    AlipayConstants.CHARSET_UTF8,
                    AlipayConstants.SIGN_TYPE_RSA2);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        // 验签失败则记录异常日志，并在response中返回failure.
        if (!signVerified) {
            log.error("支付成功异步通知验签失败！");
            return result;
        }

        // 按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，
        // 1）商户需要验证该通知数据中的 out_trade_no 是否为商户系统中创建的订单号
        String outTradeNo = params.get("out_trade_no");
        OrderInfo order = orderInfoService.getOrderByOrderNo(outTradeNo);
        if (order == null) {
            log.error("订单不存在");
            return result;
        }

        // 2）判断 total_amount 是否确实为该订单的实际金额（即商户订单创建时的金额）
        String totalAmount = params.get("total_amount");
        int totalAmountInt = new BigDecimal(totalAmount).multiply(new BigDecimal("100")).intValue();
        int totalFeeInt = order.getTotalFee();
        if (totalAmountInt != totalFeeInt) {
            log.error("金额校验失败");
            return result;
        }

        // 3）校验通知中的 seller_id（或者 seller_email) 是否为 out_trade_no 这笔单据的对应的操作方
        String sellerId = params.get("seller_id");
        String sellerIdProperty = config.getProperty("alipay.seller-id");
        if (!sellerId.equals(sellerIdProperty)) {
            log.error("商家pid校验失败");
            return result;
        }

        // 4）验证 app_id 是否为该商户本身
        String appId = params.get("app_id");
        String appIdProperty = config.getProperty("alipay.app-id");
        if (!appId.equals(appIdProperty)) {
            log.error("appid校验失败");
            return result;
        }

        // 在支付宝的业务通知中，只有交易通知状态为 TRADE_SUCCESS时，支付宝才会认定为买家付款成功。
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            log.error("支付未成功");
            return result;
        }

        // 处理业务：修改订单状态，记录支付日志，分配接口的调用次数
        aliPayService.processOrder(params);

        // 校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
        result = "success";
        return result;
    }
}
