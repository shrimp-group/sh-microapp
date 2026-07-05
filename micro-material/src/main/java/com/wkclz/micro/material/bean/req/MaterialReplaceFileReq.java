package com.wkclz.micro.material.bean.req;

import com.wkclz.web.bean.UpdateReq;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialReplaceFileReq extends UpdateReq {

    @NotBlank(message = "fileId 不能为空")
    private String fileId;
    private String fileName;
    private Long fileSize;
}
