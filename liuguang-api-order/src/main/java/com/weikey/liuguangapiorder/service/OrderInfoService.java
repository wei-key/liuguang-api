package com.weikey.liuguangapiorder.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.weikey.liuguangapicommon.model.dto.pay.OrderInfoPageRequest;
import com.weikey.liuguangapicommon.model.dto.pay.TradeSubmitRequest;
import com.weikey.liuguangapicommon.model.entity.OrderInfo;
import com.weikey.liuguangapicommon.model.enums.OrderStatus;
import com.weikey.liuguangapicommon.model.response.BaseResponse;

/**
* @author wei-key
* @description 针对表【order_info(用户)】的数据库操作Service
* @createDate 2023-11-01 16:08:53
*/
public interface OrderInfoService extends IService<OrderInfo> {

    OrderInfo createOrder(TradeSubmitRequest tradeSubmitRequest, int payType, Long userId);

    BaseResponse<Page<OrderInfo>> listOrderPageByCreateTimeDesc(OrderInfoPageRequest orderInfoPageRequest, long userId);

    void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus);

    int getOrderStatus(String orderNo);

    OrderInfo getOrderByOrderNo(String orderNo);
}
