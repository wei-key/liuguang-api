package com.weikey.liuguangapiuser.controller.inner;

import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.dto.cache.UserCacheDto;
import com.weikey.liuguangapicommon.model.entity.User;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.service.UserFeignClient;
import com.weikey.liuguangapiuser.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 该服务仅内部调用，不是给前端的
 */
@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {

    @Resource
    private UserService userService;

    /**
     * 根据accessKey查询用户
     *
     * @param accessKey
     * @return
     */
    @Override
    @GetMapping("/get/invoke/user")
    public UserCacheDto getInvokeUser(@RequestParam("accessKey") String accessKey) {
        return userService.getInvokeUser(accessKey);
    }

    /**
     * 获取当前登录用户
     *
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/get/login/user")
    public User getLoginUser(@RequestParam("userId") long userId) {
        // 从数据库查询
        User currentUser = userService.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
}
