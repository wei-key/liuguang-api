package com.weikey.liuguangapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 * @TableName order_info
 */
@TableName(value ="order_info")
@Data
public class OrderInfo implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单标题：接口名称
     */
    private String title;

    /**
     * 商户订单编号
     */
    private String orderNo;

    /**
     * 用户id
     */
    private Long userId;

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

    /**
     * 订单状态（0：未支付，1：支付成功，2：超时已关闭，3：用户已取消）
     */
    private Integer orderStatus = 0;

    /**
     * 支付方式（0：支付宝）
     */
    private Integer paymentType = 0;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}