package com.example.demo.utils;


import org.hibernate.boot.MetadataSources;

import org.hibernate.boot.registry.StandardServiceRegistry;

import org.hibernate.boot.spi.MetadataImplementor;


import org.hibernate.internal.SessionFactoryImpl;

import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;


import javax.persistence.EntityManagerFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.EnumSet;
import java.util.List;


/**
 * 参考资料：
 * https://stackoverflow.com/questions/34612019/programmatic-schemaexport-schemaupdate-with-hibernate-5-and-spring-4
 */
@Component
public class JPASchemaSchemaUpdate implements ApplicationRunner {
    private static final String SCHEMA_SQL2 = "db/base/update-ddl_2_%s.sql";

    @Autowired
    LocalContainerEntityManagerFactoryBean fb;

    @Autowired
    private EntityManagerFactory emf;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        SessionFactoryImpl nativeEntityManagerFactory = (SessionFactoryImpl)fb.getNativeEntityManagerFactory();
        StandardServiceRegistry serviceRegistry = nativeEntityManagerFactory.getSessionFactoryOptions().getServiceRegistry();
        MetadataSources metadata = new MetadataSources(serviceRegistry);
        List<String> managedClassNames = fb.getPersistenceUnitInfo().getManagedClassNames();
        for (String managedClassName : managedClassNames) {
            metadata.addAnnotatedClassName(managedClassName);
        }
        MetadataImplementor metadataImplementor = (MetadataImplementor) metadata.getMetadataBuilder().build();

        SchemaUpdate schemaUpdate = new SchemaUpdate();

        schemaUpdate.setOutputFile(getOutputFilename());

        schemaUpdate.setDelimiter(";");
        schemaUpdate.setFormat(false);
//
        EnumSet<TargetType> enumSet = EnumSet.of(TargetType.STDOUT);
        schemaUpdate.execute( enumSet , metadataImplementor);

    }


    private static String getOutputFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDate = sdf.format(Calendar.getInstance().getTime());

        return String.format(SCHEMA_SQL2, currentDate);
    }

}