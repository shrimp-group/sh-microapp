package com.wkclz.micro.msg.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户消息记录列表请求")
public class MsgUserRecordListReq implements Serializable {

    @Schema(description = "消息编码")
    private String noticeNo;

    @Schema(description = "阅读状态")
    private Integer readStatus;

    @Schema(description = "标题(模糊查询)")
    private String title;

    @Schema(description = "分页大小")
    private Long size;
}
