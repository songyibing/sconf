#分布式配置管理

##设计思路： 使用zookeeper做集中式配置存储，客户端启动后连接zookeeper，根据项目名称，监听对应的目录，并根据bean的域注解进行注册。开发人员通过web界面更改配置值，客户端收到事件后通过反射修改域值，并回调特定的接口。项目必须在spring环境下运行。
##使用方法：
（1）将sconf-core 和 sconf-client 两个项目的jar包安装到本地仓库。

（2）新建项目，创建一个类，类中的配置项以@Sconf标记，附带描述文字desc（描述文字可以任意写）。并以单例在spring中配置（要求按照spring默认规则命名对象，如StoreService命名为storeService)。同时，配置sconf的启动类com.sohu.sconf.Loader。
例如，类代码如下：
```Java
package com.syb.test;
public class TestClass
{
    @Sconf(desc="描述")
    private volatile int name = 56;

    // 回调方法
    public void nameChanged() {
        System.out.println("fiele value is changed to " + name);    
    }   
}
```

配置代码如下：
```
<bean class="com.sohu.sconf.Loader" init-method="load">
        <property name="id" value="123456"/>       
</bean>

<bean class="com.syb.test.TestClass"></bean>
```

（3）修改配置项的值。精力有限，尚未做web界面，目前只能通过linux命令或者zookeeper可视化工具对配置进行修改。修改完毕后，程序中的值也被修改，并回调【域名+Changed】方法。
