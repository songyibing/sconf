
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.zookeeper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import com.sohu.sconf.Loader;
import com.sohu.sconf.core.common.FieldLocator;
import com.sohu.sconf.core.common.FieldTv;
import com.sohu.sconf.core.common.FieldTvo;
import com.sohu.sconf.core.enums.StatusEnum;
import com.sohu.sconf.core.exception.SconfException;
import com.sohu.sconf.core.utils.LogUtils;
import com.sohu.sconf.core.utils.Logger;
import com.sohu.sconf.scanner.AnnotationScanner;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author yibingsong
 * @Date 2016年8月24日 下午2:27:09
 */
public class PathCreator {
    private static final Logger LOGGER = LogUtils.getLogger(PathCreator.class);

    // 空字符串的提示
    private static final String NULL_STRING_CHIN = "空字符串";

    private static final String BASE_PATH = "/";

    private static final String SLASH = "/";

    private static final String SIGN = "#";

    private static final String DEFAULT = "default";

    private enum Type {
        SHORT("short"), INT("int"), LONG("long"), STRING("String");


        final String name;

        Type(String name) {
            this.name = name;
        }

    }

    private static void createNonStaticNode(String projectPath, DataModifier connection, Set<String> zkNodeSet) {
        // 创建子节点
        boolean exist;
        for (Entry<FieldLocator, FieldTvo> entry : AnnotationScanner.getNonStaticFieldMap().entrySet()) {
            FieldLocator fieldLocator = entry.getKey();
            FieldTvo fieldTvo = entry.getValue();
            // 节点路径 ex: /projectName/com.xx.xx.className_fieldName
            String fieldPath = projectPath + SLASH + fieldLocator.getNodeName();
            // 对象
            Object object = fieldTvo.getObject();
            // 域
            Field field = fieldTvo.getField();
            // 类型
            String type = fieldTvo.getType().getSimpleName();
            // 描述
            String desc = fieldTvo.getDesc();
            // 节点值 ex: int_1
            String value = desc + SIGN + type + SIGN + fieldTvo.getValue().toString();

            try {
                exist = connection.exist(fieldPath);
                if (!exist) {
                    connection.create(fieldPath, value);
                } else {
                    zkNodeSet.remove(fieldPath);
                    // 读取zookeeper中的值
                    String _value = connection.read(fieldPath);
                    String[] strings = StringUtils.split(_value, SIGN);
                    String fieldValueString = strings[2];
                    if(!type.equals(strings[1])) {
                        LOGGER.info("变量[%s]声明的类型和zookeeper存储的类型不同,将以代码声明的类型为准。([%s]->[%s])", field.getName(), strings[1], type);
                        connection.write(fieldPath, value);
                        fieldValueString = fieldTvo.getValue().toString();
                    }
                    // 只有类型不发生变化，才需要判断描述是否相等。否则会导致上一步的值错误。
                    else if(!desc.equals(strings[0])) {
                        LOGGER.info("变量[%s]的描述发生了变化，将更新到zookeeper中。([%s]->[%s])", field.getName(), strings[0], desc);
                        value = desc + SIGN + strings[1] + SIGN + strings[2];
                        connection.write(fieldPath, value);
                    }

                    if (StringUtils.equalsIgnoreCase(Type.INT.toString(), type)) {
                        field.set(object, Integer.valueOf(fieldValueString));
                        LOGGER.info("配置项[%s]的值被初始化为[%s]", field.getName(), fieldValueString);
                    } else if (StringUtils.equalsIgnoreCase(Type.SHORT.toString(), type)) {
                        field.set(object, Short.valueOf(fieldValueString));
                        LOGGER.info("配置项[%s]的值被初始化为[%s]", field.getName(), fieldValueString);
                    } else if (StringUtils.equalsIgnoreCase(Type.LONG.toString(), type)) {
                        field.set(object, Long.valueOf(fieldValueString));
                        LOGGER.info("配置项[%s]的值被初始化为[%s]", field.getName(), fieldValueString);
                    } else if (StringUtils.equalsIgnoreCase(Type.STRING.toString(), type)) {
                        field.set(object, String.valueOf(fieldValueString));
                        LOGGER.info("配置项[%s]的值被初始化为[%s]", field.getName(), fieldValueString == "" ? NULL_STRING_CHIN : fieldValueString);
                    } else
                        throw new SconfException(StatusEnum.UN_KNOWN, "未找到匹配的类型");
                }
            } catch (Exception e) {
                throw new SconfException(StatusEnum.UN_KNOWN, e.getMessage());
            }
        }
    }

