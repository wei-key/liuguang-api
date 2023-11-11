package com.weikey.liuguangapicommon.model.dto.pay;

import lombok.Data;

import java.io.Serializable;

/**
 *  下单请求
 *
 * @author wei-key
 */
@Data
public class TradeSubmitRequest implements Serializable {

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 订单金额(单位：分)
     */
    private Integer totalFee;

    /**
     * 接口调用次数
     */
    private Integer amount;

    private static final long serialVersionUID = 1L;
}
