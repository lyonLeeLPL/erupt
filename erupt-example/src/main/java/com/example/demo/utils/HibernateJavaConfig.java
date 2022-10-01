package com.example.demo.utils;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;

import javax.persistence.spi.PersistenceUnitInfo;
import java.util.List;
import java.util.Map;

@Configuration
@EntityScan("com.windcoder.qycms.*")
@AutoConfigureAfter({HibernateJpaAutoConfiguration.class})
public class HibernateJavaConfig {

//    /**
//    *  生成元数据Metadata
//    *  在这里将要解析的类加在进Metadata中
//    * @param standardServiceRegistry
//    * @param persistenceUnitInfo
//    * @return
//    */
//    @ConditionalOnMissingBean({Metadata.class})
//    @Bean
//    public Metadata getMetadata(StandardServiceRegistry standardServiceRegistry,
//                                PersistenceUnitInfo persistenceUnitInfo) {
//        MetadataSources metadataSources = new MetadataSources(standardServiceRegistry);
//
//        List<String> managedClassNames = persistenceUnitInfo.getManagedClassNames();
//        for (String managedClassName : managedClassNames) {
//            metadataSources.addAnnotatedClassName(managedClassName);
//        }
//
//        Metadata metadata = metadataSources.buildMetadata();
//        return metadata;
//    }
    /**
    *   该实例将配置信息合并到一组工作服务
    * @param jpaProperties
    * @return
    */
    @ConditionalOnMissingBean({StandardServiceRegistry.class})
    @Bean
    public StandardServiceRegistry getStandardServiceRegistry(JpaProperties jpaProperties) {
        StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder();
        Map<String, String> properties = jpaProperties.getProperties();
        ssrb.applySettings(properties);

        StandardServiceRegistry ssr = ssrb.build();

        return ssr;
    }
    /**
     * PersistenceUnitInfo接口由容器实现并由创建一个javax.persistence.EntityManagerFactory时的persistence提供者使用，
     * 这里用于生成PersistenceUnitInfo的Been,用于代替persistence.xml
     * @param entityScanPackages
     * @return
     */
    @ConditionalOnMissingBean({PersistenceUnitInfo.class})
    @Bean
    public PersistenceUnitInfo getPersistenceUnitInfo(EntityScanPackages entityScanPackages) {
        List<String> packagesToScan = entityScanPackages.getPackageNames();

        DefaultPersistenceUnitManager persistenceUnitManager = new DefaultPersistenceUnitManager();

        String[] packagesToScanArr = (String[]) packagesToScan.toArray(new String[packagesToScan.size()]);
        persistenceUnitManager.setPackagesToScan(packagesToScanArr);
        persistenceUnitManager.afterPropertiesSet();

        PersistenceUnitInfo persistenceUnitInfo = persistenceUnitManager.obtainDefaultPersistenceUnitInfo();
        return persistenceUnitInfo;
    }
}