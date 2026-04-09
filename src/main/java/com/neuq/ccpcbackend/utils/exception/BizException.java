package com.neuq.ccpcbackend.utils.exception;

import com.neuq.ccpcbackend.utils.response.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BizException extends RuntimeException {
    protected String errCode;
    protected String errDesc;

    public BizException() {
        super();
    }

    public BizException(ErrorCode errorInfoInterface) {
        super(errorInfoInterface.getErrCode());
        this.errCode = errorInfoInterface.getErrCode();
        this.errDesc = errorInfoInterface.getErrDesc();
    }

    public BizException(ErrorCode errorInfoInterface, Throwable cause) {
        super(errorInfoInterface.getErrCode(), cause);
        this.errCode = errorInfoInterface.getErrCode();
        this.errDesc = errorInfoInterface.getErrDesc();
    }

    public BizException(String errCode, String errDesc) {
        super(errCode);
        this.errCode = errCode;
        this.errDesc = errDesc;
    }

    public BizException(String errCode, String errDesc, Throwable cause) {
        super(errCode, cause);
        this.errCode = errCode;
        this.errDesc = errDesc;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
