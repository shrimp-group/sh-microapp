package com.wkclz.auto.bean.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class RunReq implements Serializable {

    @NotBlank(message = "包路径不能为空")
    private String packagePath;

    private String reportDir;
}
