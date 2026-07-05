package com.wkclz.micro.fileos.bean.req;

import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文件记录删除请求")
public class RecordRemoveReq extends RemoveReq implements Serializable {
}
