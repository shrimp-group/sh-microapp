package com.wkclz.micro.material.api;

import java.util.Map;

public interface MaterialApi {

    Integer bind(String materialCode, String bizType, String bizCode, String refDesc);

    Integer unbind(String materialCode, String bizType, String bizCode);

    Map<String, Object> check(String materialCode);
}
