/*
 * @author yjiewei
 * @date 2021/8/17 21:28
 */
package com.weikey.liuguangapicommon.utils;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.dto.common.JwtUserDto;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Map;

public class JWTUtils {

    /**
     * 密钥：使用的签名算法是HmacSHA256，密钥长度建议是256bit（32byte），刚好是32个英文或数字字符
     */
    private static final String SECRET = ""; // todo 开源之前记得脱敏

    /**
     * token过期时间，单位：天
     */
    private static final int EXPIRE = 3;

    public static final String AUTHORIZATION = "Authorization";

    /**
     * 生成token
     * @param jwtUserDto
     * @return
     */
    public static String generateToken(JwtUserDto jwtUserDto) {
        ThrowUtils.throwIf(jwtUserDto == null, ErrorCode.PARAMS_ERROR);

        JWTCreator.Builder builder = JWT.create();
        // payload
        builder.withClaim("uid", jwtUserDto.getUid()); // 用户id
        builder.withClaim("role", jwtUserDto.getRole()); // 用户角色

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, EXPIRE);
        builder.withExpiresAt(instance.getTime()); // 指定令牌的过期时间，默认3天

        return builder.sign(Algorithm.HMAC256(SECRET)); // 指定签名算法和密钥
    }

    /**
     * 验证token
     * @param header
     * @return 成功返回true，失败false
     */
    public static boolean verify(String header) {
        String token = getTokenFromRequest(header);
        if(token == null) {
            return false;
        }
        try {
            // 如果有任何验证异常，此处都会抛出异常
            JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取token中的用户id
     * @param request
     * @return
     */
    public static Long getUidFromToken(HttpServletRequest request) {
        Map<String, Claim> claims = getClaimsFromToken(request);
        return claims.get("uid").asLong();
    }

    /**
     * 获取token中的用户角色
     * @param request
     * @return
     */
    public static String getRoleFromToken(HttpServletRequest request) {
        Map<String, Claim> claims = getClaimsFromToken(request);
        return claims.get("role").asString();
    }

    /**
     * 获取claims
     * @param request
     * @return
     */
    private static Map<String, Claim> getClaimsFromToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request.getHeader(AUTHORIZATION));
        DecodedJWT decodedJWT = null;
        try {
            // 如果有任何验证异常，此处都会抛出异常
            decodedJWT = JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户校验失败");
        }
        return decodedJWT.getClaims();
    }

    /**
     * 从请求头Authorization中拿到token
     * @param header
     * @return
     */
    private static String getTokenFromRequest(String header) {
        if (StrUtil.isEmpty(header)) {
            return null;
        }
        // {'Authorization': 'Bearer<空格>' + token}
        String token = header.replace("Bearer ", "");
        return token;
    }
}
