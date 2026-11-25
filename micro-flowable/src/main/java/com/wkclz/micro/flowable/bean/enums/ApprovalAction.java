package com.wkclz.micro.flowable.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 审批动作 */
@Getter
@RequiredArgsConstructor
public enum ApprovalAction {

    SUBMIT("提交"),
    APPROVE("通过"),
    REJECT("驳回"),
    TRANSFER("转办"),
    DELEGATE("委派"),
    WITHDRAW("撤回"),
    CLAIM("领取"),
    ;

    /** 中文含义 */
    private final String desc;
}
