package com.example.demo.utils;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
@Component
public class JPASchemaExtractor2 implements ApplicationRunner {
    private static final String SCHEMA_SQL2 = "db/base/new_create-ddl_2_%s.sql";

    @Autowired
    LocalContainerEntityManagerFactoryBean fb;

    @Override
    public void run(ApplicationArguments args) throws Exception {


        StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(fb.getJpaPropertyMap())
                .build();

        MetadataSources metadata = new MetadataSources(standardServiceRegistry);
        List<String> managedClassNames = fb.getPersistenceUnitInfo().getManagedClassNames();
        for (String managedClassName : managedClassNames) {
            metadata.addAnnotatedClassName(managedClassName);
        }

        MetadataImplementor metadataImplementor = (MetadataImplementor) metadata.getMetadataBuilder().build();
        SchemaExport schemaExport = new SchemaExport();
        String outputFile = getOutputFilename();
        schemaExport.setOutputFile(outputFile);
        schemaExport.setDelimiter(";");
        schemaExport.setFormat(false);

        EnumSet<TargetType> enumSet = EnumSet.of(TargetType.SCRIPT);
        schemaExport.create(enumSet, metadataImplementor);

        schemaExport.execute(enumSet, SchemaExport.Action.CREATE , metadataImplementor, standardServiceRegistry);
    }



    private static String getOutputFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDate = sdf.format(Calendar.getInstance().getTime());

        return String.format(SCHEMA_SQL2, currentDate);
    }
}