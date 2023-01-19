package xyz.erupt.generator.handler;

import cn.hutool.core.util.RandomUtil;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.common.constant.CommonConst;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.generator.base.GeneratorType;
import xyz.erupt.generator.model.GeneratorClass;
import xyz.erupt.jpa.service.EntityManagerService;
import xyz.erupt.jpa.support.JPASchemaSchemaUpdate;
import xyz.erupt.tpl.service.EruptTplService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.Metamodel;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//泛型说明
//EruptTest 为目标数据的类型
//Void erupt支持使用另一个erupt类作为表单输入框而存在，因为此演示代码并未涉及，所以使用Void来占位
@Component(value = "generatorOperationHandlerImpl")
public class OperationHandlerImpl implements OperationHandler<GeneratorClass, Void> {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    LocalContainerEntityManagerFactoryBean fb;


    //返回值为事件触发执行函数
    @Override
    @SneakyThrows
    public String exec(List<GeneratorClass> data, Void vo, String[] param) {
        Map<String, Object> map = new HashMap<>();
        // get my source
        SessionFactoryImpl nativeEntityManagerFactory = (SessionFactoryImpl)fb.getNativeEntityManagerFactory();
        StandardServiceRegistry serviceRegistry = nativeEntityManagerFactory.getSessionFactoryOptions().getServiceRegistry();
        MetadataSources metadata = new MetadataSources(serviceRegistry);

        List<String> managedClassNames = fb.getPersistenceUnitInfo().getManagedClassNames();
        for (String managedClassName : managedClassNames) {
            metadata.addAnnotatedClassName(managedClassName);
        }

        //
        GeneratorClass generatorClass = data.get(0);
        String newClassName = generatorClass.getClassName() + CommonConst.SPECIAL_SPLIT_SYMBOL + RandomUtil.randomString(6);
        TemplateHashModel staticModels = BeansWrapper.getDefaultInstance().getStaticModels();
        TemplateHashModel fileStatics = (TemplateHashModel) staticModels.get(GeneratorType.class.getName());
        map.put("rows", data);
        map.put("newClassName", newClassName);
        map.put(GeneratorType.class.getSimpleName(), fileStatics);

        EruptTplService eruptTplService = EruptSpringUtil.getBean(EruptTplService.class);
        String code = eruptTplService.tplRender2Str(Tpl.Engine.FreeMarker, "generator/erupt-code-hot-load.java", map);
        if (StringUtils.isNotBlank(generatorClass.getSourceCode()) &&  generatorClass.getSourceCode().length() > 100 ){
            code = generatorClass.getSourceCode();
            String[] s = newClassName.split("_");

            String origin = s[0];
            // 处理，替换
            code= code.replace(origin,newClassName);
        }
        // 更新创建数据库
        JPASchemaSchemaUpdate jpaSchemaSchemaUpdate = EruptSpringUtil.getBean(JPASchemaSchemaUpdate.class);
        Class aClass = jpaSchemaSchemaUpdate.runForJavaCode(newClassName, code,metadata);

        EruptModel eruptModel = EruptCoreService.initEruptModel(aClass);
        EruptCoreService.putErupt(eruptModel.getEruptName(),eruptModel);
        EruptCoreService.getErupts().add(eruptModel);

        //类 动态更新。

        // 类 key value ， value
        // 往 entity manager注册
        EntityManagerService entityManagerService = EruptSpringUtil.getBean(EntityManagerService.class);
        entityManagerService.entityRegisterInJpa(aClass, eruptModel.getEruptName() ,metadata);
//        runForClass(metadata);
        // 更新
        return "this.msg.success('同步成功')";
        // return "this.msg.info('提示信息')"
        // return "this.msg.error('错误信息')"
        // return "this.msg.success('成功信息')";
    }

    public MetadataImplementor runForClass(MetadataSources metadata) {

        MetadataImplementor metadataImplementor = (MetadataImplementor) metadata.getMetadataBuilder().build();

//        SchemaUpdate schemaUpdate = new SchemaUpdate();
//
//        schemaUpdate.setOutputFile(getOutputFilename());
//        schemaUpdate.setDelimiter(";");
//        schemaUpdate.setFormat(false);
////
//        EnumSet<TargetType> enumSet = EnumSet.of(TargetType.DATABASE);
//        schemaUpdate.execute( enumSet , metadataImplementor);//更新数据库，不创建表

        SchemaExport schemaExport = new SchemaExport();
        EnumSet<TargetType> enumSet2 = EnumSet.of(TargetType.DATABASE);
        // 重新构建 数据库，把之前的数据备份然后往回塞。
        schemaExport.createOnly(enumSet2, metadataImplementor);//创建表
        return metadataImplementor;
    }


}
