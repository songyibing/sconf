
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.core.common;

import java.lang.reflect.Field;

/**
 * <p>
 * Description: field的type,value and object。用于非静态域
 * </p>
 * 
 * @author yibingsong
 * @Date 2016年8月24日 下午1:51:51
 */
public class FieldTvo extends FieldTv {
    public FieldTvo(Field field, Class<?> type, Object value, Object object, String desc) {

        super(field, type, value, desc);
        this.object = object;

    }

    public Object getObject() {

        return object;

    }

    private final Object object;
}
