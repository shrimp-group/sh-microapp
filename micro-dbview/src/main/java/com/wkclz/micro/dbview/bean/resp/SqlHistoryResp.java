package com.wkclz.micro.dbview.bean.resp;

import com.wkclz.web.bean.EntityResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SqlHistoryResp extends EntityResp {
    private Long datasourceId;
    private String sql;
    private String sqlType;
    private Long costTime;
}
