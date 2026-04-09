package com.zhku.agriwarningplatform.common.result;

import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 11:07
 */
@Data
public class PageResult<T> {
    private int total;
    private List<T> records;
}
