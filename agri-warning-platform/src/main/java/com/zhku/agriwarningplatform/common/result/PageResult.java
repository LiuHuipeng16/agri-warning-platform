package com.zhku.agriwarningplatform.common.result;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 11:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private Integer total;
    private List<T> records;
}
