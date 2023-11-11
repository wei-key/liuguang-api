package com.weikey.liuguangapicommon.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 接口信息及调用情况视图
 */
@Data
public class InterfaceInfoInvokeVO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 请求参数示例，json格式
     *
     * 示例：
     * {
     *     "username":"Tom",
     *     "age":20
     * }
     */
    private String requestParams;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 接口状态（0-关闭，1-开启）
     */
    private Integer status;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 请求参数说明
     */
    private List<RequestParamsRemarkVO> requestParamsRemark;

    /**
     * 响应参数说明
     */
    private List<ResponseParamsRemarkVO> responseParamsRemark;

    /**
     * 接口调用示例代码
     */
    private String code;

    /**
     * 调用次数
     */
    private Integer totalNum;

    /**
     * 剩余调用次数
     */
    private Integer leftNum;

    /**
     * 单次调用的价格（单位：分）
     */
    private Integer price = 0;

    /**
     * 是否被当前用户所拥有，即用户是否已经开通接口
     */
    private Boolean isOwnedByCurrentUser;

    private static final long serialVersionUID = 1L;
}
