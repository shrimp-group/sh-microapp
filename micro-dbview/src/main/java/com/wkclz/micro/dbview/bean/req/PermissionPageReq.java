package com.wkclz.micro.dbview.bean.req;

import com.wkclz.web.bean.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PermissionPageReq extends PageReq {
    private Long datasourceId;
}
