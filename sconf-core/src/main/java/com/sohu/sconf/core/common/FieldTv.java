
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.core.common;

import java.lang.reflect.Field;

/**
 * <p>
 * Description: Field的type,value。用于静态域。
 * </p>
 * 
 * @author yibingsong
 * @Date 2016年8月24日 上午11:24:56
 */
public class FieldTv {
    public Class<?> getType() {

        return type;
    }

    public Object getValue() {

        return value;
    }

    public FieldTv(Field field, Class<?> type, Object value, String desc) {
        super();
        this.field = field;
        this.type = type;
        this.value = value;
        this.desc = desc;
    }

    public Field getField() {
        
        return field;
            
    }

    public String getDesc() {
        return desc;
    }

    private final Class<?> type;
    private final Object value;
    private final Field field;
    private final String desc;
}
