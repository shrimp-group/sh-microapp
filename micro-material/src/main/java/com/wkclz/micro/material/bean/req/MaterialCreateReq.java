package com.wkclz.micro.material.bean.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaterialCreateReq {

    @NotBlank(message = "fileId 不能为空")
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String materialName;
    private String materialType;
    private String groupCode;
    private String visibility;
    private String description;
}
