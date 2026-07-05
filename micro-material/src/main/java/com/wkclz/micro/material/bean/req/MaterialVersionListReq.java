package com.wkclz.micro.material.bean.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class MaterialVersionListReq implements Serializable {

    @NotBlank(message = "materialCode 不能为空")
    private String materialCode;
}
