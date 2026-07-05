package com.wkclz.micro.seq.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "序列生成响应")
public class SequenceResp extends EntityResp {

    @Schema(description = "名称")
    private String seqName;

    @Schema(description = "前缀")
    private String prefix;

    @Schema(description = "当前序列")
    private Integer sequence;

    @Schema(description = "序列长度(不计前缀长度)")
    private Integer codeLength;
}
