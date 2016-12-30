
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.sohu.sconf.core.enums.StatusEnum;
import com.sohu.sconf.core.exception.SconfException;
import com.sohu.sconf.scanner.AnnotationScanner;
import com.sohu.sconf.zookeeper.PathCreator;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author yibingsong
 * @Date 2016年8月19日 下午4:14:53
 */

public class Loader implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private static String projectId;
    public static String getHost() {
        return "10.11.132.194:2181,10.11.132.195:2181,10.11.132.196:2181";
    }
    public static String getProjectId() {
        return projectId;
    }
    /**
     * 实现ApplicationContextAware接口的context注入函数, 将其存入静态变量.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        Loader.applicationContext = applicationContext; // NOSONAR
    }


    /**
     * 取得存储在静态变量中的ApplicationContext.
     */
    public static ApplicationContext getApplicationContext() {
        checkApplicationContext();
        return applicationContext;
    }

    /**
     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        checkApplicationContext();
        return (T) applicationContext.getBean(name);
    }
    /**
    * @throws NoSuchBeanDefinitionException if no bean of the given type was found
    * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
    */
    public static <T> T getBean(Class<T> className) throws BeansException{
        checkApplicationContext();
        return applicationContext.getBean(className);
    }

//    /**
//     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
//     */
//    @SuppressWarnings("unchecked")
//    public static <T> T getBean(Class<T> clazz) {
//        checkApplicationContext();
//        return (T) applicationContext.getBeansOfType(clazz);
//    }

    /**
     * 清除applicationContext静态变量.
     */
    public static void cleanApplicationContext() {
        applicationContext = null;
    }

    private static void checkApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
        }
    }
    
    
    private String id;

    public String getId() {
        
        return id;
            
    }

    public void setId(String id) {
        
        this.id = id;
        projectId = id;
            
    }
    public void load( ) throws Exception{
        // 扫描注解，检查域是否为静态，放入相应的map中，并检查非静态域的bean是否为单例。
        try {
            AnnotationScanner.createFieldMap();
        } catch(Exception e) {
            throw e;
        }
        // 对于zookeeper中没有的节点，创建节点。对于已经存在的节点，更新域值。
        PathCreator.apply(id);
            
    }
}