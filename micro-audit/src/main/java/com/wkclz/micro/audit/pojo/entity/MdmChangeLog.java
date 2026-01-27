package com.wkclz.micro.audit.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_change_log (变更记录) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmChangeLog extends BaseEntity {

    /**
     * 批次
     */
    @Desc("批次")
    private String batchNo;

    /**
     * 表名
     */
    @Desc("表名")
    private String tableName;

    /**
     * 数据ID
     */
    @Desc("数据ID")
    private Long dataId;

    /**
     * 数据版本
     */
    @Desc("数据版本")
    private Integer dataVersion;

    /**
     * 操作类型
     */
    @Desc("操作类型")
    private String operateType;

    /**
     * 原数据
     */
    @Desc("原数据")
    private String dataFrom;

    /**
     * 目标数据
     */
    @Desc("目标数据")
    private String dataTo;


    public static MdmChangeLog copy(MdmChangeLog source, MdmChangeLog target) {
        if (target == null ) { target = new MdmChangeLog();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setBatchNo(source.getBatchNo());
        target.setTableName(source.getTableName());
        target.setDataId(source.getDataId());
        target.setDataVersion(source.getDataVersion());
        target.setOperateType(source.getOperateType());
        target.setDataFrom(source.getDataFrom());
        target.setDataTo(source.getDataTo());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmChangeLog copyIfNotNull(MdmChangeLog source, MdmChangeLog target) {
        if (target == null ) { target = new MdmChangeLog();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getBatchNo() != null) { target.setBatchNo(source.getBatchNo()); }
        if (source.getTableName() != null) { target.setTableName(source.getTableName()); }
        if (source.getDataId() != null) { target.setDataId(source.getDataId()); }
        if (source.getDataVersion() != null) { target.setDataVersion(source.getDataVersion()); }
        if (source.getOperateType() != null) { target.setOperateType(source.getOperateType()); }
        if (source.getDataFrom() != null) { target.setDataFrom(source.getDataFrom()); }
        if (source.getDataTo() != null) { target.setDataTo(source.getDataTo()); }
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

