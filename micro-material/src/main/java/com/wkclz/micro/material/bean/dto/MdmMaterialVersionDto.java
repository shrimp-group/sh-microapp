package com.wkclz.micro.material.bean.dto;

import com.wkclz.micro.material.bean.entity.MdmMaterialVersion;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MdmMaterialVersion (素材版本) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaterialVersionDto extends MdmMaterialVersion {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmMaterialVersionDto copy(MdmMaterialVersion source) {
        MdmMaterialVersionDto target = new MdmMaterialVersionDto();
        BeanUtil.cpAll(source, target);
        return target;
    }
}
