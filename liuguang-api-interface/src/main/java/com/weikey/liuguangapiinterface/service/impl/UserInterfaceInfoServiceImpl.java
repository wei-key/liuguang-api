package com.weikey.liuguangapiinterface.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weikey.liuguangapicommon.constant.CommonConstant;
import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoQueryRequest;
import com.weikey.liuguangapicommon.model.entity.UserInterfaceInfo;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.model.enums.UserInterfaceInfoStatusEnum;
import com.weikey.liuguangapicommon.utils.SqlUtils;
import com.weikey.liuguangapicommon.utils.ThrowUtils;
import com.weikey.liuguangapiinterface.mapper.UserInterfaceInfoMapper;
import com.weikey.liuguangapiinterface.service.UserInterfaceInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author wei-key
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
* @createDate 2023-07-11 10:35:13
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService {

    /**
     * 参数校验
     * 增加（add）时，增加所必须的参数都不能为空，并进行内容的校验；其他情况（不为add），哪些参数不为空，则校验哪些参数
     * => 增加（add）时，有必须传递的参数；其他情况（改、查），传参比较自由，没有必须要传递的
     *
     * @param userInterfaceInfo 用实体对象统一接收，调用此方法，需要先将参数对象dto转化为实体对象
     * @param add 判断是否为增加，根据布尔值参数add判断
     */
    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = userInterfaceInfo.getId();
        Long userId = userInterfaceInfo.getUserId();
        Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
        Integer totalNum = userInterfaceInfo.getTotalNum();
        Integer leftNum = userInterfaceInfo.getLeftNum();
        Integer status = userInterfaceInfo.getStatus();
        Date createTime = userInterfaceInfo.getCreateTime();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(userId == null || interfaceInfoId == null || leftNum == null, ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(leftNum <= 0, ErrorCode.PARAMS_ERROR); // 创建时，剩余调用次数要大于0
        }
        // 有参数则校验
        ThrowUtils.throwIf(id != null && id <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userId != null && userId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(interfaceInfoId != null && interfaceInfoId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(leftNum != null && leftNum < 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(totalNum != null && totalNum < 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(status != null && status != UserInterfaceInfoStatusEnum.FORBIDDEN.getValue()
                && status != UserInterfaceInfoStatusEnum.NORMAL.getValue(), ErrorCode.PARAMS_ERROR, "状态错误");

    }

    /**
     *
     * 获取查询包装类
     *
     * @param userInterfaceInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<UserInterfaceInfo> getQueryWrapper(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        if (userInterfaceInfoQueryRequest == null) {
            return new QueryWrapper<>();
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfo);
        // userInterfaceInfo【非空字段】作为查询条件
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfo);

        String sortField = userInterfaceInfoQueryRequest.getSortField();
        String sortOrder = userInterfaceInfoQueryRequest.getSortOrder();

        // 拼接查询条件
        // mp自动过滤逻辑删除的数据，不需要自己手动加查询条件
        // queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    // todo 是否有其他需要完善？
    // 加了乐观锁；不用事务，只有一次数据库更新操作；失败返回false
    @Override
    public boolean invokeCount(long userId, long interfaceInfoId) {
        ThrowUtils.throwIf(userId <= 0 || interfaceInfoId <= 0, ErrorCode.PARAMS_ERROR);
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        // todo prettyCode 乐观锁
        // 为了避免leftNum“超卖”问题，这里用了乐观锁：update语句加条件 leftNum > 0
        updateWrapper.setSql("totalNum = totalNum + 1, leftNum = leftNum - 1")
                .eq("userId", userId).eq("interfaceInfoId", interfaceInfoId).gt("leftNum", 0);
        return this.update(updateWrapper);
    }

    /**
     * 接口调用次数回滚
     * @param userId
     * @param interfaceInfoId
     * @return
     */
    @Override
    public boolean rollbackCount(long userId, long interfaceInfoId) {
        ThrowUtils.throwIf(userId <= 0 || interfaceInfoId <= 0, ErrorCode.PARAMS_ERROR);
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("totalNum = totalNum - 1, leftNum = leftNum + 1")
                .eq("userId", userId).eq("interfaceInfoId", interfaceInfoId);
        return this.update(updateWrapper);
    }

    /**
     * 分配接口的调用次数
     * todo 并发的考虑：是否存在并发的问题？
     * @param interfaceId 接口id
     * @param amount 接口调用次数
     * @param userId 用户id
     */
    @Override
    public void addCount(long interfaceId, int amount, long userId) {
        // 1. 校验参数
        if (interfaceId <= 0 || amount <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 查询用户是否有接口次数分配的记录
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("interfaceInfoId", interfaceId);
        UserInterfaceInfo userInterfaceInfo = this.getOne(queryWrapper);

        if (userInterfaceInfo == null) {
            // 2.1 没有记录，创建记录，同时设置调用次数
            userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(userId);
            userInterfaceInfo.setInterfaceInfoId(interfaceId);
            userInterfaceInfo.setLeftNum(amount);
            this.save(userInterfaceInfo);
        } else {
            // 2.2 有记录，增加调用次数
            UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("leftNum", userInterfaceInfo.getLeftNum() + amount)
                    .eq("id", userInterfaceInfo.getId());
            this.update(updateWrapper);
        }
    }

}




