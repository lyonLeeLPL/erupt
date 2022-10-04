package com.example.demo.utils;

import net.openhft.compiler.CachedCompiler;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class JPASchemaExtractor2 implements ApplicationRunner {
    private static final String SCHEMA_SQL2 = "db/base/new_create-ddl_2_%s.sql";
    private final CachedCompiler cc = new CachedCompiler(null, null);
    @Autowired
    LocalContainerEntityManagerFactoryBean fb;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Class aClass = (Class) test_asm();

        StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(fb.getJpaPropertyMap())
                .build();

        MetadataSources metadata = new MetadataSources(standardServiceRegistry);
        List<String> managedClassNames = fb.getPersistenceUnitInfo().getManagedClassNames();
        for (String managedClassName : managedClassNames) {
            metadata.addAnnotatedClassName(managedClassName);
        }
        metadata.addAnnotatedClass(aClass);
        metadata.addAnnotatedClassName("com.example.demo.model.Test0925");

        MetadataImplementor metadataImplementor = (MetadataImplementor) metadata.getMetadataBuilder().build();
        SchemaExport schemaExport = new SchemaExport();
        String outputFile = getOutputFilename();
        schemaExport.setOutputFile(outputFile);
        schemaExport.setDelimiter(";");
        schemaExport.setFormat(false);

        EnumSet<TargetType> enumSet = EnumSet.of(TargetType.SCRIPT);
        schemaExport.create(enumSet, metadataImplementor);

//        EnumSet<TargetType> enumSet2 = EnumSet.of(TargetType.DATABASE);
//        schemaExport.create(enumSet2, metadataImplementor);

        schemaExport.execute(enumSet, SchemaExport.Action.CREATE , metadataImplementor, standardServiceRegistry);
    }

    public Object test_asm() throws Exception {
        String javaCode = "package com.example.demo.model;\n" +
                "/* "+
                "* Copyright © 2020-2035 erupt.xyz All rights reserved. "+
                "* Author: YuePeng (erupts@126.com) "+
                "*/ "+
                "import javax.persistence.*; "+
                "import xyz.erupt.annotation.*; "+
                "import xyz.erupt.annotation.sub_erupt.*; "+
                "import xyz.erupt.annotation.sub_field.*; "+
                "import xyz.erupt.annotation.sub_field.sub_edit.*; "+
                "import xyz.erupt.upms.model.base.HyperModel; "+
                "import xyz.erupt.jpa.model.BaseModel; "+
                "import java.util.Set; "+
                "import java.util.Date; "+
                "@Erupt(name = \"Test0925\") "+
                "@Table(name = \"erupt_test0925\") "+
                "@Entity "+
                "public class Test0925 extends BaseModel { "+
                "@EruptField( "+
                "views = @View( "+
                "title = \"myKey\" "+
                "), "+
                "edit = @Edit( "+
                "title = \"myKey\", "+
                "type = EditType.INPUT, search = @Search, notNull = true, "+
                "inputType = @InputType "+
                ") "+
                ") "+
                "private String my_key; "+
                "} ";
        Class aClass = cc.loadFromJava("com.example.demo.model.Test0925", javaCode);
        Object o = aClass.newInstance();  // instance of the object declared in 'javaCode'

        //groovy提供了一种将字符串文本代码直接转换成Java Class对象的功能
//        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
//        //里面的文本是Java代码,但是我们可以看到这是一个字符串我们可以直接生成对应的Class<?>对象,而不需要我们写一个.java文件
//        Class<?> clazz = groovyClassLoader.parseClass(javaCode);
//        groovyClassLoader.
//
//        Class<?> aClass = groovyClassLoader.loadClass("com.example.demo.model.Test0925");
//        Class<?> aClass1 = Class.forName("com.example.demo.model.Test0925", false, groovyClassLoader);
//        aClass1 = Class.forName("com.example.demo.model.Test0925", false,  ClassLoader.getSystemClassLoader());
//        Class<?> aClass2 = ClassLoader.getSystemClassLoader().loadClass("com.example.demo.model.Test0925");
//        ClassLoader.getSystemClassLoader();
//        String testEruptName = "testErupt";
//        aClass.getClassLoader().getParent().getParent();
//        EruptModel eruptModel = EruptCoreService.initEruptModel(clazz);
//        EruptCoreService.putErupt(testEruptName,eruptModel);
//        EruptCoreService.getErupts().add(eruptModel);
//
//        JsonObject data = new JsonObject();

//        data.addProperty("my_key", "999");
//        Gson gson = new Gson();
//        Object o = gson.fromJson(data.toString(), eruptModel.getClazz());
//        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).addData(eruptModel, o);

        return aClass;
    }



    private static String getOutputFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDate = sdf.format(Calendar.getInstance().getTime());

        return String.format(SCHEMA_SQL2, currentDate);
    }
}