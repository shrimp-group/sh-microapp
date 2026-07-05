package com.wkclz.micro.pdf.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.pdf.bean.entity.MdmPdfTemplate;
import com.wkclz.micro.pdf.bean.req.*;
import com.wkclz.micro.pdf.bean.resp.PdfTemplateInfoResp;
import com.wkclz.micro.pdf.bean.resp.PdfTemplatePageResp;
import com.wkclz.micro.pdf.helper.PdfHelper;
import com.wkclz.micro.pdf.service.MdmPdfTemplateService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

@Tag(name = "PDF模板管理", description = "PDF模板的增删改查与Mock预览接口")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class PdfTemplateRest {

    @Autowired
    private PdfHelper pdfHelper;
    @Autowired
    private MdmPdfTemplateService mdmPdfTemplateService;

    @Operation(summary = "1. PDF-模板-分页", description = "根据条件分页查询PDF模板列表")
    @GetMapping(Route.PDF_TEMPLATE_PAGE)
    public R<PageData<PdfTemplatePageResp>> pdfTemplatePage(@Valid PdfTemplatePageReq req) {
        MdmPdfTemplate entity = BeanUtil.cp(req, MdmPdfTemplate.class);
        PageData<MdmPdfTemplate> page = mdmPdfTemplateService.getPdfTemplatePage(entity);
        PageData<PdfTemplatePageResp> newPage = page.convert(PdfTemplatePageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2. PDF-模板-详情", description = "根据ID查询PDF模板详情")
    @GetMapping(Route.PDF_TEMPLATE_INFO)
    public R<PdfTemplateInfoResp> pdfTemplateInfo(@Valid PdfTemplateInfoReq req) {
        MdmPdfTemplate entity = mdmPdfTemplateService.selectById(req.getId());
        PdfTemplateInfoResp resp = BeanUtil.cp(entity, PdfTemplateInfoResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3. PDF-模板-新增", description = "新增PDF模板")
    @PostMapping(Route.PDF_TEMPLATE_CREATE)
    public R<PdfTemplateInfoResp> pdfTemplateCreate(@Valid @RequestBody PdfTemplateCreateReq req) {
        PdfHelper.getContext(req.getMockData());
        MdmPdfTemplate entity = BeanUtil.cp(req, MdmPdfTemplate.class);
        entity = mdmPdfTemplateService.create(entity);
        PdfTemplateInfoResp resp = BeanUtil.cp(entity, PdfTemplateInfoResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4. PDF-模板-修改", description = "修改PDF模板")
    @PostMapping(Route.PDF_TEMPLATE_UPDATE)
    public R<PdfTemplateInfoResp> pdfTemplateUpdate(@Valid @RequestBody PdfTemplateUpdateReq req) {
        PdfHelper.getContext(req.getMockData());
        MdmPdfTemplate entity = BeanUtil.cp(req, MdmPdfTemplate.class);
        entity = mdmPdfTemplateService.update(entity);
        PdfTemplateInfoResp resp = BeanUtil.cp(entity, PdfTemplateInfoResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5. PDF-模板-删除", description = "删除PDF模板")
    @PostMapping(Route.PDF_TEMPLATE_REMOVE)
    public R<Void> pdfTemplateRemove(@Valid @RequestBody PdfTemplateRemoveReq req) {
        MdmPdfTemplate entity = new MdmPdfTemplate();
        entity.setId(req.getId());
        mdmPdfTemplateService.deleteById(entity);
        return R.ok();
    }

    @Operation(summary = "6. PDF-模板-Mock", description = "在线预览PDF模板效果")
    @PostMapping(Route.PDF_TEMPLATE_MOCK)
    public void pdfTemplateMock(@Valid @RequestBody PdfTemplateMockReq req, HttpServletResponse response) {
        Context context = PdfHelper.getContext(req.getMockData());
        String htmlContent = PdfHelper.thymeleafRenderer(req.getTemplateContext(), context);
        String pdfPath = pdfHelper.pdfRenderer(htmlContent);
        String name = "Mock_" + System.currentTimeMillis();
        PdfHelper.pdfResponse(response, pdfPath, name);
    }
}
