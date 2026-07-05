package com.wkclz.micro.seq.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "序列生成分页查询请求")
public class SequencePageReq extends PageReq {

    @Schema(description = "名称【支持模糊查询】")
    private String seqName;

    @Schema(description = "前缀【支持模糊查询】")
    private String prefix;

    @Schema(description = "当前序列")
    private Integer sequence;

    @Schema(description = "序列长度(不计前缀长度)")
    private Integer codeLength;
}
