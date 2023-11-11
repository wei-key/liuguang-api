package com.weikey.liuguangapicommon.model.dto.userInterfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 开通接口请求
 *
 * @author wei-key
 */
@Data
public class UserInterfaceInfoInvokeCountRequest implements Serializable {

    /**
     * 接口id
     */
    private Long interfaceInfoId;

    /**
     * 用户id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
