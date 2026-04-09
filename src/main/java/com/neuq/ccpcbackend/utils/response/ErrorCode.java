package com.neuq.ccpcbackend.utils.response;

import lombok.Getter;

@Getter
public enum ErrorCode {
    TOKEN_PARSE_ERROR("400", "JWT解析错误"),
    TOKEN_EXPIRE("401", "JWT过期"),
    ACCESS_DENIED("402", "无权限"),
    NO_SIGN_IN("403", "未登录"),

    NOT_FOUND("404", "NOT FOUND"),

    ILLEGAL_ARGUMENT("405", "参数异常"),

    DATA_EXIST("406", "数据已存在"),
    DATA_NOT_EXIST("407", "数据不存在"),

    VERIFY_ERROR("408", "验证错误"),
    VERIFY_EXPIRE("409", "验证过期"),

    REQUEST_FREQUENTLY("410", "请求过于频繁"),

    UPDATE_FAILED("411", "更新失败"),
    QUERY_FAILED("412", "查询失败"),
    DELETE_FAILED("413", "删除失败"),
    INSERT_FAILED("414", "添加失败"),

    ACCEPT_INVITE_FAILED("415", "接受邀请失败"),
    GENERATE_INVITE_FAILED("416", "生成邀请失败"),

    SEND_SMS_FAILED("417", "短信发送失败"),

    DOWNLOAD_FAILED("418", "下载失败"),

    UNKNOWN_ERROR("500", "未知错误"),
    NULL_POINT_EXCEPTION("501", "访问null值");

    private final String errCode;
    private final String errDesc;

    private ErrorCode(String errCode, String errDesc) {
        this.errCode = errCode;
        this.errDesc = errDesc;
    }
}
