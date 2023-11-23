package com.weikey.liuguangapiinterface.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.weikey.liuguangapicommon.annotation.AuthCheck;
import com.weikey.liuguangapicommon.constant.UserConstant;
import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.dto.common.DeleteRequest;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoActivateRequest;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoAddRequest;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoQueryRequest;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoUpdateRequest;
import com.weikey.liuguangapicommon.model.entity.UserInterfaceInfo;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.service.UserFeignClient;
import com.weikey.liuguangapicommon.utils.JWTUtils;
import com.weikey.liuguangapicommon.utils.ResultUtils;
import com.weikey.liuguangapicommon.utils.ThrowUtils;
import com.weikey.liuguangapiinterface.service.UserInterfaceInfoService;
import com.weikey.liuguangapisdk.client.ApiClient;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户调用接口关系的接口
 *
 * @author wei-key
 */
@RestController
@RequestMapping("/userInterfaceInfo")
public class UserInterfaceInfoController {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private ApiClient apiClient;

    @Resource
    private UserFeignClient userFeignClient;


    private final static Gson GSON = new Gson();

    /**
     * 开通接口
     * @param userInterfaceInfoActivateRequest
     * @param request
     * @return
     */
    @PostMapping("/activate/interface")
    public BaseResponse<Boolean> activateInterface(@RequestBody UserInterfaceInfoActivateRequest userInterfaceInfoActivateRequest,
                                                   HttpServletRequest request) {
        // 1. 校验参数
        ThrowUtils.throwIf(userInterfaceInfoActivateRequest == null || userInterfaceInfoActivateRequest.getUserId() <= 0
        || userInterfaceInfoActivateRequest.getAmount() <= 0, ErrorCode.PARAMS_ERROR);

        Long interfaceInfoId = userInterfaceInfoActivateRequest.getUserId();
        Integer invokeNum = userInterfaceInfoActivateRequest.getAmount();
        Long userId = JWTUtils.getUidFromToken(request);

        // todo prettyCode 加锁解决并发；摘要算法解决字符串拼接冲突
        Boolean result = null;
        String userIdStr = DigestUtil.md5Hex16(userId.toString());
        String interfaceInfoIdStr = DigestUtil.md5Hex16(interfaceInfoId.toString());
        synchronized ((userIdStr + interfaceInfoIdStr).intern()) {
            // 2. 查询是否有记录
            QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId", userId).eq("interfaceInfoId", interfaceInfoId);
            UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(queryWrapper);
            if (userInterfaceInfo == null) {
                // 3. 没有记录，创建，同时设置调用次数
                userInterfaceInfo = new UserInterfaceInfo();
                userInterfaceInfo.setUserId(userId);
                userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
                userInterfaceInfo.setLeftNum(invokeNum);
                result = userInterfaceInfoService.save(userInterfaceInfo);
            } else {
                // 4. 有记录，增加次数
                Integer leftNum = userInterfaceInfo.getLeftNum();
                UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
                updateWrapper.set("leftNum", leftNum + invokeNum)
                        .eq("id", userInterfaceInfo.getId());
                result = userInterfaceInfoService.update(updateWrapper);
            }
        }
        return ResultUtils.success(result);
    }


    // region 增删改查（仅管理员可操作） todo 这里的增删改查需要完善

    /**
     * 创建
     * @param userInterfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUserInterfaceInfo(@RequestBody UserInterfaceInfoAddRequest userInterfaceInfoAddRequest,
                                                   HttpServletRequest request) {
        if (userInterfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoAddRequest, userInterfaceInfo); // 将参数对象转化为实体对象

        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true); // 参数校验
        // todo userId如何设置？（疑问：由管理员创建？userId不是创建人，是调用接口的用户id）
//        User loginUser = userService.getLoginUser(request);
//        userInterfaceInfo.setUserId(loginUser.getId()); // 创建人为当前用户

        boolean result = userInterfaceInfoService.save(userInterfaceInfo); // 保存
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        long newUserInterfaceInfoId = userInterfaceInfo.getId();
        return ResultUtils.success(newUserInterfaceInfoId);
    }

    /**
     * 删除
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断记录是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldUserInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean b = userInterfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     * @param userInterfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserInterfaceInfo(@RequestBody UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (userInterfaceInfoUpdateRequest == null || userInterfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoUpdateRequest, userInterfaceInfo);
        // 参数校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, false);
        long id = userInterfaceInfoUpdateRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldUserInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = userInterfaceInfoService.updateById(userInterfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserInterfaceInfo> getUserInterfaceInfoById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getById(id);
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(userInterfaceInfo);
    }

    /**
     * 获取列表
     *
     * @param userInterfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/list")
    public BaseResponse<List<UserInterfaceInfo>> listUserInterfaceInfo(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        if (userInterfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoService.list(queryWrapper);
        return ResultUtils.success(userInterfaceInfoList);
    }

    /**
     * 分页获取列表
     *
     * @param userInterfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserInterfaceInfo>> listUserInterfaceInfoByPage(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest,
                                                                     HttpServletRequest request) {
        if (userInterfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userInterfaceInfoQueryRequest.getCurrent();
        long size = userInterfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        // todo 这里的限制爬虫是什么意思
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<UserInterfaceInfo> userInterfaceInfoPage = userInterfaceInfoService.page(new Page<>(current, size),
                userInterfaceInfoService.getQueryWrapper(userInterfaceInfoQueryRequest));
        return ResultUtils.success(userInterfaceInfoPage);
    }

    // endregion




}
