package xyz.erupt.common.utils;

import cn.hutool.core.io.file.PathUtil;

import java.io.File;
import java.net.URL;
import java.util.Objects;

/**
 * @author YuePeng
 * date 2019-07-05.
 */
public class CommonUtil {

    public static<T> boolean isStartupFromJar(Class<T> clazz) {
        File file = new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
        String absolutePath = file.getAbsolutePath();
        return absolutePath.contains(".jar!");
    }

}
