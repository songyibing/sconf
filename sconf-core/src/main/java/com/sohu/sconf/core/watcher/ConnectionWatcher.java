
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.core.watcher;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

import com.sohu.sconf.core.utils.LogUtils;
import com.sohu.sconf.core.utils.Logger;
import static com.sohu.sconf.core.utils.Unit.*;

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
        try {
            zk.getData("/project222/com.syb.test.App_name1", new ConnectionWatcher(), null);
        } catch (KeeperException e) {
            
            // TODO Auto-generated catch block
            e.printStackTrace();
                
        }
        countDownLatch.await(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
        logger.info("zookeeper: " + hosts + " , connected.");
    }
    
    @Override
    public void process(WatchedEvent event) {
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
        if(event.getType().equals(EventType.NodeDataChanged)){
            logger.info("NodeDataChanged");
            String value = "";
            try {
                value = new String(zk.getData(event.getPath(), true, null));
            } catch (KeeperException | InterruptedException e) {
                
                // TODO Auto-generated catch block
                e.printStackTrace();
                    
            }
            System.out.println("changed value : " + value);
            String path = event.getPath();
           // processPath(path, event, value);
            
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
