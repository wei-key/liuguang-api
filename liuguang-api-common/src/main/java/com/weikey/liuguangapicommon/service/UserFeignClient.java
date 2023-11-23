package com.weikey.liuguangapicommon.service;


import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.entity.User;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.model.enums.UserRoleEnum;
import com.weikey.liuguangapicommon.utils.JWTUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;


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
     * @param userId
     * @return
     */
    @GetMapping("/get/login/user")
    User getLoginUser(@RequestParam("userId") long userId);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    default boolean isAdmin(HttpServletRequest request) {
        String role = JWTUtils.getRoleFromToken(request);
        return UserRoleEnum.ADMIN.getValue().equals(role);
    }

}
