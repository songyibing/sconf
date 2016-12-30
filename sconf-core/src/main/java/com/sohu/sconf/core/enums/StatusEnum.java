
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.core.enums;

/**
 * <p>
 * Description: 状态码枚举，包括状态码和对应的描述
 * </p>
 * 
 * @author yibingsong
 * @Date 2016年8月23日 下午3:02:11
 */
public enum StatusEnum {
    
    UN_KNOWN(0, "未知错误"),
    
    CONFIG_FILE_NOT_FOUND(1001, "未找到配置文件"),
    
    CONNECT_ERROR(1002, "连接服务器失败"),
    
    ID_NOT_MATCH(1003, "id不匹配,请检查"),
    
    FATHER_NO_EXITS(2001, "父节点不存在"),
    
    NODE_NOT_EXITS(2003, "该节点不存在"),
    
    DELETE_NODE_ERROR(2004, "删除节点失败"),
    
    BEAN_NOT_DEFINITION(3001, "非静态域所在的类没有定义相应的bean"),

    BEAN_NOT_SINGLE(3002, "非静态域所在的类不是单例"),
    
    DESC_EMPTY(3003, "desc字段不能为空"),
    
    
    ;
    
    private int statusCode;

    private String message;

    public int getStatusCode() {

        return statusCode;
    }

    public void setStatusCode(int statusCode) {

        this.statusCode = statusCode;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    private StatusEnum(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
