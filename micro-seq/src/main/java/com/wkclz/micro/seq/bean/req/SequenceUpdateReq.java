package com.wkclz.micro.seq.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "序列生成修改请求")
public class SequenceUpdateReq extends UpdateReq {

    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String seqName;

    @NotBlank(message = "前缀不能为空")
    @Schema(description = "前缀", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prefix;

    @NotNull(message = "当前序列不能为空")
    @Schema(description = "当前序列", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sequence;

    @NotNull(message = "序列长度不能为空")
    @Schema(description = "序列长度(不计前缀长度)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer codeLength;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
