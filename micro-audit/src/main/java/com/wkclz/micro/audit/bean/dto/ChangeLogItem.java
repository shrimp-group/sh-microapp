package com.wkclz.micro.audit.bean.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author shrimp
 */
@Data
public class ChangeLogItem implements Serializable {

    private String columnName;
    private String columnDesc;
    private Object oldValue;
    private Object newValue;

}

