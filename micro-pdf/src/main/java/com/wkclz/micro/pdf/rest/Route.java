package com.wkclz.micro.pdf.rest;


import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-pdf", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-pdf";


    @ApiDesc("1. PDF-模板-分页")
    String PDF_TEMPLATE_PAGE = "/template/page";
    @ApiDesc("2. PDF-模板-详情")
    String PDF_TEMPLATE_INFO = "/template/info";
    @ApiDesc("3. PDF-模板-新增")
    String PDF_TEMPLATE_CREATE = "/template/create";
    @ApiDesc("4. PDF-模板-修改")
    String PDF_TEMPLATE_UPDATE = "/template/update";
    @ApiDesc("5. PDF-模板-删除")
    String PDF_TEMPLATE_REMOVE = "/template/remove";
    @ApiDesc("6. PDF-模板-Mock")
    String PDF_TEMPLATE_MOCK = "/template/mock";

}
