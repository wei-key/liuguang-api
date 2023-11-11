package com.weikey.liuguangapicommon.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderStatus {
    /**
     * 未支付
     */
    NOTPAY("未支付", 0),


    /**
     * 支付成功
     */
    SUCCESS("支付成功", 1),

    /**
     * 已关闭
     */
    CLOSED("超时已关闭", 2),

    /**
     * 已取消
     */
    CANCEL("用户已取消", 3);

    private final String text;

    private final int value;
}
