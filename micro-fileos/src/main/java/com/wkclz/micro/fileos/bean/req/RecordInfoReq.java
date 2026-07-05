package com.wkclz.micro.fileos.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文件记录详情查询请求")
public class RecordInfoReq extends IdReq implements Serializable {
}
