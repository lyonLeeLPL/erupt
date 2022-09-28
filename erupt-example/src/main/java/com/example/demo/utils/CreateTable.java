package com.example.demo.utils;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lingsf on 2019/11/5.
 */
public class CreateTable {
    public static Map<String, String> javaProperty2SqlColumnMap = new HashMap<>();

	//下边是对应的oracle的生成语句，类型都是oracle，如果是mysql还需要改。
    static {
        javaProperty2SqlColumnMap.put("Integer", "NUMBER(9)");
        javaProperty2SqlColumnMap.put("Short", "NUMBER(4)");
        javaProperty2SqlColumnMap.put("Long", "NUMBER(18)");
        javaProperty2SqlColumnMap.put("BigDecimal", "NUMBER(22,2)");
        javaProperty2SqlColumnMap.put("Double", "NUMBER(22,2)");
        javaProperty2SqlColumnMap.put("Float", "NUMBER(22,2)");
        javaProperty2SqlColumnMap.put("Boolean", "NUMBER(1)");
        javaProperty2SqlColumnMap.put("Timestamp", "date");
        javaProperty2SqlColumnMap.put("String", "VARCHAR(255)");
    }
    
    public static String createTable(Class<?> clz, String tableName) throws IOException {
        // 判断类上是否有次注解
        boolean clzHasAnno = clz.isAnnotationPresent(Table.class);
        String prikey = null;
        if (clzHasAnno) {
            // 获取类上的注解
            Table annotation = (Table)clz.getAnnotation(Table.class);
            // 输出注解上的类名
            String tableNameAnno = annotation.name();
            if(tableNameAnno != null && !"".equals(tableNameAnno)){
                tableName = tableNameAnno;
            }else{
                throw new RuntimeException("没有类名");
            }
            String keyIdAnno = annotation.schema();
            if(keyIdAnno != null && !"".equals(keyIdAnno)){
                prikey = keyIdAnno;
            }else{
                throw new RuntimeException("没有设置主键");
            }
        }
        Field[] fields = null;
        fields = clz.getDeclaredFields();
        String param = null;
        String column = null;
        StringBuilder sb = null;
        sb = new StringBuilder(50);
        sb.append("create table ").append(tableName).append(" ( \r\n");
        boolean firstId = true;
        File file = null;
        for (Field f : fields) {
            column = f.getName();
            if (column.equals("serialVersionUID")) {
                continue;
            }
            boolean fieldHasAnno = f.isAnnotationPresent(Column.class);
            if(fieldHasAnno){
                Column fieldAnno = f.getAnnotation(Column.class);
                //输出注解属性
                String  name = fieldAnno.name();
                if(name != null && !"".equals(name)){
                    column = name;
                }
            }else{
                continue; //没有column注解的过滤掉
            }

            param = f.getType().getSimpleName();
            sb.append(column);//一般第一个是主键
            sb.append(" ").append(javaProperty2SqlColumnMap.get(param)).append(" ");
            if(prikey == null || "".equals(prikey)){
                if (firstId) {//类型转换
                    sb.append(" PRIMARY KEY ");
                    firstId = false;
                }
            }else{
                if(prikey.equals(column.toLowerCase())){
                    sb.append(" PRIMARY KEY ");
                }
            }
            sb.append(",\n ");
        }
        String sql = null;
        sql = sb.toString();
        //去掉最后一个逗号
        int lastIndex = sql.lastIndexOf(",");
        sql = sql.substring(0, lastIndex) + sql.substring(lastIndex + 1);

        sql = sql.substring(0, sql.length() - 1) + " );\r\n";
        System.out.println("sql :");
        System.out.println(sql);
        return sql;
    }
}
