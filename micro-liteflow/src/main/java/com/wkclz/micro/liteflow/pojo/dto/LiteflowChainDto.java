package com.wkclz.micro.liteflow.pojo.dto;

import com.wkclz.micro.liteflow.pojo.entity.LiteflowChain;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table liteflow_chain (liteflow-规则) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class LiteflowChainDto extends LiteflowChain {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static LiteflowChainDto copy(LiteflowChain source) {
        LiteflowChainDto target = new LiteflowChainDto();
        LiteflowChain.copy(source, target);
        return target;
    }
}

