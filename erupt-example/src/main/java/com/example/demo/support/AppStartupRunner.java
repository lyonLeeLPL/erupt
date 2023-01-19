package com.example.demo.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

import static xyz.erupt.common.utils.CommonUtil.isStartupFromJar;

@Component
public class AppStartupRunner implements ApplicationRunner {
    @Value("${config-compiler.jarRootPath}")
    String jarRootPath;
    @Override
    public void run(ApplicationArguments args)  {
        System.out.println("初始化代码");
        jarLoad();
    }

    private void jarLoad(){
        if (isStartupFromJar(this.getClass())){
            String property = System.getProperty("java.class.path");
            System.out.println(property);
            StringBuffer stringBuffer = new StringBuffer(property);
            File file = new File(jarRootPath);
            File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
            for(File f:fs){					//遍历File[]数组
                if(!f.isDirectory()){
                    stringBuffer.append(";" + f.getAbsolutePath());
                }
            }
            String s = stringBuffer.toString();
            System.setProperty("java.class.path",s);
        }

    }
}