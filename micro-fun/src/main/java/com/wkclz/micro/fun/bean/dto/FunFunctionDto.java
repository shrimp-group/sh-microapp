package com.wkclz.micro.fun.bean.dto;

import com.wkclz.micro.fun.bean.entity.FunFunction;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_function (函数-函数体) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class FunFunctionDto extends FunFunction {


    private String param;


    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static FunFunctionDto copy(FunFunction source) {
        FunFunctionDto target = new FunFunctionDto();
        BeanUtil.cpAll(source, target);
        return target;
    }
}

