package com.wkclz.micro.form.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;



/**
 * @author shrimp
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FormRuleItemVo implements Serializable {

    private boolean required;
    private String type;
    private Double min;
    private Double max;
    private Integer minLength;
    private Integer maxLength;
    private String pattern;
    private String validator;
    private String trigger;
    private String message;

}

