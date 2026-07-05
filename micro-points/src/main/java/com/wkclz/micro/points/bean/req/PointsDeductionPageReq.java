package com.wkclz.micro.points.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 扣减明细分页入参
 * 用于运营端消费扣减明细查询（对账展示）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "扣减明细分页入参")
public class PointsDeductionPageReq extends PageReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "关联消费单据号")
    private String orderNo;

    @Schema(description = "积分获取流水号（可选筛选）")
    private String earnFlowNo;

}
