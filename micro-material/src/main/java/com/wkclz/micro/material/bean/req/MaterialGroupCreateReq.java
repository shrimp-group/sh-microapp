package com.wkclz.micro.material.bean.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaterialGroupCreateReq {

    private String parentCode;
    @NotBlank(message = "groupName 不能为空")
    private String groupName;
    private String groupType;
}
