package com.weikey.liuguangapiorder.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.weikey.liuguangapicommon.model.entity.PaymentInfo;

import java.util.Map;

/**
* @author 肖
* @description 针对表【payment_info(支付日志)】的数据库操作Service
* @createDate 2023-11-01 16:10:43
*/
public interface PaymentInfoService extends IService<PaymentInfo> {

    void createPaymentInfo(Map<String, String> params);
}
