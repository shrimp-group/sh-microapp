package com.wkclz.micro.fun.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_function (函数-函数体) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class FunFunction extends BaseEntity {

    /**
     * 分类编码
     */
    @Desc("分类编码")
    private String categoryCode;

    /**
     * 函数编码
     */
    @Desc("函数编码")
    private String funCode;

    /**
     * 函数名称
     */
    @Desc("函数名称")
    private String funName;

    /**
     * 参数列表
     */
    @Desc("参数列表")
    private String funParams;

    /**
     * 函数语言
     */
    @Desc("函数语言")
    private String funLanguage;

    /**
     * 函数体
     */
    @Desc("函数体")
    private String funBody;

    /**
     * 返回类型
     */
    @Desc("返回类型")
    private String funReturn;

    /**
     * 函数说明
     */
    @Desc("函数说明")
    private String funDesc;

    /**
     * 模拟数据
     */
    @Desc("模拟数据")
    private String funMockData;

    /**
     * 可见1/0
     */
    @Desc("可见1/0")
    private Integer visible;

    /**
     * 内置
     */
    @Desc("内置")
    private Integer defaultFlag;


    public static FunFunction copy(FunFunction source, FunFunction target) {
        if (target == null ) { target = new FunFunction();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setCategoryCode(source.getCategoryCode());
        target.setFunCode(source.getFunCode());
        target.setFunName(source.getFunName());
        target.setFunParams(source.getFunParams());
        target.setFunLanguage(source.getFunLanguage());
        target.setFunBody(source.getFunBody());
        target.setFunReturn(source.getFunReturn());
        target.setFunDesc(source.getFunDesc());
        target.setFunMockData(source.getFunMockData());
        target.setVisible(source.getVisible());
        target.setDefaultFlag(source.getDefaultFlag());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static FunFunction copyIfNotNull(FunFunction source, FunFunction target) {
        if (target == null ) { target = new FunFunction();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getCategoryCode() != null) { target.setCategoryCode(source.getCategoryCode()); }
        if (source.getFunCode() != null) { target.setFunCode(source.getFunCode()); }
        if (source.getFunName() != null) { target.setFunName(source.getFunName()); }
        if (source.getFunParams() != null) { target.setFunParams(source.getFunParams()); }
        if (source.getFunLanguage() != null) { target.setFunLanguage(source.getFunLanguage()); }
        if (source.getFunBody() != null) { target.setFunBody(source.getFunBody()); }
        if (source.getFunReturn() != null) { target.setFunReturn(source.getFunReturn()); }
        if (source.getFunDesc() != null) { target.setFunDesc(source.getFunDesc()); }
        if (source.getFunMockData() != null) { target.setFunMockData(source.getFunMockData()); }
        if (source.getVisible() != null) { target.setVisible(source.getVisible()); }
        if (source.getDefaultFlag() != null) { target.setDefaultFlag(source.getDefaultFlag()); }
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

