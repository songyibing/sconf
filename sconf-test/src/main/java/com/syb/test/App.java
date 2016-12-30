package com.syb.test;

import com.sohu.sconf.annotations.Sconf;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class App

{
    @Sconf(desc="描述")
    private int name1 = 56;

    public void name1Changed() {
        System.out.println("hello");
    }
    public static void main( String[] args )
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationConte" +
                "xt.xml");
        System.out.println( "Hello World!" );
        while(true);
    }
}
