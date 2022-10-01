package com.example.demo.utils;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;


import javax.persistence.Entity;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 可以使用JPA Entity类生成DDL查询的类
 *  windcoder.com
 * 生成成功，但DIALECT_CLASS获取不友好。
 * 参考：https://gist.github.com/sbcoba/e4264f4b4217746767e682c61f9dc3a6
 */
public class JpaEntityDdlExport {
    /**
     * 要创建的文件名
     */
    private static final String SCHEMA_SQL = "schema_%s.sql";

    /**
     * 域类路径位置（如果范围很宽，则只能找到带有@Entity的类）
     */
    private final static String PATTERN = "classpath*:**/*.class";



    /**
     * 定义DB的DDL
     * org.hibernate.dialect.* 包参考*
     *
     * - Oracle  Oracle10gDialect.class
     * - H2 H2Dialect.class
     * ...
     *
     */
    private final static Class<? extends Dialect> DIALECT_CLASS = MySQL5InnoDBDialect.class;

    public static void main(String[] args) {
        createData(args);
    }


    /**
     * 生成全量SQL脚本
     * @param args
     */
    public static void createData(String[] args){
        Map<String, Object> settings = new HashMap<>();
        settings.put("hibernate.dialect", DIALECT_CLASS);
        settings.put("hibernate.physical_naming_strategy","org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");
        settings.put("hibernate.implicit_naming_strategy","org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        settings.put("hibernate.id.new_generator_mappings", false);

        StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        MetadataSources metadata = new MetadataSources(standardServiceRegistry);
        String pattern = getPattern(args);
        List<Class<?>> classes = getClassesByAnnotation(Entity.class, pattern);
        classes.forEach(metadata::addAnnotatedClass);
        MetadataImplementor metadataImplementor = (MetadataImplementor) metadata.getMetadataBuilder().build();
        SchemaExport schemaExport = new SchemaExport();
        String outputFile = getOutputFilename(args);
        schemaExport.setOutputFile(outputFile);
        schemaExport.setDelimiter(";");
        EnumSet<TargetType> enumSet = EnumSet.of(TargetType.STDOUT);
        schemaExport.create(enumSet, metadataImplementor);
    }


    private static String getPattern(String[] args) {
        String pattern = PATTERN;
        if(args != null && args.length >= 3
                && StringUtils.hasText(args[2])) {
            pattern = args[2];
        }
        return pattern;
    }

    private static void appendMetaData(String outputFile, Map<String, Object> settings) {
        String charsetName = "UTF-8";
        File ddlFile = new File(outputFile);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("/* Generate Environment\n");
            for (Map.Entry<String, Object> entry : settings.entrySet()) {
                sb.append(entry.getKey().toString() + ": " + entry.getValue() + "\n");
            }
            sb.append("*/\n");
            String ddlFileContents = StreamUtils.copyToString(new FileInputStream(ddlFile), Charset.forName(charsetName));
            sb.append(ddlFileContents);
            FileCopyUtils.copy(sb.toString().getBytes(charsetName), ddlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static List<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation, String pattern) {
        return getResources(pattern).stream()
                .map(r -> metadataReader(r))
                .filter(Objects::nonNull)
                .filter(mr -> mr.getAnnotationMetadata().hasAnnotation(annotation.getName()))
                .map(mr -> entityClass(mr))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取与模式对应的资源信息。
     * @param pattern
     * @return
     */
    private static List<Resource> getResources(String pattern) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;
        try {
            resources = resolver.getResources(pattern);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.asList(resources);
    }


    private static Class<?> entityClass(MetadataReader mr) {
        String className = mr.getClassMetadata().getClassName();
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            System.err.printf("%s Class not found", className);
            return null;
        }
        return clazz;
    }

    private static MetadataReader metadataReader(Resource r) {
        MetadataReader mr;
        try {
            mr = new SimpleMetadataReaderFactory().getMetadataReader(r);
        } catch (IOException e) {
            System.err.printf(e.getMessage());
            return null;
        }
        return mr;
    }

    private static String getOutputFilename(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDate = sdf.format(Calendar.getInstance().getTime());
        if(args != null && args.length > 0
                && StringUtils.hasText(args[0])) {
            String customSchemaName = args[0];
            if(customSchemaName.contains("%s")) {
                return String.format(customSchemaName, currentDate);
            }
            return customSchemaName;
        }
        return String.format(SCHEMA_SQL, currentDate);
    }
}