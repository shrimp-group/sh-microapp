package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "流程设计旧版本清理请求")
public class DesignCleanupReq {

    @NotNull(message = "id 不能为空")
    @Schema(description = "设计 ID")
    private Long id;

    @Schema(description = "是否级联删除历史（默认 false：保留历史，旧版本存在运行实例时引擎拒绝；true：级联删除运行实例与全部历史）")
    private Boolean cascade;
}
