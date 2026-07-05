package com.wkclz.micro.material.bean.dto;

import com.wkclz.micro.material.bean.entity.MdmMaterialRef;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MdmMaterialRef (素材引用) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaterialRefDto extends MdmMaterialRef {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmMaterialRefDto copy(MdmMaterialRef source) {
        MdmMaterialRefDto target = new MdmMaterialRefDto();
        BeanUtil.cpAll(source, target);
        return target;
    }
}
