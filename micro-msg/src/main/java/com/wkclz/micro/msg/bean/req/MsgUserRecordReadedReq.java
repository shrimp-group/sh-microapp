package com.wkclz.micro.msg.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户消息记录批量已读请求")
public class MsgUserRecordReadedReq extends UpdateReq {

    @Schema(description = "主键ID数组")
    private List<Long> ids;
}
