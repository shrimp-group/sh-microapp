package com.wkclz.micro.pdf.rest;


import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-pdf", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-pdf";


    String PDF_TEMPLATE_PAGE = "/template/page";
    String PDF_TEMPLATE_INFO = "/template/info";
    String PDF_TEMPLATE_CREATE = "/template/create";
    String PDF_TEMPLATE_UPDATE = "/template/update";
    String PDF_TEMPLATE_REMOVE = "/template/remove";
    String PDF_TEMPLATE_MOCK = "/template/mock";

}
