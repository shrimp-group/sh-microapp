package com.wkclz.micro.dict.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "字典复制请求")
public class DictCopyReq implements Serializable {

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "字典类型列表")
    private List<String> dictTypes;
}
