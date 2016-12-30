
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.core.enums;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;

/**
 * <p>
 *     Description: 
 * </p>
 * @author yibingsong
 * @Date 2016年8月24日 上午10:41:36
 */
public enum FieldType {
    SHORT(Short.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    STRING(String.class)
    ;
    
    private static Map<Class<?>, FieldType> innerMap;
    
    private FieldType(Class<?> clazz) {
        this.clazz = clazz;
        
    }
    
    private Class<?> clazz;
    
    
//    public static FieldType get
}

    