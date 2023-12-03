package com.weikey.liuguangapiinterface.controller.inner;

import cn.hutool.core.util.ObjectUtil;
import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.dto.cache.InterfaceCacheDto;
import com.weikey.liuguangapicommon.model.dto.interfaceInfo.InterfaceInfoGetRequest;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoActivateRequest;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoInvokeCountRequest;
import com.weikey.liuguangapicommon.model.entity.InterfaceInfo;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.service.InterfaceFeignClient;
import com.weikey.liuguangapiinterface.service.InterfaceInfoService;
import com.weikey.liuguangapiinterface.service.UserInterfaceInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 该服务仅内部调用，不是给前端的
 */
@RestController
@RequestMapping("/inner")
public class InterfaceInnerController implements InterfaceFeignClient {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    /**
     * 分配接口的调用次数
     *
     * @param userInterfaceInfoActivateRequest
     */
    @PostMapping("/add/count")
    @Override
    public void addCount(@RequestBody UserInterfaceInfoActivateRequest userInterfaceInfoActivateRequest) {
        Long interfaceId = userInterfaceInfoActivateRequest.getInterfaceId();
        Integer amount = userInterfaceInfoActivateRequest.getAmount();
        Long userId = userInterfaceInfoActivateRequest.getUserId();

        // 校验参数
        if (ObjectUtil.hasNull(interfaceId, amount, userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        userInterfaceInfoService.addCount(interfaceId, amount, userId);
    }

    /**
     * 根据id查询
     *
     * @param interfaceId
     * @return
     */
    @GetMapping("/get/by/id")
    @Override
    public InterfaceInfo getById(@RequestParam("interfaceId") long interfaceId) {
        // 校验参数
        if (interfaceId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return interfaceInfoService.getById(interfaceId);
    }

    /**
     * 根据请求路径和方式查询
     *
     * @param interfaceInfoGetRequest
     * @return
     */
    @PostMapping("/get/post")
    @Override
    public InterfaceCacheDto getInterface(@RequestBody InterfaceInfoGetRequest interfaceInfoGetRequest) {
        String url = interfaceInfoGetRequest.getUrl();
        // 校验参数
        if (ObjectUtil.hasEmpty(url)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return interfaceInfoService.getInterface(url);
    }

    /**
     * 调用接口次数统计
     *
     * @param userInterfaceInfoInvokeCountRequest
     * @return false表示接口剩余调用次数为0，接口调用失败
     */
    @PostMapping("/invoke/count")
    @Override
    public boolean invokeCount(@RequestBody UserInterfaceInfoInvokeCountRequest userInterfaceInfoInvokeCountRequest) {
        Long interfaceInfoId = userInterfaceInfoInvokeCountRequest.getInterfaceInfoId();
        Long userId = userInterfaceInfoInvokeCountRequest.getUserId();
        // 校验参数
        if (ObjectUtil.hasNull(interfaceInfoId, userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userInterfaceInfoService.invokeCount(userId, interfaceInfoId);
    }
}
