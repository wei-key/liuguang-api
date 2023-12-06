package com.weikey.liuguangapiorder.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.weikey.liuguangapicommon.model.entity.PaymentInfo;
import com.weikey.liuguangapicommon.model.enums.PayType;
import com.weikey.liuguangapiorder.mapper.PaymentInfoMapper;
import com.weikey.liuguangapiorder.service.PaymentInfoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
* @author wei-key
* @description 针对表【payment_info(支付日志)】的数据库操作Service实现
* @createDate 2023-11-01 16:10:43
*/
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo>
    implements PaymentInfoService {

    /**
     * 记录支付日志
     * @param params
     */
    @Override
    public void createPaymentInfo(Map<String, String> params) {

        // 获取订单号
        String orderNo = params.get("out_trade_no");
        // 支付宝交易号
        String transactionId = params.get("trade_no");
        // 交易金额
        String totalAmount = params.get("total_amount");
        int totalAmountInt = new BigDecimal(totalAmount).multiply(new BigDecimal("100")).intValue();


        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        paymentInfo.setPaymentType(PayType.ALIPAY.getValue());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setPayFee(totalAmountInt);

        // 序列化
        Gson gson = new Gson();
        String json = gson.toJson(params, HashMap.class);
        paymentInfo.setContent(json);

        // 执行插入操作
        baseMapper.insert(paymentInfo);
    }

}




