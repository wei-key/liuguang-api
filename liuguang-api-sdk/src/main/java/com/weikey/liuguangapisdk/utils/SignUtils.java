package com.weikey.liuguangapisdk.utils;

import cn.hutool.crypto.digest.DigestUtil;

public class SignUtils {

    /**
     * 生成签名
     *
     * @param param 用于生成签名的参数
     * @param secretKey 密钥
     * @return
     */
    public static String getSign(String param, String secretKey) {
        String data = param + "-" + secretKey;
        return DigestUtil.md5Hex(data); // todo 优化
    }
}
