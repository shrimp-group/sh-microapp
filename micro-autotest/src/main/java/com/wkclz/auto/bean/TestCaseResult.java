package com.wkclz.auto.bean;

import lombok.Data;
import java.io.Serializable;

@Data
public class TestCaseResult implements Serializable {
    private String uri;
    private String method;
    private String desc;
    private String module;
    private boolean success;
    private int httpStatus;
    private long costTimeMs;
    private String requestBody;
    private String responseBody;
    private String errorMessage;
}
