package com.neuq.ccpcbackend.config;

import com.neuq.ccpcbackend.utils.exception.BizException;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import com.neuq.ccpcbackend.utils.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 处理权限不足异常（@PreAuthorize 等触发）
    @ExceptionHandler(AccessDeniedException.class)
    public Response handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied exception occurred: ", e);
        return Response.error(ErrorCode.ACCESS_DENIED);
    }

    // 处理未认证异常（Token 无效、未登录等）
    @ExceptionHandler(AuthenticationException.class)
    public Response handleAuthenticationException(AuthenticationException e) {
        log.error("Authentication exception occurred: ", e);
        return Response.error(ErrorCode.NO_SIGN_IN);
    }

    // 访问空对象
    @ExceptionHandler(NullPointerException.class)
    public Response handleNullPointerException(NullPointerException e) {
        log.error("Null pointer exception occurred: ", e);
        return Response.error(ErrorCode.NULL_POINT_EXCEPTION);
    }

    // 传递参数不合法
    @ExceptionHandler(IllegalArgumentException.class)
    public Response handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument exception occurred: ", e);
        return Response.error(ErrorCode.ILLEGAL_ARGUMENT);
    }

    // @Valid注解触发
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.error("Method argument not valid exception occurred: ", e);
        String errMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), errMessage);
    }

    // 请求体数据异常
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("Http message not readable exception occurred: ", e);
        return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "请求体数据异常");
    }

    // 请求不存在的接口
    @ExceptionHandler(NoResourceFoundException.class)
    public Response handleNoResourceFoundException(NoResourceFoundException e) {
        log.error("No resource found exception occurred: ", e);
        return Response.error(ErrorCode.NOT_FOUND);
    }

    // 自定义异常
    @ExceptionHandler(BizException.class)
    public Response handleBizException(BizException e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        log.error("\n业务异常:\n    异常定位:{}\n    异常类型:{}\n    异常信息:{}",
                stackTrace.length == 0 ? "" : stackTrace[0], e.getClass(), e.getErrDesc());
        return Response.error(e.getErrCode(), e.getErrDesc());
    }

    // 其他异常
    @ExceptionHandler(Exception.class)
    public Response handleOtherException(Exception e) {
        log.error("Unknown exception occurred: ", e);
        return Response.error(ErrorCode.UNKNOWN_ERROR);
    }
}