package com.weikey.liuguangapisdk.exception;

public enum ApiError {

    IP_IN_BLACKLIST("IpInBlacklist", "The accessing ip is in the blacklist."),
    IP_NOT_IN_WHITELIST("IpNotInWhitelist", "The accessing ip is not in the whitelist."),
    BLANK_HEADER("BlankHeader", "Any of the headers for signature authentication is blank."),
    INVALID_ACCESSKEY("InvalidAccessKey", "The accessKey is invalid."),
    SIGNATURE_FAILURE("SignatureFailure", "The signature you provided is inconsistent with the one background generated."),
    SIGNATURE_EXPIRE("SignatureExpire", "The timestamp is more than 5 minutes from the server time."),
    INTERFACE_NOT_FOUNT("InterfaceNotFound", "The interface you query is not found."),
    COUNT_NOT_ENOUGH("CountNotEnough", "The invoke count is not enough."),
    INTERNAL_ERROR("InternalError", "A error occurred inside the system."),
    INVOKE_FAILURE("InvokeFailure", "Invoking the interface failed."),
    REQUEST_REPLAY("RequestReplay", "The request is replayed from an old one."),
    FLOW_CONTROL("FlowControl", "The request is limited by flow control."),
    INTERFACE_CLOSE("InterfaceClose", "The interface has been closed.");


    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误的具体信息
     */
    private final String message;

    ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
