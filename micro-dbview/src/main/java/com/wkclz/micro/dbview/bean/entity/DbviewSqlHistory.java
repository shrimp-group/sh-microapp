package com.wkclz.micro.dbview.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DbviewSqlHistory extends BaseEntity {

    @FieldDesc("数据源ID")
    private Long datasourceId;

    @FieldDesc("执行人")
    private String userCode;

    @FieldDesc("SQL文本")
    private String sqlText;

    @FieldDesc("SQL类型")
    private String sqlType;

    @FieldDesc("执行状态：0-失败 1-成功")
    private Integer status;

    @FieldDesc("影响行数")
    private Long affectedRows;

    @FieldDesc("耗时(ms)")
    private Long costMs;

    @FieldDesc("错误信息")
    private String errorMessage;
}
