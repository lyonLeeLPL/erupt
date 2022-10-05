package xyz.erupt.jpa.support;


import net.openhft.compiler.CachedCompiler;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumSet;

@Component
public class JPASchemaSchemaUpdate  {
    private static final String SCHEMA_SQL2 = "db/base/update-ddl_2_%s.sql";

    private final CachedCompiler cc = new CachedCompiler(null, null);
    @Autowired
    LocalContainerEntityManagerFactoryBean fb;

    public Class runForJavaCode(String className, String code) throws Exception {
        Class aClass = cc.loadFromJava(className, code);
        runForClass(aClass);
        return aClass;
    }

    public void runForClass(Class aClass) {
        SessionFactoryImpl nativeEntityManagerFactory = (SessionFactoryImpl)fb.getNativeEntityManagerFactory();
        StandardServiceRegistry serviceRegistry = nativeEntityManagerFactory.getSessionFactoryOptions().getServiceRegistry();
        MetadataSources metadata = new MetadataSources(serviceRegistry);
        metadata.addAnnotatedClass(aClass);

        MetadataImplementor metadataImplementor = (MetadataImplementor) metadata.getMetadataBuilder().build();

        SchemaExport schemaExport = new SchemaExport();
        String outputFile = getOutputFilename();
        schemaExport.setOutputFile(outputFile);
        schemaExport.setDelimiter(";");
        schemaExport.setFormat(false);

        EnumSet<TargetType> enumSet = EnumSet.of(TargetType.DATABASE);
        // 重新构建 数据库，把之前的数据备份然后往回塞。
        schemaExport.create(enumSet, metadataImplementor);
    }

    private static String getOutputFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDate = sdf.format(Calendar.getInstance().getTime());

        return String.format(SCHEMA_SQL2, currentDate);
    }

}