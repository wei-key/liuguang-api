package com.weikey.liuguangapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口信息
 * @TableName interface_info
 */
@TableName(value ="interface_info")
@Data
public class InterfaceInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
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
     * 接口地址
     */
    private String url;

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
     * json对象数组，一个参数对应一个json对象
     *
     * 示例：
     * [
     *    {
     * 		"name":"sort",
     * 		"isRequired":"yes",
     * 		"type":"string",
     * 		"remark":"选择输出分类[男|女|动漫男|动漫女]，为空随机输出"
     *    },
     *    {
     * 		"name":"format",
     * 		"isRequired":"no",
     * 		"type":"string",
     * 		"remark":"选择输出格式[json|images]"
     *    }
     * ]
     */
    private String requestParamsRemark;

    /**
     * 响应参数说明
     * json对象数组，一个参数对应一个json对象
     *
     * 示例：
     * [
     *    {
     * 		"name":"code",
     * 		"type":"string",
     * 		"remark":"返回的状态码"
     *    },
     *    {
     * 		"name":"imgurl",
     * 		"type":"string",
     * 		"remark":"返回图片地址"
     *    },
     *    {
     * 		"name":"msg",
     * 		"type":"string",
     * 		"remark":"返回错误提示信息！"
     *    }
     * ]
     */
    private String responseParamsRemark;

    /**
     * 接口调用示例代码
     */
    private String code;

    /**
     * 单次调用的价格（单位：分）
     */
    private Integer price = 0;

    /**
     * 是否删除(0-未删, 1-已删)
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}