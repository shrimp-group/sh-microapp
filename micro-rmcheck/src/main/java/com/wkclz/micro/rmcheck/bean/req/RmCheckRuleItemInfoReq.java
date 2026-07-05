package com.wkclz.micro.rmcheck.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "删除检查规则检查项详情查询请求")
public class RmCheckRuleItemInfoReq extends IdReq {
}
