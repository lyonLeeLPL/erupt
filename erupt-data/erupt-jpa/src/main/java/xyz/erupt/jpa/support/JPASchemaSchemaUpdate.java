package xyz.erupt.jpa.support;


import net.openhft.compiler.CachedCompiler;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumSet;

@Component
public class JPASchemaSchemaUpdate  {
    private static final String SCHEMA_SQL2 = "db/base/update-ddl_2_%s.sql";

    private final CachedCompiler cc = new CachedCompiler(null, null);

    public Class runForJavaCode(String className, String code, MetadataSources metadata) throws Exception {
        code = recoverFromFM(code);
        Class aClass = cc.loadFromJava(className, code);
        MetadataImplementor metadataImplementor = runForClass(aClass, metadata);
        return aClass;
    }

    private String recoverFromFM(String code) {
        code = code.replaceAll("&lt;","<");
        code = code.replaceAll("&gt;",">");

        return code;
    }

    public MetadataImplementor runForClass(Class aClass, MetadataSources metadata) {

        MetadataImplementor metadataImplementor = (MetadataImplementor) metadata.getMetadataBuilder().build();

        SchemaUpdate schemaUpdate = new SchemaUpdate();

        schemaUpdate.setOutputFile(getOutputFilename());
        schemaUpdate.setDelimiter(";");
        schemaUpdate.setFormat(false);
//
        EnumSet<TargetType> enumSet = EnumSet.of(TargetType.DATABASE);
        schemaUpdate.execute( enumSet , metadataImplementor);

        return metadataImplementor;
    }

    private static String getOutputFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDate = sdf.format(Calendar.getInstance().getTime());

        return String.format(SCHEMA_SQL2, currentDate);
    }

}