
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.zookeeper;

import static com.sohu.sconf.core.utils.Unit.s;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

import com.sohu.sconf.Loader;
import com.sohu.sconf.core.enums.StatusEnum;
import com.sohu.sconf.core.exception.SconfException;
import com.sohu.sconf.core.utils.LogUtils;
import com.sohu.sconf.core.utils.Logger;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author yibingsong
 * @Date 2016年8月23日 下午4:29:33
 */
public class ConnectionWatcher implements Watcher {
    
    private static final Logger logger = LogUtils.getLogger(ConnectionWatcher.class);

    protected String hostString;
    
    private static final ConnectionWatcher INSTANCE = new ConnectionWatcher();

    private enum Type {
        SHORT("short"), INT("int"), LONG("long"), STRING("String");


        final String name;

        Type(String name) {
            this.name = name;
        }

    }
    

    // 10 秒会话时间 ，避免频繁的session expired
    private static final int SESSION_TIMEOUT = 10 * s;

    // 3秒
    private static final int CONNECT_TIMEOUT = 3 * s;
    
    private static final int SLEEP_SECOND = 2;
    
    protected ZooKeeper zk;
    
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    
    protected ConnectionWatcher() {
        super();
    }
  
    public static ConnectionWatcher getInstance() {
        return INSTANCE;
    }
    
    public void connect(String hosts) throws IOException, InterruptedException {
        hostString = hosts;
        // 异步
        zk = new ZooKeeper(hostString, SESSION_TIMEOUT, this);
        countDownLatch.await(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
        logger.info("zookeeper: " + hosts + " , connected.");
    }
    
    @Override
    public void process(WatchedEvent event) {
        if(event.getType().equals(EventType.NodeDataChanged)){
            logger.info("NodeDataChanged");
            String value = "";
            String path = event.getPath();
            try {
                value = new String(zk.getData(path, true, null));
            } catch (KeeperException | InterruptedException e) {
                
                // TODO Auto-generated catch block
                e.printStackTrace();
                    
            }
            System.out.println("changed value : " + value);
            
            processPath(path, event, value);
            
        }
        if (event.getState() == KeeperState.SyncConnected) {
            logger.info("zk SyncConnected");
            countDownLatch.countDown();
        } else if (event.getState().equals(KeeperState.Disconnected)) {
            // 这时收到断开连接的消息，这里其实无能为力，因为这时已经和ZK断开连接了，只能等ZK再次开启了
            logger.error("zk Disconnected");
        } else if (event.getState().equals(KeeperState.Expired)) {
            // 过期后需要重新建立连接
            logger.error("zk Expired");
            reconnect();
        } else if (event.getState().equals(KeeperState.AuthFailed)) {
            logger.error("zk AuthFailed");
        }
        
    }
    
    private void processPath(String path, WatchedEvent event, String value) {
        int index = path.lastIndexOf("/");
        path = path.substring(index + 1, path.length());
        String[] strings = path.split("_");
        String[] vStrings = value.split("#");
        String type = vStrings[1];
        String fieldValueString = vStrings[2];
        String className = strings[0];
        String fieldName = strings[1];
        Field field = null;
        try {
            field = Class.forName(className).getDeclaredField(fieldName);
        } catch (NoSuchFieldException | SecurityException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        String name = field.getDeclaringClass().getSimpleName();
        name = name.substring(0, 1).toLowerCase() + name.substring(1, name.length());
        Object o = Loader.getBean(name);
        try {
            if (StringUtils.equalsIgnoreCase(Type.INT.toString(), type)) {
                field.set(o, Integer.valueOf(fieldValueString));
                logger.info("配置项[%s]的值被修改为[%s]", field.getName(), fieldValueString);
            } else if (StringUtils.equalsIgnoreCase(Type.SHORT.toString(), type)) {
                field.set(o, Short.valueOf(fieldValueString));
                logger.info("配置项[%s]的值被修改为[%s]", field.getName(), fieldValueString);
            } else if (StringUtils.equalsIgnoreCase(Type.LONG.toString(), type)) {
                field.set(o, Long.valueOf(fieldValueString));
                logger.info("配置项[%s]的值被修改为[%s]", field.getName(), fieldValueString);
            } else if (StringUtils.equalsIgnoreCase(Type.STRING.toString(), type)) {
                field.set(o, String.valueOf(fieldValueString));
                logger.info("配置项[%s]的值被修改为[%s]", field.getName(), fieldValueString == "" ? "NULL" : fieldValueString);
            } else
                throw new SconfException(StatusEnum.UN_KNOWN, "未找到匹配的类型");
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            Method m = o.getClass().getDeclaredMethod(field.getName() + "Changed");
            m.invoke(o);
        } catch (NoSuchMethodException e) {
            logger.info("没有method方法");
        } catch (Exception e1) {
            logger.error(e1); 
        } 
           
        
       
    }

    /**
     * 含有重试机制的retry，一直尝试连接，直至成功
     */
    public void reconnect() {

        logger.info("reconnect....");

        int retries = 0;
        while (true) {
            try {
                if (!zk.getState().equals(States.CLOSED)) {
                    break;
                }
                logger.info("zookeeper lost connection, reconnect");
                close();
                connect(hostString);
            } catch (Exception e) {
                logger.error(retries + "\t" + e.toString());
                // sleep then retry
                try {
                    TimeUnit.SECONDS.sleep(SLEEP_SECOND);
                } catch (InterruptedException e1) {
                    
                }
            }
        }
    }

    
    public void close() throws InterruptedException {
        zk.close();
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

}
