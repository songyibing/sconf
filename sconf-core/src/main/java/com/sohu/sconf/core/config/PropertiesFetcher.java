
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.core.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.map.UnmodifiableMap;

import com.sohu.sconf.core.utils.ClassLoaderUtil;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author yibingsong
 * @Date 2016年8月23日 下午2:17:17
 */
public class PropertiesFetcher implements ConfigFetcher {

    private static Map<String, String> map;

    private final String fileName;

    PropertiesFetcher(String fileName) throws IOException {
        this.fileName = fileName;
        init();

    }

    @SuppressWarnings("unchecked")
    private void init() throws IOException {
        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<String, String>();
        Properties pro = new Properties();
        String classPath = ClassLoaderUtil.getLoader().getResource(fileName).getPath();
        pro.load(new FileInputStream(classPath));
        for (Entry<Object, Object> entry : pro.entrySet()) {
            concurrentHashMap.put((String) entry.getKey(), (String) entry.getValue());
        }
        map = UnmodifiableMap.decorate(concurrentHashMap);
    }

    @Override
    public String getValue(String key) {
        return map.get(key);
    }

}
