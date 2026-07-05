package com.wkclz.micro.points.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 钱包查询入参（运营端按 userCode 查询）
 */
@Data
@Schema(description = "钱包查询入参")
public class PointsWalletQueryReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @NotBlank(message = "userCode 不能为空")
    @Schema(description = "用户编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userCode;

}
