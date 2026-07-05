package com.wkclz.micro.dbview.bean.req;

import com.wkclz.web.bean.UpdateReq;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PermissionUpdateReq extends UpdateReq {
    @NotNull(message = "数据源ID不能为空")
    private Long datasourceId;
    @NotBlank(message = "用户编码不能为空")
    private String userCode;
    @NotBlank(message = "权限等级不能为空")
    private String permissionLevel;
}
