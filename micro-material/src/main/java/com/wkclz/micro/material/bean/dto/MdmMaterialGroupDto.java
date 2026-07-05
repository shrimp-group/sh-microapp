package com.wkclz.micro.material.bean.dto;

import com.wkclz.micro.material.bean.entity.MdmMaterialGroup;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MdmMaterialGroup (素材分组) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaterialGroupDto extends MdmMaterialGroup {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmMaterialGroupDto copy(MdmMaterialGroup source) {
        MdmMaterialGroupDto target = new MdmMaterialGroupDto();
        BeanUtil.cpAll(source, target);
        return target;
    }
}
