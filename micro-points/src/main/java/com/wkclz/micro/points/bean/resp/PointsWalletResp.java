package com.wkclz.micro.points.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 钱包查询出参
 */
@Data
@Schema(description = "钱包查询出参")
public class PointsWalletResp {

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "可用积分")
    private Integer availablePoints;

    @Schema(description = "冻结积分")
    private Integer frozenPoints;

    @Schema(description = "历史总获得积分")
    private Integer totalEarnedPoints;

}
