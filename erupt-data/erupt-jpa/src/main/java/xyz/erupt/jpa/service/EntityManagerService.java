package xyz.erupt.jpa.service;

import cn.hutool.core.util.ReflectUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.persister.spi.PersisterFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.core.annotation.EruptDataSource;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.prop.EruptPropDb;
import xyz.erupt.core.util.EruptSpringUtil;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.Metamodel;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author YuePeng
 * date 2020-01-13
 */
@Service
public class EntityManagerService implements DisposableBean {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private EruptProp eruptProp;

    @Autowired
    LocalContainerEntityManagerFactoryBean fb;

    private final Map<String, EntityManagerFactory> entityManagerFactoryMap = new HashMap<>();

    private final List<EntityManager> extEntityManagers = new ArrayList<>();

    private synchronized EntityManagerFactory getEntityManagerFactory(String dbName) {
        if (entityManagerFactoryMap.containsKey(dbName)) return entityManagerFactoryMap.get(dbName);
        for (EruptPropDb prop : eruptProp.getDbs()) {
            if (dbName.equals(prop.getDatasource().getName())) {
                Objects.requireNonNull(prop.getDatasource().getName(), "dbs configuration Must specify name → dbs.datasource.name");
                Objects.requireNonNull(prop.getScanPackages(), String.format("%s DataSource not found 'scanPackages' configuration",
                        prop.getDatasource().getName()));
                LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
                {
                    JpaProperties jpa = prop.getJpa();
                    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
                    vendorAdapter.setGenerateDdl(jpa.isGenerateDdl());
                    vendorAdapter.setDatabase(jpa.getDatabase());
                    vendorAdapter.setShowSql(jpa.isShowSql());
                    vendorAdapter.setDatabasePlatform(jpa.getDatabasePlatform());
                    factory.setJpaVendorAdapter(vendorAdapter);
                    Properties properties = new Properties();
                    properties.putAll(jpa.getProperties());
                    factory.setJpaProperties(properties);
                }
                {
                    HikariConfig hikariConfig = prop.getDatasource().getHikari().toHikariConfig();
                    Optional.ofNullable(prop.getDatasource().getUrl()).ifPresent(hikariConfig::setJdbcUrl);
                    Optional.ofNullable(prop.getDatasource().getDriverClassName()).ifPresent(hikariConfig::setDriverClassName);
                    Optional.ofNullable(prop.getDatasource().getUsername()).ifPresent(hikariConfig::setUsername);
                    Optional.ofNullable(prop.getDatasource().getPassword()).ifPresent(hikariConfig::setPassword);
                    Optional.ofNullable(prop.getDatasource().getHikari().getPoolName()).ifPresent(hikariConfig::setPoolName);
                    factory.setDataSource(new HikariDataSource(hikariConfig));
                    factory.setPackagesToScan(prop.getScanPackages());
                    factory.afterPropertiesSet();
                }
                entityManagerFactoryMap.put(prop.getDatasource().getName(), factory.getObject());
                return factory.getObject();
            }
        }
        throw new RuntimeException("Failed to match data source '" + dbName + "'");
    }

    //注册扩展的EntityManager
    public void addExtEntityManager(EntityManager entityManager) {
        if (!extEntityManagers.contains(entityManager)) {
            extEntityManagers.add(entityManager);
        }
    }

    //移除扩展的EntityManager
    public void removeExtEntityManager(EntityManager entityManager) {
        extEntityManagers.remove(entityManager);
    }

    private EntityManager getExtEntityManager(Class<?> eruptClass) {
        if (extEntityManagers.isEmpty()) return null;
        return extEntityManagers.stream().filter(em -> {
            try {
                return em.getMetamodel().entity(eruptClass) != null;
            } catch (Exception e) {
                return false;
            }
        }).findFirst().orElse(null);
    }

    public <R> R getEntityManager(Class<?> eruptClass, Function<EntityManager, R> function) {
        EntityManager extEntityManager = getExtEntityManager(eruptClass);
        if (null != extEntityManager) return function.apply(extEntityManager);
        EruptDataSource eruptDataSource = eruptClass.getAnnotation(EruptDataSource.class);
        if (null == eruptDataSource) return function.apply(entityManager);
        EntityManager em = this.getEntityManagerFactory(eruptDataSource.value()).createEntityManager();
        try {
            return function.apply(em);
        } finally {
            if (em.isOpen()) em.close();
        }
    }


    public void entityManagerTran(Class<?> eruptClass, Consumer<EntityManager> consumer) {
        EntityManager extEntityManager = getExtEntityManager(eruptClass);
        if (null != extEntityManager) {
            consumer.accept(extEntityManager);
            return;
        }
        EruptDataSource eruptDataSource = eruptClass.getAnnotation(EruptDataSource.class);
        if (null == eruptDataSource) {
            consumer.accept(entityManager);
            return;
        }
        EntityManager em = this.getEntityManagerFactory(eruptDataSource.value()).createEntityManager();
        try {
            em.getTransaction().begin();
            consumer.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Comment("必须手动执行 close() 方法")
    public EntityManager findEntityManager(String name) {
        return this.getEntityManagerFactory(name).createEntityManager();
    }


    @Override
    public void destroy() throws Exception {
        for (EntityManagerFactory value : entityManagerFactoryMap.values()) {
            value.close();
        }
        extEntityManagers.clear();
    }

    public void entityRegisterInJpa(Class aclass)  {
        Metamodel metamodel = entityManager.getMetamodel();

        SessionFactoryImpl nativeEntityManagerFactory = (SessionFactoryImpl)fb.getNativeEntityManagerFactory();
        StandardServiceRegistry serviceRegistry = nativeEntityManagerFactory.getSessionFactoryOptions().getServiceRegistry();
        IdentifierGenerator coupon = nativeEntityManagerFactory.getIdentifierGenerator("com.example.demo.model.mall.Coupon");

        final PersisterFactory persisterFactory = nativeEntityManagerFactory.getServiceRegistry().getService( PersisterFactory.class );

//        final PersisterCreationContext persisterCreationContext = new PersisterCreationContext() {
//            @Override
//            public SessionFactoryImplementor getSessionFactory() {
//                return nativeEntityManagerFactory;
//            }
//
//            @Override
//            public MetadataImplementor getMetadata() {
//                return mappingMetadata;
//            }
//        };
//        final NavigableRole rootEntityRole = new NavigableRole( model.getRootClass().getEntityName() );
//        final EntityDataAccess accessStrategy = sessionFactory.getCache().getEntityRegionAccess( rootEntityRole );
//        final NaturalIdDataAccess naturalIdAccessStrategy = sessionFactory.getCache().getNaturalIdCacheRegionAccessStrategy( rootEntityRole );
//
//        final EntityPersister cp = persisterFactory.createEntityPersister(
//                model,
//                accessStrategy,
//                naturalIdAccessStrategy,
//                persisterCreationContext
//        );
//        entityPersisterMap.put( aclass.getName(), cp );



//        nativeEntityManagerFactory.meta
//        final MetadataImplementor metadata,
//
//        metadata.getEntityBindings().stream().filter( model -> !model.isInherited() ).forEach( model -> {
//            IdentifierGenerator generator = model.getIdentifier().createIdentifierGenerator(
//                    metadata.getIdentifierGeneratorFactory(),
//                    jdbcServices.getJdbcEnvironment().getDialect(),
//                    (RootClass) model
//            );
//            generator.initialize( sqlStringGenerationContext );
//            identifierGenerators.put( model.getEntityName(), generator );
//        } );
//        metadata.validate();
        System.out.println(coupon);

    }

}
