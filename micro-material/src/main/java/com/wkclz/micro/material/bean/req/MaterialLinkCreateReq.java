package com.wkclz.micro.material.bean.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaterialLinkCreateReq {

    private String materialName;
    private String materialType;
    @NotBlank(message = "linkUrl 不能为空")
    private String linkUrl;
    private String groupCode;
    private String visibility;
    private String description;
}
