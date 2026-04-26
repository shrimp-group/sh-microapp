package com.wkclz.micro.dict.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_dict (字典) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmDict extends BaseEntity {

    /**
     * 字典分类
     */
    @FieldDesc("字典分类")
    private String dictCtg;

    /**
     * 字典类型
     */
    @FieldDesc("字典类型")
    private String dictType;

    /**
     * 类型描述信息
     */
    @FieldDesc("类型描述信息")
    private String description;


    public static MdmDict copy(MdmDict source, MdmDict target) {
        if (target == null ) { target = new MdmDict();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setDictCtg(source.getDictCtg());
        target.setDictType(source.getDictType());
        target.setDescription(source.getDescription());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmDict copyIfNotNull(MdmDict source, MdmDict target) {
        if (target == null ) { target = new MdmDict();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getDictCtg() != null) { target.setDictCtg(source.getDictCtg()); }
        if (source.getDictType() != null) { target.setDictType(source.getDictType()); }
        if (source.getDescription() != null) { target.setDescription(source.getDescription()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }

}

