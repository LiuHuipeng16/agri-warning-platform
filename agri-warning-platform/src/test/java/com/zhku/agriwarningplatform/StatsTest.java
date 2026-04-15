package com.zhku.agriwarningplatform;

import com.zhku.agriwarningplatform.module.stats.mapper.StatsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static reactor.core.publisher.Mono.when;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-15
 * Time: 11:12
 */
@SpringBootTest
public class StatsTest {
    @Autowired
    StatsMapper statsMapper;
    @Test
    public void test(){
        when(statsMapper.getDashboardStats()).thenThrow(new RuntimeException("db error"));
    }
}
