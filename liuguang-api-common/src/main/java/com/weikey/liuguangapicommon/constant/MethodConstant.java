package com.weikey.liuguangapicommon.constant;

/**
 * 请求类型常量
 */
public interface MethodConstant {

    String GET_REQ = "get";

    String POST_REQ = "post";

    String PUT_REQ = "put";

    String DELETE_REQ = "delete";

    /**
     * 检验请求类型是否有效
     * @param method
     * @return
     */
    static boolean validMethod(String method) {
        return GET_REQ.equalsIgnoreCase(method) || POST_REQ.equalsIgnoreCase(method)
                || PUT_REQ.equalsIgnoreCase(method) || DELETE_REQ.equalsIgnoreCase(method);
    }

}
