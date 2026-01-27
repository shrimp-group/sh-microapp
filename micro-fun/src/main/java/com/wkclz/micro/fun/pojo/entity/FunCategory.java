package com.wkclz.micro.fun.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_category (函数-分类) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class FunCategory extends BaseEntity {

    /**
     * 父类Code,0为顶级
     */
    @Desc("父类Code,0为顶级")
    private String pcode;

    /**
     * 分类编码
     */
    @Desc("分类编码")
    private String categoryCode;

    /**
     * 分类名称
     */
    @Desc("分类名称")
    private String categoryName;

    /**
     * 描述
     */
    @Desc("描述")
    private String description;

    /**
     * 可见1/0
     */
    @Desc("可见1/0")
    private Integer visible;


    public static FunCategory copy(FunCategory source, FunCategory target) {
        if (target == null ) { target = new FunCategory();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setPcode(source.getPcode());
        target.setCategoryCode(source.getCategoryCode());
        target.setCategoryName(source.getCategoryName());
        target.setDescription(source.getDescription());
        target.setVisible(source.getVisible());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static FunCategory copyIfNotNull(FunCategory source, FunCategory target) {
        if (target == null ) { target = new FunCategory();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getPcode() != null) { target.setPcode(source.getPcode()); }
        if (source.getCategoryCode() != null) { target.setCategoryCode(source.getCategoryCode()); }
        if (source.getCategoryName() != null) { target.setCategoryName(source.getCategoryName()); }
        if (source.getDescription() != null) { target.setDescription(source.getDescription()); }
        if (source.getVisible() != null) { target.setVisible(source.getVisible()); }
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

