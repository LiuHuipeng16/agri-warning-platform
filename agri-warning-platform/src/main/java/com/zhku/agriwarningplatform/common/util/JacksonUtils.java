package com.zhku.agriwarningplatform.common.util;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-11
 * Time: 9:32
 */
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.json.JsonParseException;

import java.util.List;
import java.util.concurrent.Callable;


public class JacksonUtils {
    private JacksonUtils() {

    }

    /**
     * 单例
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    private static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    private static  <T> T tryParse(Callable<T> parser) {
        return tryParse(parser, JacksonException.class);
    }

    private static  <T> T tryParse(Callable<T> parser, Class<? extends Exception> check) {
        try {
            return parser.call();
        } catch (Exception var4) {
            if (check.isAssignableFrom(var4.getClass())) {
                throw new JsonParseException(var4);
            }

            throw new IllegalStateException(var4);
        }
    }

    /**
     * 序列化方法
     *
     * @param object
     * @return
     */
    public static String writeValueAsString(Object object) {
        return JacksonUtils.tryParse(() -> {
            return JacksonUtils.getObjectMapper().writeValueAsString(object);
        });
    }

    /**
     * 反序列化
     *
     * @param content
     * @param valueType
     * @return
     * @param <T>
     */
    public static <T> T readValue(String content, Class<T> valueType) {
        return JacksonUtils.tryParse(() -> {
            return JacksonUtils.getObjectMapper().readValue(content, valueType);
        });

    }

    /**
     * 反序列化 List
     *
     * @param content
     * @param paramClasses
     * @return
     * @param <T>
     */
    public static <T> T readListValue(String content, Class<?> paramClasses) {
        JavaType javaType = JacksonUtils.getObjectMapper().getTypeFactory()
                .constructParametricType(List.class, paramClasses);
        return JacksonUtils.tryParse(() -> {
            return JacksonUtils.getObjectMapper().readValue(content, javaType);
        });
    }

}
