package com.weikey.liuguangapicommon.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PayType {
    /**
     * 支付宝
     */
    ALIPAY("支付宝", 0);

    private final String text;

    private final int value;
}
