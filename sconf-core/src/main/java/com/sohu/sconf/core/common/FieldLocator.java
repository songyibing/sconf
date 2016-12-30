
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.core.common;
/**
 * <p>
 *     Description: field的定位描述，包括类的全名和域名。
 * </p>
 * @author yibingsong
 * @Date 2016年8月24日 上午11:28:23
 */
public class FieldLocator {
    private short i;
    public String getClassName() {
    
        return className;
    }
    public String getFieldName() {
    
        return fieldName;
    }
    public FieldLocator(String className, String fieldName) {
        super();
        this.className = className;
        this.fieldName = fieldName;
    }
    private final String className;
    private final String fieldName;
    
    public String getNodeName() {
        return className + "_" + fieldName;
    }
    
    public static void main(String[] args) throws NoSuchFieldException, SecurityException {
        System.out.println(new FieldLocator(null, null).getClass().getDeclaredField("i").getType());
    }
}

    