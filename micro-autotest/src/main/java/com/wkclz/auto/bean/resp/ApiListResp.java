package com.wkclz.auto.bean.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ApiListResp implements Serializable {

    private String className;
    private String methodName;
    private String methodDesc;
    private String requestMethod;
    private String url;
    private List<ApiParamResp> params;

    @Data
    public static class ApiParamResp implements Serializable {
        private String paramName;
        private String paramType;
        private String paramDesc;
        private Boolean required;
    }
}
