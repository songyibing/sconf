
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.core.exception;

import org.apache.commons.lang.StringUtils;

import com.sohu.sconf.core.enums.StatusEnum;

/**
 * <p>
 * Description:缓存异常类
 * </p>
 * 
 * @author yibingsong
 * @Date 2016年7月18日 下午3:30:47
 */
public class SconfException extends RuntimeException {

        
    private static final long serialVersionUID = 1L;

    // 结果码
    private StatusEnum statusEnum;

    // 额外的异常信息
    private String errorMsg;

    public SconfException(StatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }

    public SconfException(StatusEnum statusEnum, String msgSkeleton, String... params) {
        this.statusEnum = statusEnum;
        this.errorMsg = (msgSkeleton == null ? null : String.format(msgSkeleton, (Object[])params));
    }
    
    public SconfException(StatusEnum statusEnum, String errorMsg) {
        this.statusEnum = statusEnum;
        this.errorMsg = errorMsg;
    }

    /**
     * 重写getMessage，附带结果枚举的信息
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(200);
        if (super.getMessage() != null) {
            sb.append(super.getMessage());
        }
        sb.append(" 异常原因：");
        sb.append(this.getStatusEnum().getStatusCode());
        sb.append("|").append(this.getStatusEnum().getMessage());
        if (StringUtils.isNotBlank(errorMsg)) {
            sb.append("|");
            sb.append(errorMsg);
        }
        return sb.toString();
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public StatusEnum getStatusEnum() {
        return statusEnum;
    }

}
