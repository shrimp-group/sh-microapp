package com.wkclz.micro.points.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取流水分页入参
 * C 端基于登录 userCode，运营端按 userCode 查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "获取流水分页入参")
public class PointsEarnPageReq extends PageReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "积分来源类型（可选筛选，ISSUANCE/REFUND/ADMIN_ISSUE）")
    private String pointSourceType;

    @Schema(description = "来源单据号（可选筛选）")
    private String sourceNo;

    @Schema(description = "是否已使用完（可选筛选，0/1）")
    private Integer isUsedUp;

}
