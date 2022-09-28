package com.example.demo.controller;

import com.example.demo.model.jin_gen.NewTest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import groovy.lang.GroovyClassLoader;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.controller.EruptModifyController;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.config.HibernateConfig;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;


@RestController
public class Test2Controller {

    @Autowired
    private EruptCoreService eruptCoreService;

    @Autowired
    private HibernateConfig hibernateConfig;


    @RequestMapping("/test2")
    public Object test2( HttpServletRequest request) throws Exception {
        String erupt = "NewTest";
        JsonObject data = new JsonObject();
        JsonObject jsonObject = new JsonObject();

        data.addProperty("my_key", "999");

        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Gson gson = new Gson();
        Object o = gson.fromJson(data.toString(), eruptModel.getClazz());

        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).addData(eruptModel, o);
        return null;
    }

    private static SessionFactory factory;
    @RequestMapping("/test3")
    public Object test3() throws Exception {
        return null;
    }
    /* Method to CREATE an employee in the database */
    public Integer addEmployee(String fname){
        Session session = factory.openSession();
        Transaction tx = null;
        Integer employeeID = null;
        try{
            tx = session.beginTransaction();
            JsonObject data = new JsonObject();
            JsonObject jsonObject = new JsonObject();
            data.addProperty("my_key", fname);

            Gson gson = new Gson();
            NewTest newTest = gson.fromJson(data.toString(), NewTest.class);

            employeeID = (Integer) session.save(newTest);
            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            session.close();
        }
        return employeeID;
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

        data.addProperty("my_key", "999");
        Gson gson = new Gson();
        Object o = gson.fromJson(data.toString(), eruptModel.getClazz());
        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).addData(eruptModel, o);

        return eruptModel;
    }

    public void createSession(Class _class){
        Date date = new Date();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd");
        String now_time = simpledateformat.format(date);
        String tablename = "TBL_REPORT_STATUS_20050707";
        tablename = "TBL_REPORT_STATUS_" + now_time;
        try {
//            Configuration cfg = new Configuration().addClass(_class).configure();
//            Table table = cfg.getClassMapping(_class).getTable();
//            table.setName(tablename);
//            cfg.getClassMapping(TblReportStatus.class).setTable(table);
//            sessionFactory  = cfg.buildSessionFactory();

            Configuration cfg = new Configuration().addClass(_class).configure();
            cfg.getSqlResultSetMappings();
            SessionFactory factory = cfg.configure().buildSessionFactory();
        }
        catch (MappingException ex) {
            ex.printStackTrace();
        }catch (HibernateException ex) {
            ex.printStackTrace();
        }
    }

}