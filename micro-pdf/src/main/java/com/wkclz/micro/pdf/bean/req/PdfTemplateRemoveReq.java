package com.wkclz.micro.pdf.bean.req;

import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "PDF模板删除请求")
public class PdfTemplateRemoveReq extends RemoveReq {
}
