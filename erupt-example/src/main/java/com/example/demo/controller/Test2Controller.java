package com.example.demo.controller;

import cn.hutool.core.util.ReflectUtil;
import com.example.demo.utils.JPASchemaSchemaUpdate;
//import com.example.demo.utils.RedefineClassAgent;
import net.openhft.compiler.CachedCompiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.service.EruptCoreService;
import javax.persistence.EntityManagerFactory;
import javax.tools.JavaFileManager;
import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Method;
import java.util.*;


@RestController
public class Test2Controller {

    @Autowired
    private EruptCoreService eruptCoreService;

    @Autowired
    private LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private JPASchemaSchemaUpdate jpaSchemaSchemaUpdate;

    private final CachedCompiler cc = new CachedCompiler(null, null);

    @RequestMapping("/test33")
    public Object test33( ) throws Exception {
        String className = "com.example.demo.model.Test0925";
        String javaCode = "package com.example.demo.model;\n" +
               "/*\n" +
                " * Copyright © 2020-2035 erupt.xyz All rights reserved.\n" +
                " * Author: YuePeng (erupts@126.com)\n" +
                " */\n" +
                "\n" +
                "import javax.persistence.*;\n" +
                "import xyz.erupt.annotation.*;\n" +
                "import xyz.erupt.annotation.sub_erupt.*;\n" +
                "import xyz.erupt.annotation.sub_field.*;\n" +
                "import xyz.erupt.annotation.sub_field.sub_edit.*;\n" +
                "import xyz.erupt.upms.model.base.HyperModel;\n" +
                "import xyz.erupt.jpa.model.BaseModel;\n" +
                "import java.util.Set;\n" +
                "import java.util.Date;\n" +
                "\n" +
                "@Erupt(name = \"Test0925\")\n" +
                "@Table(name = \"erupt_test0925\")\n" +
                "@Entity\n" +
                "public class Test0925_abc extends BaseModel {\n" +
                "\n" +
                "        @EruptField(\n" +
                "                views = @View(\n" +
                "                        title = \"myKey\"\n" +
                "                ),\n" +
                "                edit = @Edit(\n" +
                "                        title = \"myKey\",\n" +
                "                        type = EditType.INPUT, search = @Search, notNull = true,\n" +
                "                        inputType = @InputType\n" +
                "                )\n" +
                "        )\n" +
                "        private String my_key;\n" +
                "\n" +
                "        @EruptField(\n" +
                "                views = @View(\n" +
                "                        title = \"value_text\"\n" +
                "                ),\n" +
                "                edit = @Edit(\n" +
                "                        title = \"value_text\",\n" +
                "                        type = EditType.INPUT, search = @Search, notNull = true,\n" +
                "                        inputType = @InputType\n" +
                "                )\n" +
                "        )\n" +
                "        private String value_text;\n" +
                "\n" +
                "}";
//        Map<ClassLoader, JavaFileManager> fileManagerMap = (Map<ClassLoader, JavaFileManager>) ReflectUtil.getFieldValue(cc, "fileManagerMap");
//        JavaFileManager fileManager = fileManagerMap.get(ClassLoader.getSystemClassLoader());
//        Method compileFromJava = ReflectUtil.getMethodByName(CachedCompiler.class, "compileFromJava");
//        Object invoke = ReflectUtil.invoke(cc, compileFromJava,"com.example.demo.model.Test0925" , javaCode, fileManager);

//        cc.c
//        Class clazz = Class.forName(className); // the class to reload

//// load the bytecode from the .class file
//        URL url = new URL(clazz.getProtectionDomain().getCodeSource().getLocation(),
//                className.replace(".", "/") + ".class");
//        InputStream classStream = url.openStream();
//        byte[] bytecode = IOUtils.toByteArray(classStream);


//        Map<String, byte[]> byteFromJava = cc.getByteFromJava("com.example.demo.model.Test0925_abc", javaCode);
//
//        byte[] bytes = byteFromJava.get(className);
//
//        ClassDefinition definition = new ClassDefinition(clazz, bytes);
//        RedefineClassAgent.redefineClasses(definition);

        Class aClass = cc.loadFromJava("com.example.demo.model.Test0925_abc", javaCode);
        Object o = aClass.newInstance();  // instance of the object declared in 'javaCode'
        jpaSchemaSchemaUpdate.runForClass(null,aClass);
        return null;
    }



}