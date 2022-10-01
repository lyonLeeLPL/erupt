package com.example.demo.controller;

import com.example.demo.model.jin_gen.NewTest;
import com.example.demo.utils.JPASchemaExtractor2;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import groovy.lang.GroovyClassLoader;
import org.hibernate.*;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.internal.SessionFactoryBuilderImpl;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.model.relational.internal.SqlStringGenerationContextImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.EventEngine;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.mapping.MetadataSource;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistryFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.internal.ExceptionHandlerLoggedImpl;
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool;
import org.hibernate.tool.schema.spi.DelayedDropAction;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfiguration;
import org.springframework.boot.configurationprocessor.MetadataStore;
import org.springframework.core.type.StandardClassMetadata;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.config.HibernateConfig;

import javax.annotation.processing.ProcessingEnvironment;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.text.SimpleDateFormat;
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
    private JPASchemaExtractor2 jpaSchemaExtractor2;


    @RequestMapping("/test33")
    public Object test33( ) throws Exception {
//        Map<String, String> settings = new HashMap<>();
//        settings.put("connection.driver_class", "com.mysql.jdbc.Driver");
//        settings.put("dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
//        settings.put("hibernate.connection.url", "jdbc:mysql://152.70.87.42:33306/erupt-example?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai");
//        settings.put("hibernate.connection.username", "root");
//        settings.put("hibernate.connection.password", "5601564a");
//        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
//                .applySettings(settings).build();
//        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
//        Metadata metadata1 = metadataSources.buildMetadata();
//        EnumSet<TargetType> enumSet = EnumSet.of(TargetType.STDOUT);
//        SchemaExport schemaExport = new SchemaExport();
//        schemaExport.execute(enumSet, SchemaExport.Action.BOTH, metadataSources.buildMetadata());

        StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(localContainerEntityManagerFactoryBean.getJpaPropertyMap())
                .build();

        MetadataSources metadata = new MetadataSources(standardServiceRegistry);
        List<String> managedClassNames = localContainerEntityManagerFactoryBean.getPersistenceUnitInfo().getManagedClassNames();
        for (String managedClassName : managedClassNames) {
            metadata.addAnnotatedClassName(managedClassName);
        }

        MetadataImplementor metadataImplementor = (MetadataImplementor) metadata.getMetadataBuilder().build();
        Map<String, Object> jpaPropertyMap = localContainerEntityManagerFactoryBean.getJpaPropertyMap();
        SchemaManagementToolCoordinator.process(metadataImplementor, standardServiceRegistry, jpaPropertyMap, null);

//metadata.addAnnotatedClass(Player.class);
        return null;
    }


    @RequestMapping("/test44")
    public Object test44( ) throws Exception {
        jpaSchemaExtractor2.run(null);
        return null;
    }



    @RequestMapping("/test_asm")
    public Object test_asm() throws Exception {
        //groovy提供了一种将字符串文本代码直接转换成Java Class对象的功能
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        //里面的文本是Java代码,但是我们可以看到这是一个字符串我们可以直接生成对应的Class<?>对象,而不需要我们写一个.java文件
        Class<?> clazz = groovyClassLoader.parseClass("package com.xxl.job.core.glue;\n" +
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
                "} "
        );
        String testEruptName = "testErupt";
        EruptModel eruptModel = EruptCoreService.initEruptModel(clazz);
        EruptCoreService.putErupt(testEruptName,eruptModel);
        EruptCoreService.getErupts().add(eruptModel);

        JsonObject data = new JsonObject();
        createSession(clazz);

        data.addProperty("my_key", "999");
        Gson gson = new Gson();
        Object o = gson.fromJson(data.toString(), eruptModel.getClazz());
        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).addData(eruptModel, o);

        return eruptModel;
    }

    public void createSession(Class _class){
//        Configuration configuration = new Configuration();
//        configuration.addClass(_class);
//        configuration.configure("hibernate.cfg.xml");

//        Properties prop= new Properties();
//
//        prop.setProperty("hibernate.connection.url", "jdbc:mysql://<your-host>:<your-port>/<your-dbname>");
//
//        //You can use any database you want, I had it configured for Postgres
//        prop.setProperty("dialect", "org.hibernate.dialect.PostgresSQL");
//
//        prop.setProperty("hibernate.connection.username", "<your-user>");
//        prop.setProperty("hibernate.connection.password", "<your-password>");
//        prop.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
//        prop.setProperty("show_sql", "true"); //If you wish to see the generated sql query
//
//        SessionFactory sessionFactory = new Configuration().addProperties(prop).buildSessionFactory();
//        Session session = sessionFactory.openSession();
//        SchemaManagementToolCoordinator.process(metadata, this.serviceRegistry, this.properties, (action) -> {
//            this.delayedDropAction = action;
//        });

        SessionFactoryImplementor sessionFactory = emf.unwrap(SessionFactoryImplementor.class);
        ServiceRegistry serviceRegistry = sessionFactory.getServiceRegistry();

    }

}