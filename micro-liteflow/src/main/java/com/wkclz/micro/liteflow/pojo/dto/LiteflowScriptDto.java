package com.wkclz.micro.liteflow.pojo.dto;

import com.wkclz.micro.liteflow.pojo.entity.LiteflowScript;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table liteflow_script (liteflow-脚本) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class LiteflowScriptDto extends LiteflowScript {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static LiteflowScriptDto copy(LiteflowScript source) {
        LiteflowScriptDto target = new LiteflowScriptDto();
        LiteflowScript.copy(source, target);
        return target;
    }
}

