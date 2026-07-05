package com.wkclz.micro.dbview.bean.resp;

import com.wkclz.web.bean.EntityResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PermissionResp extends EntityResp {
    private Long datasourceId;
    private String userCode;
    private String permissionLevel;
}
