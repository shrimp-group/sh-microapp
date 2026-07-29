package com.wkclz.micro.flowable.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "申请单分页查询")
public class ApplyPageReq extends PageReq {
    @Schema(description = "业务类型")
    private String businessType;
    @Schema(description = "状态")
    private String status;
}
