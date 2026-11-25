package com.wkclz.micro.flowable.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** client 调用异常类型 */
@Getter
@RequiredArgsConstructor
public enum ErrorType {

    DEPLOY_ERROR("部署异常"),
    START_ERROR("发起异常"),
    APPROVE_ERROR("审批异常"),
    QUERY_ERROR("查询异常"),
    CALLBACK_ERROR("回调异常"),
    ;

    /** 中文含义 */
    private final String desc;
}
