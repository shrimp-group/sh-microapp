package com.wkclz.micro.mask.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "脱敏规则分页查询请求")
public class MaskRulePageReq extends PageReq {

    @Schema(description = "脱敏规则编码【支持模糊查询】")
    private String maskRuleCode;

    @Schema(description = "脱敏规则名称【支持模糊查询】")
    private String maskRuleName;

    @Schema(description = "请求方法")
    private String requestMethod;

    @Schema(description = "请求路径")
    private String requestUri;

    @Schema(description = "可用状态")
    private Integer enableFlag;
}
