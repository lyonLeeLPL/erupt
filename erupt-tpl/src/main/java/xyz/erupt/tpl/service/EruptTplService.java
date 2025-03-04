package xyz.erupt.tpl.service;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.StringBuilderWriter;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.*;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;
import xyz.erupt.tpl.engine.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2020-02-24
 */
@Order
@Service
@Slf4j
public class EruptTplService {

    public static String TPL = "tpl";

    private static final Map<Tpl.Engine, EngineTemplate<Object>> tplEngines = new HashMap<>();

    private static final Class<?>[] engineTemplates = {
            NativeEngine.class,
            FreemarkerEngine.class,
            ThymeleafEngine.class,
            VelocityTplEngine.class,
            BeetlEngine.class,
            EnjoyEngine.class
    };

    static {
        for (Class<?> tpl : engineTemplates) {
            try {
                EngineTemplate<Object> engineTemplate = (EngineTemplate) tpl.newInstance();
                engineTemplate.setEngine(engineTemplate.init());
                tplEngines.put(engineTemplate.engine(), engineTemplate);
            } catch (NoClassDefFoundError ignored) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Map<String, Method> tplActions = new LinkedCaseInsensitiveMap<>();

    private final Map<String, Method> tplMatcherActions = new LinkedCaseInsensitiveMap<>();

    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    public static Map<Tpl.Engine, EngineTemplate<Object>> getTplEngines() {
        return tplEngines;
    }

    public void run() {
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(),
                new TypeFilter[]{new AnnotationTypeFilter(EruptTpl.class)},
                this::registerTpl);
    }

    //注册模板
    public void registerTpl(Class<?> tplClass) {
        Arrays.stream(tplClass.getDeclaredMethods()).forEach(method -> Optional.ofNullable(method.getAnnotation(TplAction.class))
                .ifPresent(it -> {
                            if (pathMatcher.isPattern(it.value())) {
                                tplMatcherActions.put(it.value(), method);
                            } else {
                                tplActions.put(it.value(), method);
                            }
                        }
                ));
    }

    //移除模板
    public void unregisterTpl(Class<?> tplClass) {
        Arrays.stream(tplClass.getDeclaredMethods()).forEach(
                method -> Optional.ofNullable(method.getAnnotation(TplAction.class)).ifPresent(
                        it -> {
                            tplActions.remove(it.value());
                            tplMatcherActions.remove(it.value());
                        }
                )
        );
    }

    public Method getAction(String path) {
        if (tplActions.containsKey(path)) {
            return tplActions.get(path);
        } else {
            // 从模糊匹配中查询资源路径
            for (Map.Entry<String, Method> entry : tplMatcherActions.entrySet()) {
                if (pathMatcher.match(entry.getKey(), path)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public Object getEngine(Tpl.Engine engine) {
        return tplEngines.get(engine).getEngine();
    }

    @SneakyThrows
    public void tplRender(Tpl tpl, Map<String, Object> map, HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        this.tplRender(tpl, map, response.getWriter());
    }

    public void tplRender(Tpl tpl, Map<String, Object> map, Writer writer) {
        if (!tpl.tplHandler().isInterface()) {
            Tpl.TplHandler tplHandler = EruptSpringUtil.getBean(tpl.tplHandler());
            tplHandler.bindTplData(Optional.ofNullable(map).orElse(new HashMap<>()), tpl.params());
        }
        this.tplRender(tpl.engine(), tpl.path(), map, writer);
    }

    @SneakyThrows
    public void tplRender(Tpl.Engine engine, String path, Map<String, Object> map, Writer writer) {
        map = Optional.ofNullable(map).orElse(new HashMap<>());
        map.put(EngineConst.INJECT_REQUEST, request);
        map.put(EngineConst.INJECT_RESPONSE, response);
        map.put(EngineConst.INJECT_BASE, request.getContextPath());
        EngineTemplate<Object> engineAbstractTemplate = tplEngines.get(engine);
        Assert.notNull(engineAbstractTemplate, engine.name() + " jar not found");
        engineAbstractTemplate.render(engineAbstractTemplate.getEngine(), path, map, writer);
    }

    /**
     *
     * @param engine
     * @param path
     * @param map
     * @return
     */
    @SneakyThrows
    public String tplRender2Str(Tpl.Engine engine, String path, Map<String, Object> map) {
        map = Optional.ofNullable(map).orElse(new HashMap<>());
        map.put(EngineConst.INJECT_BASE, request.getContextPath());
        EngineTemplate<Object> engineAbstractTemplate = tplEngines.get(engine);
        Assert.notNull(engineAbstractTemplate, engine.name() + " jar not found");

        StringBuilderWriter stringBuilderWriter = new StringBuilderWriter();
        engineAbstractTemplate.render(engineAbstractTemplate.getEngine(), path, map, stringBuilderWriter);
        stringBuilderWriter.close();

        return stringBuilderWriter.toString();
    }

}
