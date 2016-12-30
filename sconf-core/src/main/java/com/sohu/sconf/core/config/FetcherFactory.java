
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.core.config;

import java.io.IOException;




import com.sohu.sconf.core.enums.StatusEnum;
import com.sohu.sconf.core.exception.SconfException;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author yibingsong
 * @Date 2016年8月23日 下午2:34:26
 */
public class FetcherFactory {
    private static final String PROPERTIES = ".properties";
    private static final String XML = ".xml";

    public static ConfigFetcher getFetcher(String fileName) {
        PropertiesFetcher fetcher = null;
        if (StringUtils.endsWith(fileName, PROPERTIES)) {
            try {
                fetcher = new PropertiesFetcher(fileName);
            } catch (IOException e) {
                throw new SconfException(StatusEnum.CONFIG_FILE_NOT_FOUND, "文件名 ：%s", fileName + "|" + e.getMessage());
            }
        }
        return fetcher;
    }
}
