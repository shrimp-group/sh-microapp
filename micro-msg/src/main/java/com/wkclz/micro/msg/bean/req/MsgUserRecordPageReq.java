package com.wkclz.micro.msg.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户消息记录分页查询请求")
public class MsgUserRecordPageReq extends PageReq {

    @Schema(description = "消息编码")
    private String noticeNo;

    @Schema(description = "阅读状态")
    private Integer readStatus;

    @Schema(description = "标题(模糊查询)")
    private String title;
}
