package com.weikey.liuguangapiinterface.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoQueryRequest;
import com.weikey.liuguangapicommon.model.entity.UserInterfaceInfo;

/**
* @author wei-key
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2023-07-11 10:35:13
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean b);

    QueryWrapper<UserInterfaceInfo> getQueryWrapper(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest);

    /**
     * 调用接口次数统计
     * @param userId
     * @param interfaceInfoId
     * @return false表示接口剩余调用次数为0，接口调用失败
     */
    boolean invokeCount(long userId, long interfaceInfoId);

    boolean rollbackCount(long userId, long interfaceInfoId);

    void addCount(long interfaceId, int amount, long userId);
}
