package com.weikey.liuguangapicommon.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口总调用次数视图
 */
@Data
public class InterfaceInfoCountVO implements Serializable {

    /**
     * 接口id
     */
    private Long id;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 总调用次数
     */
    private Integer totalCount;

    private static final long serialVersionUID = 1L;
}
