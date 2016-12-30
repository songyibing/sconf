
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.core.utils;

/**
 * <p>
 *     Description: 日志接口
 * </p>
 * @author yibingsong
 * @Date 2016年8月23日 下午4:32:02
 */
public interface Logger {
    
    public void info(Object message);

    public void info(String message, Object... params);
      

    public void error(Object message);

    public void error(String message, Object... params);
}

    