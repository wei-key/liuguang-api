package com.weikey.liuguangapiorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.dto.pay.OrderInfoPageRequest;
import com.weikey.liuguangapicommon.model.dto.pay.TradeSubmitRequest;
import com.weikey.liuguangapicommon.model.entity.InterfaceInfo;
import com.weikey.liuguangapicommon.model.entity.OrderInfo;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.model.enums.OrderStatus;
import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.service.InterfaceFeignClient;
import com.weikey.liuguangapicommon.service.UserFeignClient;
import com.weikey.liuguangapicommon.utils.OrderNoUtils;
import com.weikey.liuguangapicommon.utils.ResultUtils;
import com.weikey.liuguangapicommon.utils.ThrowUtils;
import com.weikey.liuguangapiorder.mapper.OrderInfoMapper;
import com.weikey.liuguangapiorder.service.OrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 肖
 * @description 针对表【order_info(用户)】的数据库操作Service实现
 * @createDate 2023-11-01 16:08:53
 */
@Service
@Slf4j
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements OrderInfoService {

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private InterfaceFeignClient interfaceFeignClient;

    @Override
    public OrderInfo createOrder(TradeSubmitRequest tradeSubmitRequest, int payType, Long userId) {
        Long interfaceId = tradeSubmitRequest.getInterfaceId();
        Integer totalFee = tradeSubmitRequest.getTotalFee();
        Integer amount = tradeSubmitRequest.getAmount();

        // 获取接口信息
        InterfaceInfo interfaceInfo = interfaceFeignClient.getById(interfaceId);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口不存在");
        }

        String title = interfaceInfo.getName() + "-" + interfaceInfo.getDescription();

        // 创建订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTitle(title);
        orderInfo.setOrderNo(OrderNoUtils.getOrderNo());
        orderInfo.setUserId(userId);
        orderInfo.setInterfaceId(interfaceId);
        orderInfo.setTotalFee(totalFee);
        orderInfo.setAmount(amount);
        orderInfo.setOrderStatus(0);
        orderInfo.setPaymentType(0);

        orderInfoMapper.insert(orderInfo);

        return orderInfo;
    }

    /**
     * 分页查询订单列表，按照订单创建时间倒序排列
     * @param orderInfoPageRequest
     * @param userId
     * @return
     */
    @Override
    public BaseResponse<Page<OrderInfo>> listOrderPageByCreateTimeDesc(OrderInfoPageRequest orderInfoPageRequest, long userId) {
        if (orderInfoPageRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = orderInfoPageRequest.getCurrent();
        long pageSize = orderInfoPageRequest.getPageSize();

        // 限制爬虫: 如果一次性获取太多条数据，视为爬虫，报错
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<OrderInfo>();
        queryWrapper.orderByDesc("createTime") // 按照订单创建时间倒序排列
                .eq("userId", userId);
        // 分页查询接口信息
        Page<OrderInfo> page = this.page(new Page<>(current, pageSize), queryWrapper);

        return ResultUtils.success(page);
    }

    /**
     * 根据订单号更新订单状态
     *
     * @param orderNo
     * @param orderStatus
     * @return 更新是否成功
     */
    @Override
    public void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus) {

        UpdateWrapper<OrderInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("orderNo", orderNo);
        updateWrapper.set("orderStatus", orderStatus.getValue());

        this.update(updateWrapper);
    }

    /**
     * 根据订单号获取订单状态
     *
     * @param orderNo
     * @return
     */
    @Override
    public int getOrderStatus(String orderNo) {

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orderNo", orderNo);
        OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        if (orderInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        return orderInfo.getOrderStatus();
    }

    /**
     * 根据订单号获取订单
     *
     * @param orderNo
     * @return
     */
    @Override
    public OrderInfo getOrderByOrderNo(String orderNo) {

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orderNo", orderNo);
        OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);

        return orderInfo;
    }

}




