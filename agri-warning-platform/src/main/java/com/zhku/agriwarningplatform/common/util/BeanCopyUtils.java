package com.zhku.agriwarningplatform.common.util;

import cn.hutool.core.bean.BeanUtil;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 12:01
 */
public class BeanCopyUtils {
    /**
     * 不同层之间的对象转换
     * List需调用copyList,Enum需要手动转换
     * @param source
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> T copy(Object source, Class<T> clazz){
        if(source == null){
            return null;
        }
        return BeanUtil.copyProperties(source, clazz);
    }

    public static <T> List<T> copyList(List<?> source, Class<T> clazz){
        if(source == null){
            return List.of();
        }
        return source.stream()
                .map(e -> BeanUtil.copyProperties(e, clazz))
                .toList();
    }

}
