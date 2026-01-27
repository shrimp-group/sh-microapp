package com.wkclz.micro.dict.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_dict_item (字典内容) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmDictItem extends BaseEntity {

    /**
     * 字典类型
     */
    @Desc("字典类型")
    private String dictType;

    /**
     * 字典值
     */
    @Desc("字典值")
    private String dictValue;

    /**
     * 字典标签
     */
    @Desc("字典标签")
    private String dictLabel;

    /**
     * el类型
     */
    @Desc("el类型")
    private String elType;

    /**
     * 描述
     */
    @Desc("描述")
    private String description;

    /**
     * 生效状态
     */
    @Desc("生效状态")
    private Integer enableFlag;


    public static MdmDictItem copy(MdmDictItem source, MdmDictItem target) {
        if (target == null ) { target = new MdmDictItem();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setDictType(source.getDictType());
        target.setDictValue(source.getDictValue());
        target.setDictLabel(source.getDictLabel());
        target.setElType(source.getElType());
        target.setDescription(source.getDescription());
        target.setEnableFlag(source.getEnableFlag());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmDictItem copyIfNotNull(MdmDictItem source, MdmDictItem target) {
        if (target == null ) { target = new MdmDictItem();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getDictType() != null) { target.setDictType(source.getDictType()); }
        if (source.getDictValue() != null) { target.setDictValue(source.getDictValue()); }
        if (source.getDictLabel() != null) { target.setDictLabel(source.getDictLabel()); }
        if (source.getElType() != null) { target.setElType(source.getElType()); }
        if (source.getDescription() != null) { target.setDescription(source.getDescription()); }
        if (source.getEnableFlag() != null) { target.setEnableFlag(source.getEnableFlag()); }
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

