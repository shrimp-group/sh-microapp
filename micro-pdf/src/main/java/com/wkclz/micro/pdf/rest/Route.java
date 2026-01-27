package com.wkclz.micro.pdf.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-pdf", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-pdf";


    @Desc("1. PDF-模板-分页")
    String PDF_TEMPLATE_PAGE = "/pdf/template/page";
    @Desc("2. PDF-模板-详情")
    String PDF_TEMPLATE_INFO = "/pdf/template/info";
    @Desc("3. PDF-模板-新增")
    String PDF_TEMPLATE_CREATE = "/pdf/template/create";
    @Desc("4. PDF-模板-修改")
    String PDF_TEMPLATE_UPDATE = "/pdf/template/update";
    @Desc("5. PDF-模板-删除")
    String PDF_TEMPLATE_REMOVE = "/pdf/template/remove";
    @Desc("6. PDF-模板-Mock")
    String PDF_TEMPLATE_MOCK = "/pdf/template/mock";

}