    private static void createStaticNode(String projectPath, DataModifier connection, Set<String> zkNodeSet) {
        boolean exist;
        for (Entry<FieldLocator, FieldTv> entry : AnnotationScanner.getStaticFieldMap().entrySet()) {
            FieldLocator fieldLocator = entry.getKey();
            FieldTv fieldTv = entry.getValue();
            // 节点路径 ex: /projectName/com.xx.xx.className_fieldName
            String fieldPath = projectPath + SLASH + fieldLocator.getNodeName();
            Field field = fieldTv.getField();
            String type = fieldTv.getType().getSimpleName();
            // 描述
            String desc = fieldTv.getDesc();
            // 节点值 ex: int_1
            String value = type + SIGN + fieldTv.getValue().toString();
            try {
                exist = connection.exist(fieldPath);
                if (!exist) {
                    connection.create(fieldPath, value);
                } else {
                    zkNodeSet.remove(fieldPath);
                    // 读取zookeeper中的值
                    String _value = connection.read(fieldPath);
                    String[] strings = StringUtils.split(_value, SIGN);
                    String fieldValueString = strings[2];

                    if(!type.equals(strings[1])) {
                        LOGGER.info("变量[%s]声明的类型和zookeeper存储的类型不同,将以代码声明的类型为准。([%s]->[%s])", field.getName(), strings[1], type);
                        connection.write(fieldPath, value);
                        fieldValueString = fieldTv.getValue().toString();
                    }
                    // 只有类型不发生变化，才需要判断描述是否相等。否则会导致上一步的值错误。
                    else if(!desc.equals(strings[0])) {
                        LOGGER.info("变量[%s]的描述发生了变化，将更新到zookeeper中。([%s]->[%s])", field.getName(), strings[0], desc);
                        value = desc + SIGN + strings[1] + SIGN + strings[2];
                        connection.write(fieldPath, value);
                    }
                    if (StringUtils.equalsIgnoreCase(Type.INT.toString(), type)) {
                        field.set(null, Integer.valueOf(fieldValueString));
                        LOGGER.info("配置项[%s]的值被初始化为[%s]", field.getName(), fieldValueString);
                    } else if (StringUtils.equalsIgnoreCase(Type.SHORT.toString(), type)) {
                        field.set(null, Short.valueOf(fieldValueString));
                        LOGGER.info("配置项[%s]的值被初始化为[%s]", field.getName(), fieldValueString);
                    } else if (StringUtils.equalsIgnoreCase(Type.LONG.toString(), type)) {
                        field.set(null, Long.valueOf(fieldValueString));
                        LOGGER.info("配置项[%s]的值被初始化为[%s]", field.getName(), fieldValueString);
                    } else if (StringUtils.equalsIgnoreCase(Type.STRING.toString(), type)) {
                        field.set(null, String.valueOf(fieldValueString));
                        LOGGER.info("配置项[%s]的值被初始化为[%s]", field.getName(), fieldValueString == "" ? NULL_STRING_CHIN : fieldValueString);
                    } else
                        throw new SconfException(StatusEnum.UN_KNOWN, "未找到匹配的类型");
                }
            } catch (Exception e) {
                throw new SconfException(StatusEnum.UN_KNOWN, e.getMessage());
            }

        }
    }

    private static void deleteObsoleteNode(DataModifier dataModifier, Set<String> zkNodeSet) {
        for(String path : zkNodeSet) {
            dataModifier.delete(path);
            LOGGER.info("删除节点%s", path);
        }
    }

    public static void apply(String id) {

        // 连接zookeeper
        DataModifier connection = DataModifier.getInstance();
        try {
            connection.connect(Loader.getHost());
        } catch (IOException e) {
            throw new SconfException(StatusEnum.CONNECT_ERROR, e.getMessage());
        } catch(InterruptedException e1) {
            throw new SconfException(StatusEnum.CONNECT_ERROR, e1.getMessage());
        }

        // 查看根节点是否存在，如果不存在，则创建。如果存在，检查id是否匹配。
        boolean exist;
        String projectPath = BASE_PATH + "project222";
        try {
            exist = connection.exist(projectPath);
            if (!exist) {
                connection.create(projectPath, id);
            } else {
                String _id = connection.read(projectPath);
                if (!id.equals(_id))
                    throw new SconfException(StatusEnum.ID_NOT_MATCH);
            }
        } catch (SconfException e) {
            throw e;
        } catch (Exception e) {
            throw new SconfException(StatusEnum.UN_KNOWN);
        }
        Set<String> zkNodeSet;
        List<String> zkNodeList;
        try {
            zkNodeSet = new HashSet<String>();
            zkNodeList = connection.getChildren(projectPath);
            for(String path : zkNodeList) {
                zkNodeSet.add(projectPath + SLASH + path);
            }
        } catch (Exception e) {
            throw new SconfException(StatusEnum.CONNECT_ERROR);
        }
        createNonStaticNode(projectPath, connection, zkNodeSet);
        createStaticNode(projectPath, connection, zkNodeSet);
        deleteObsoleteNode(connection, zkNodeSet);
        try {
            List<String> childrenList = connection.getZk().getChildren(projectPath, false);
            for(String s : childrenList) {
                connection.getZk().getData(projectPath + "/" + s, connection , new Stat());
            }
        } catch (KeeperException | InterruptedException e) {
            
            // TODO Auto-generated catch block
            e.printStackTrace();
                
        }
    }
}
