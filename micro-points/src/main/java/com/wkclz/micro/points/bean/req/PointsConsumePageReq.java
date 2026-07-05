package com.wkclz.micro.points.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消费流水分页入参
 * C 端基于登录 userCode，运营端按 userCode 查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消费流水分页入参")
public class PointsConsumePageReq extends PageReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "关联单据号（可选筛选）")
    private String orderNo;

    @Schema(description = "状态（可选筛选，FROZEN 冻结 / DEDUCTED 已扣减）")
    private String status;

}
