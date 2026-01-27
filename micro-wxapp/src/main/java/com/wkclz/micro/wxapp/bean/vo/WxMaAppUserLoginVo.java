package com.wkclz.micro.wxapp.bean.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author shrimp
 */
@Data
public class WxMaAppUserLoginVo implements Serializable {

    private String appId;
    private String code;
    private String encryptedData;
    private String iv;
    private String rawData;
    private String signature;

}
