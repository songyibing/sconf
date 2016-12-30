
/*
 * Copyright (c) 2016 Sohu. All Rights Reserved
 */
package com.sohu.sconf.zookeeper;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;

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
 * @Date 2016年8月23日 下午5:08:55
 */
public class DataModifier extends ConnectionWatcher {

    private static final Logger logger = LogUtils.getLogger(DataModifier.class);

    private static final int RETRY_TIMES = 3;
    private static final int RETRY_PEROID_SECOND = 2;
    private static final DataModifier INSTANCE = new DataModifier();
    private static final Charset CHARSET = Charset.forName("utf-8");
    
    protected boolean isConnected() {
        return zk.getState().equals(States.CONNECTED);
    }
    
    private DataModifier() {
        super();
    }
    
    public static DataModifier getInstance() {
        return INSTANCE;
    }

    public boolean exist(String path) throws InterruptedException, KeeperException {
        int retry = 1;
        Stat stat;
        while (true) {
            try {
                stat = zk.exists(path, false);
                return stat != null;
            } catch (KeeperException.SessionExpiredException e) {
                throw e;
            } catch (KeeperException e1) {
                if (retry == RETRY_TIMES) {
                    throw e1;
                }
                logger.error("查询路径%s是否存在的操作失败，正在重试..", path);
                retry++;
                TimeUnit.SECONDS.sleep(RETRY_PEROID_SECOND);
            }
        }
    }
    
    public void delete(String path) {
//        zk.delete(path, version);
        List<String> list;
        try {
            list = zk.getChildren(path, false);
            for(String subPath : list) {
                if(path.equals("/")) throw new SconfException(StatusEnum.DELETE_NODE_ERROR, "不能删除跟路径");    
                else delete(path + "/" + subPath);
            }
            zk.delete(path, -1);
        } catch (KeeperException | InterruptedException e) {
            throw new SconfException(StatusEnum.DELETE_NODE_ERROR, "path : " + path);    
        }
    }
    
    public String read(String path) throws InterruptedException, KeeperException {
        int retry = 1;
        byte[] bytes;
        for(; ;) {
            try {
                bytes = zk.getData(path, false, new Stat());
                return new String(bytes, CHARSET);
            } catch (KeeperException.NoNodeException e) {
                throw new SconfException(StatusEnum.FATHER_NO_EXITS, "path : " + path);
            } catch (KeeperException e1) {
                if (retry == RETRY_TIMES) {
                    throw e1;
                }
                logger.error("读取失败， 正在重试..", path);
                retry++;
                TimeUnit.SECONDS.sleep(RETRY_PEROID_SECOND);
            }
        }

    }
    
//    public 
    
    
    public void create(String path, String data) throws InterruptedException, KeeperException {
        int retry = 1;
        while(true) {
            try {
                zk.create(path, data.getBytes(CHARSET), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                return;
            } catch (KeeperException.SessionExpiredException e) {
               throw e;
            } catch(KeeperException.NodeExistsException e) {
                logger.info("节点%s已经存在，不需要创建", path);
                return;
            } catch(KeeperException.NoNodeException e) {
                throw new SconfException(StatusEnum.FATHER_NO_EXITS);
            } catch (KeeperException e1) {
                
                if (retry == RETRY_TIMES) {
                    throw e1;
                }
                logger.error("创建路径%s的操作失败，正在重试..", path);
                retry++;
                TimeUnit.SECONDS.sleep(RETRY_PEROID_SECOND);
            }
        }
    }

    public void write(String path, String data) throws KeeperException, InterruptedException{
        int retry = 1;
        for( ; ;) {
            try {
                zk.setData(path, data.getBytes(CHARSET), -1);
                return;
            } catch (KeeperException.SessionExpiredException e) {
                throw e;
            } catch(KeeperException.NoNodeException e) {
                throw new SconfException(StatusEnum.FATHER_NO_EXITS);
            } catch (KeeperException e1) {

                if (retry == RETRY_TIMES) {
                    throw e1;
                }
                logger.error("对节点%s设置数据的操作失败，正在重试..", path);
                retry++;
                TimeUnit.SECONDS.sleep(RETRY_PEROID_SECOND);
            }
        }
    }

    public List<String> getChildren(String path) throws KeeperException, InterruptedException{
        int retry = 1;
        for(; ;) {
            try {
                List<String> list = zk.getChildren(path, false);
                return list;
            }  catch (KeeperException.SessionExpiredException e) {
                throw e;
            } catch(KeeperException.NoNodeException e) {
                throw new SconfException(StatusEnum.FATHER_NO_EXITS);
            } catch (KeeperException e1) {

                if (retry == RETRY_TIMES) {
                    throw e1;
                }
                logger.error("对节点%s获取子节点的操作失败，正在重试..", path);
                retry++;
                TimeUnit.SECONDS.sleep(RETRY_PEROID_SECOND);
            }
        }

    }
}
