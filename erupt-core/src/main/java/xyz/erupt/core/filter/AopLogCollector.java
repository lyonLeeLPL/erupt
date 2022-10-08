package xyz.erupt.core.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.LogData;
import com.github.collector.LogCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author EalenXie create on 2020/9/15 13:46
 * 此为样例参考
 * 配置一个简单的日志收集器 这里只是做了一个log.info打印一下，可以在这里写入到数据库中或者写入
 */
@Slf4j
@Component
public class AopLogCollector implements LogCollector {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void collect(LogData logData) {
        try {
            logData.setRespBody(null);
            logData.setArgs(null);
            log.info(objectMapper.writeValueAsString(logData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}