package xyz.erupt.generator.handler;

import cn.hutool.core.util.ReflectUtil;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import lombok.SneakyThrows;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.generator.base.GeneratorType;
import xyz.erupt.generator.model.GeneratorClass;
import xyz.erupt.jpa.dao.EruptJpaDao;
import xyz.erupt.jpa.service.EntityManagerService;
import xyz.erupt.jpa.support.JPASchemaSchemaUpdate;
import xyz.erupt.tpl.service.EruptTplService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.Metamodel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//泛型说明
//EruptTest 为目标数据的类型
//Void erupt支持使用另一个erupt类作为表单输入框而存在，因为此演示代码并未涉及，所以使用Void来占位
public class OperationHandlerImpl implements OperationHandler<GeneratorClass, Void> {

    //返回值为事件触发执行函数
    @Override
    @SneakyThrows
    public String exec(List<GeneratorClass> data, Void vo, String[] param) {
        Map<String, Object> map = new HashMap<>();

        GeneratorClass generatorClass = data.get(0);
        TemplateHashModel staticModels = BeansWrapper.getDefaultInstance().getStaticModels();
        TemplateHashModel fileStatics = (TemplateHashModel) staticModels.get(GeneratorType.class.getName());
        map.put("rows", data);
        map.put(GeneratorType.class.getSimpleName(), fileStatics);

        EruptTplService eruptTplService = EruptSpringUtil.getBean(EruptTplService.class);
        String code = eruptTplService.tplRender2Str(Tpl.Engine.FreeMarker, "generator/erupt-code.java", map);

        JPASchemaSchemaUpdate jpaSchemaSchemaUpdate = EruptSpringUtil.getBean(JPASchemaSchemaUpdate.class);
        Class aClass = jpaSchemaSchemaUpdate.runForJavaCode(generatorClass.getClassName(), code);

        EruptModel eruptModel = EruptCoreService.initEruptModel(aClass);
        EruptCoreService.putErupt(generatorClass.getClassName(),eruptModel);
        EruptCoreService.getErupts().add(eruptModel);

        // 往 entity manager注册
        EntityManagerService entityManagerService = EruptSpringUtil.getBean(EntityManagerService.class);
        entityManagerService.entityRegisterInJpa(aClass);

        return "this.msg.success('同步成功')";
        // return "this.msg.info('提示信息')"
        // return "this.msg.error('错误信息')"
        // return "this.msg.success('成功信息')";
    }

}
