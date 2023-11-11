package com.weikey.liuguangapicommon.model.dto.userInterfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 * @author wei-key
 */
@Data
public class UserInterfaceInfoAddRequest implements Serializable {


    /**
     * 调用用户 id
     */
    private Long userId;

    /**
     * 接口 id
     */
    private Long interfaceInfoId;

    /**
     * 剩余调用次数
     */
    private Integer leftNum;

    private static final long serialVersionUID = 1L;
}