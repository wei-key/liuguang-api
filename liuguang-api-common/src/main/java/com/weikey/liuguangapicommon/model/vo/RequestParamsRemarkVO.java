package com.weikey.liuguangapicommon.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wei-key
 */
@Data
public class RequestParamsRemarkVO implements Serializable {

    private static final long serialVersionUID = -6549477882078242340L;

    /**
     * 名称
     */
    private String name;

    /**
     * 是否必须
     */
    private String isRequired;

    /**
     * 类型
     */
    private String type;

    /**
     * 说明
     */
    private String remark;
}