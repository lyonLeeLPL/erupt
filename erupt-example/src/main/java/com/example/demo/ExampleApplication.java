package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import xyz.erupt.core.annotation.EruptScan;

import java.awt.*;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

@SpringBootApplication
@EntityScan
@EruptScan
public class ExampleApplication extends SpringBootServletInitializer {

    //详细使用方法详见项目内 README.md 文件说明
    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
        try {
            System.setProperty("java.awt.headless", "false");
            Desktop.getDesktop().browse(new URI("http://localhost:18081"));
        } catch (Exception ignore) {
        }
        printEnv();
        System.err.println("详细使用方法，请阅读：README.md");
    }

    //打WAR包的配置
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        printEnv();
        return application.sources(ExampleApplication.class);
    }

    private static void printEnv(){
        //获取环境变量
        Map<String, String> envs = System.getenv();
        envs.forEach( (key,value) ->{
            //System.out.println("env---key是: "+key +" ; "+"env---value是: "+ value);
        });

        //获取jvm属性
        Properties properties = System.getProperties();
        properties.forEach((key, value) -> {
            System.out.println("jvm---key是: "+key +" ; "+"jvm---value是: "+ value);
        });


    }
}
