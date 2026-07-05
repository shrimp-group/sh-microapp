package com.wkclz.micro.material.bean.dto;

import com.wkclz.micro.material.bean.entity.MdmMaterial;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MdmMaterial (素材) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaterialDto extends MdmMaterial {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmMaterialDto copy(MdmMaterial source) {
        MdmMaterialDto target = new MdmMaterialDto();
        BeanUtil.cpAll(source, target);
        return target;
    }
}
