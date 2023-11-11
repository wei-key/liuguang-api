package com.weikey.liuguangapiorder.service;


import com.weikey.liuguangapicommon.model.dto.pay.TradeSubmitRequest;
import com.weikey.liuguangapicommon.model.entity.OrderInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AliPayService {
    /**
     * 下单
     * @param tradeSubmitRequest
     * @return
     */
    String submitTrade(TradeSubmitRequest tradeSubmitRequest, HttpServletRequest request);

    void processOrder(Map<String, String> params);

    String queryOrder(String orderNo);

    void checkOrderStatus(OrderInfo orderInfo);

    void closeOrder(String orderNo);

}
