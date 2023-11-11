package com.weikey.liuguangapicommon.model.dto.interfaceInfo;

import com.weikey.liuguangapicommon.model.vo.RequestParamsRemarkVO;
import com.weikey.liuguangapicommon.model.vo.ResponseParamsRemarkVO;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *
 * @author wei-key
 */
@Data
public class InterfaceInfoAddRequest implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 请求参数示例
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
     * 请求类型
     */
    private String method;

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
     * 单次调用的价格（单位：分）
     */
    private Integer price = 0;

    private static final long serialVersionUID = 1L;
}