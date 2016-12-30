
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.scanner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sohu.sconf.Loader;
import com.sohu.sconf.annotations.Sconf;
import com.sohu.sconf.core.common.FieldLocator;
import com.sohu.sconf.core.common.FieldTv;
import com.sohu.sconf.core.common.FieldTvo;
import com.sohu.sconf.core.enums.StatusEnum;
import com.sohu.sconf.core.exception.SconfException;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author yibingsong
 * @Date 2016年8月24日 上午10:31:01
 */
public class AnnotationScanner {
    private static ImmutableMap<FieldLocator, FieldTv> staticFieldMap;
    public static ImmutableMap<FieldLocator, FieldTv> getStaticFieldMap() {
    
        return staticFieldMap;
    }

    public static ImmutableMap<FieldLocator, FieldTvo> getNonStaticFieldMap() {
    
        return nonStaticFieldMap;
    }

    private static ImmutableMap<FieldLocator, FieldTvo> nonStaticFieldMap;

    public static void createFieldMap() throws IllegalArgumentException, IllegalAccessException {
        Map<FieldLocator, FieldTv> innerStaticFieldMap = Maps.newHashMap();
        Map<FieldLocator, FieldTvo> innerNonStaticFieldMap = Maps.newHashMap();
        Set<Field> fields = new Reflections(StringUtils.EMPTY, new FieldAnnotationsScanner())
                .getFieldsAnnotatedWith(Sconf.class);

        for (Field field : fields) {
            Sconf sconf = field.getAnnotation(Sconf.class);
            String desc = sconf.desc();
            if(StringUtils.isEmpty(desc)) throw new SconfException(StatusEnum.DESC_EMPTY);
            int modifiers = field.getModifiers();
            // 静态变量存放到innerStaticFieldMap中
            if (Modifier.isStatic(modifiers)) {
                field.setAccessible(true);
                innerStaticFieldMap.put(new FieldLocator(field.getDeclaringClass().getName(), field.getName()),
                        new FieldTv(field, field.getType(), field.get(null) == null ? StringUtils.EMPTY : field.get(null), desc));
            }
            // 非静态变量存放到
            else {
                Object o = null;
                field.setAccessible(true);
                try {
                    o = Loader.getBean(field.getDeclaringClass());
                } catch (NoUniqueBeanDefinitionException e1) {
                    throw new SconfException(StatusEnum.BEAN_NOT_SINGLE, "class name : %s", field.getDeclaringClass().getName());
                } catch (NoSuchBeanDefinitionException e) {
                    throw new SconfException(StatusEnum.BEAN_NOT_DEFINITION, "class name : %s",
                            field.getDeclaringClass().getName());
                }
                innerNonStaticFieldMap.put(new FieldLocator(field.getDeclaringClass().getName(), field.getName()),
                        new FieldTvo(field, field.getType(), field.get(o) == null ? StringUtils.EMPTY : field.get(o), o, desc));
            }

        }
        staticFieldMap = new ImmutableMap.Builder<FieldLocator, FieldTv>().putAll(innerStaticFieldMap).build();
        nonStaticFieldMap = new ImmutableMap.Builder<FieldLocator, FieldTvo>().putAll(innerNonStaticFieldMap).build();
    }

}
