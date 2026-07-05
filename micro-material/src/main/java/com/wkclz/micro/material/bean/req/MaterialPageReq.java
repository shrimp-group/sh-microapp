package com.wkclz.micro.material.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "素材分页查询请求")
public class MaterialPageReq extends PageReq {

    @Schema(description = "素材名称【支持模糊查询】")
    private String materialName;

    @Schema(description = "素材类型(IMAGE/VIDEO/AUDIO/DOCUMENT/OTHER)")
    private String materialType;

    @Schema(description = "来源类型(UPLOAD/LINK)")
    private String sourceType;

    @Schema(description = "分组编码")
    private String groupCode;

    @Schema(description = "可见性(PRIVATE/PUBLIC)")
    private String visibility;

    @Schema(description = "所有者用户编码")
    private String userCode;

    @Schema(description = "创建时间从")
    private LocalDateTime timeFrom;

    @Schema(description = "创建时间到")
    private LocalDateTime timeTo;
}
