package com.wkclz.micro.points.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分回退出参
 */
@Data
@Schema(description = "积分回退出参")
public class PointsRefundResp {

    @Schema(description = "本次回退流水号")
    private String flowNo;

    @Schema(description = "回退积分数")
    private Integer points;

}
