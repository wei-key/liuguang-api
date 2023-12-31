package com.weikey.liuguangapicommon.model.dto.interfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author wei-key
 */
@Data
public class InterfaceInfoGetRequest implements Serializable {

    /**
     * 请求路径
     */
    private String url;

    private static final long serialVersionUID = 1L;
}