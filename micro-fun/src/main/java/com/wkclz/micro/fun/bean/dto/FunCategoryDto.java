package com.wkclz.micro.fun.bean.dto;

import com.wkclz.micro.fun.bean.entity.FunCategory;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_category (函数-分类) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class FunCategoryDto extends FunCategory {



    private List<FunCategoryDto> children;



    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static FunCategoryDto copy(FunCategory source) {
        FunCategoryDto target = new FunCategoryDto();
        BeanUtil.cpAll(source, target);
        return target;
    }
}

