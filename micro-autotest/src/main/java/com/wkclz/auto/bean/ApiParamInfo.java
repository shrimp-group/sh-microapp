package com.wkclz.auto.bean;

import lombok.Data;
import java.io.Serializable;

@Data
public class ApiParamInfo implements Serializable {
    private String name;
    private Class<?> type;
    private boolean requestBody;
    private boolean pathVariable;
    private boolean requestParam;
}
