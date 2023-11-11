package com.weikey.liuguangapicommon.service;


import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.entity.User;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.model.enums.UserRoleEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

import static com.weikey.liuguangapicommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务
 */
@FeignClient(name = "liuguang-api-user", path = "/api/user/inner")
public interface UserFeignClient {

    /**
     * 根据accessKey查询用户
     *
     * @param accessKey
     * @return
     */
    @GetMapping("/get/invoke/user")
    User getInvokeUser(@RequestParam("accessKey") String accessKey);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    default User getLoginUser(HttpServletRequest request) {
        // 判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    default boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

}
