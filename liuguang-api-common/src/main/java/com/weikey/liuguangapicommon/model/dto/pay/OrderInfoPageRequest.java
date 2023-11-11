package com.weikey.liuguangapicommon.model.dto.pay;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单分页请求
 *
 * @author wei-key
 */
@Data
public class OrderInfoPageRequest implements Serializable {

    /**
     * 当前页号
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    private static final long serialVersionUID = 1L;
}
