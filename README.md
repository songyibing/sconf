#分布式配置管理

##设计思路：
使用zookeeper做集中式配置存储，客户端启动后连接zookeeper，根据项目名称，监听对应的节点，并根据bean的域注解进行注册。开发人员通过web界面更改配置值，客户端收到事件后通过反射修改域值，并回调特定的接口。项目必须在spring环境下运行。

支持int short long String 类型的配置项。支持静态/非静态类的配置项。如果一个类只包含静态配置项，则无需在spring中为其定义bean。

##测试项目（sconf-test）运行方法:
（1）搭建zookeeper环境,将zookeeper地址作为args.properties文件的zookeeper.url的属性值。

（2）运行TestClass中的main方法。

（3）修改zookeepe中节点```/project222/com.syb.test.TestClass_name```的值为```描述#int#10```

（4）观察日志。

##使用方法（参考sconf-test项目）：
（1）搭建zookeeper环境，并新建项目，在src/main/resources下新建args.properties文件。将zookeeper地址作为zookeeper.url的属性值。

（2）将sconf-core 和 sconf-client 两个项目的jar包安装到本地仓库。

（3）创建一个类，类中的配置项以@Sconf标记，附带描述文字desc（描述文字可以任意写）。并以单例在spring中配置（要求按照spring默认规则命名对象，如StoreService命名为storeService)。同时，配置sconf的启动类com.sohu.sconf.Loader，指定项目id（与已有id不重复即可）。
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

<bean id="testClass"  class="com.syb.test.TestClass"></bean>
```

（4）修改配置项的值。精力有限，尚未做web界面，目前只能通过linux命令或者zookeeper可视化工具对配置进行修改。修改完毕后，程序中的值也被修改，并回调【域名+Changed】方法。

##已知缺陷：
1. 由于配置项所对应的域会被一个线程修改和多个线程读取，为保证可见性，需要加上volatile关键字。尽管配置项读多写少，冲突很少发生，但volatile却始终在影响性能。
2. 无法保证最终一致性。短时间连续的修改，会导致zookeeper的watcher注册跟不上，可能最后一次修改没被通知到。解决办法：定时读取zk中的数据。

注:本项目不涉及任何公司
