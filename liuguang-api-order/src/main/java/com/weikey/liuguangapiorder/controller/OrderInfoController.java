package com.weikey.liuguangapiorder.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.dto.pay.OrderInfoPageRequest;
import com.weikey.liuguangapicommon.model.entity.OrderInfo;
import com.weikey.liuguangapicommon.model.entity.User;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.model.enums.OrderStatus;
import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.service.UserFeignClient;
import com.weikey.liuguangapicommon.utils.ResultUtils;
import com.weikey.liuguangapiorder.service.AliPayService;
import com.weikey.liuguangapiorder.service.OrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 订单接口
 *
 * @author wei-key
 */
@RestController
@RequestMapping("/orderInfo")
@Slf4j
public class OrderInfoController {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private AliPayService aliPayService;

    @Resource
    private UserFeignClient userFeignClient;

    /**
     * 获取当前用户的订单列表
     *
     * @param orderInfoPageRequest
     * @param request
     * @return
     */
    @PostMapping("/page")
    public BaseResponse<Page<OrderInfo>> listOrderPageByCreateTimeDesc(@RequestBody OrderInfoPageRequest orderInfoPageRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userFeignClient.getLoginUser(request);

        return orderInfoService.listOrderPageByCreateTimeDesc(orderInfoPageRequest, loginUser.getId());
    }

    /**
     * 用户取消订单
     *
     * @param orderNo
     */
    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelOrder(String orderNo) {

        // 1.查询订单状态，只有"未支付"才可继续执行
        if (OrderStatus.NOTPAY.getValue() != orderInfoService.getOrderStatus(orderNo)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 2.调用支付宝提供的统一收单交易关闭接口
        aliPayService.closeOrder(orderNo);

        // 3.更新用户订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);

        return ResultUtils.success(true);
    }

}
