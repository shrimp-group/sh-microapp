package com.wkclz.auto.bean;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class ApiInfo implements Serializable {
    private Class<?> controllerClass;
    private String module;
    private String method;
    private String uri;
    private String name;
    private String desc;
    private List<ApiParamInfo> params;
    private Class<?> returnType;
}
