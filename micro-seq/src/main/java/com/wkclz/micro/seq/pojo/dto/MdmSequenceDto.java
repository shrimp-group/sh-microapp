package com.wkclz.micro.seq.pojo.dto;

import com.wkclz.micro.seq.pojo.entity.MdmSequence;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_sequence (序列生成) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmSequenceDto extends MdmSequence {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmSequenceDto copy(MdmSequence source) {
        MdmSequenceDto target = new MdmSequenceDto();
        MdmSequence.copy(source, target);
        return target;
    }
}

