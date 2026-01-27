package com.wkclz.micro.pdf.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.pdf.helper.PdfHelper;
import com.wkclz.micro.pdf.pojo.entity.MdmPdfTemplate;
import com.wkclz.micro.pdf.service.MdmPdfTemplateService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_pdf_template (PDF模板) 示例rest 接口，代码重新生成会覆盖
 */
@Slf4j
@RestController
public class PdfTemplateRest {

    @Autowired
    private PdfHelper pdfHelper;
    @Autowired
    private MdmPdfTemplateService mdmPdfTemplateService;

    
    /**
     * @api {get} /pdf/template/page 1. PDF-模板-分页
     * @apiGroup PDF
     *
     * @apiVersion 0.0.1
     * @apiDescription PDF模板-获取分页
     *
     * @apiParam {String} [templateCode] <code>param</code>模板编码
     * @apiParam {String} [templateName] <code>param</code>模板名称
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [templateCode] 模板编码
     * @apiSuccess {String} [templateName] 模板名称
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "templateCode": "templateCode",
     *                 "templateName": "templateName",
     *                 "sort": "sort",
     *                 "createTime": "createTime",
     *                 "createBy": "createBy",
     *                 "updateTime": "updateTime",
     *                 "updateBy": "updateBy",
     *                 "remark": "remark",
     *                 "version": "version",
     *             },
     *             ...
     *         ],
     *         "current": 1,
     *         "size": 10,
     *         "total": 1,
     *         "page": 1,
     *     }
     * }
     *
     */
    @GetMapping(Route.PDF_TEMPLATE_PAGE)
    public R mdmPdfTemplatePage(MdmPdfTemplate entity) {
        PageData<MdmPdfTemplate> page = mdmPdfTemplateService.getPdfTemplatePage(entity);
        return R.ok(page);
    }

    /**
     * @api {get} /pdf/template/info 2. PDF-模板-详情
     * @apiGroup PDF
     *
     * @apiVersion 0.0.1
     * @apiDescription PDF模板-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [templateCode] 模板编码
     * @apiSuccess {String} [templateName] 模板名称
     * @apiSuccess {String} [templateContext] 模板内容
     * @apiSuccess {Integer} [sort] 排序
     * @apiSuccess {Date} [createTime] 创建时间
     * @apiSuccess {String} [createBy] 创建人
     * @apiSuccess {Date} [updateTime] 更新时间
     * @apiSuccess {String} [updateBy] 更新人
     * @apiSuccess {String} [remark] 备注
     * @apiSuccess {Integer} [version] 版本号
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *          "id": "id",
     *          "templateCode": "templateCode",
     *          "templateName": "templateName",
     *          "templateContext": "templateContext",
     *          "sort": "sort",
     *          "createTime": "createTime",
     *          "createBy": "createBy",
     *          "updateTime": "updateTime",
     *          "updateBy": "updateBy",
     *          "remark": "remark",
     *          "version": "version",
     *          "status": "status",
     *     }
     * }
     *
     */
    @GetMapping(Route.PDF_TEMPLATE_INFO)
    public R mdmPdfTemplateInfo(MdmPdfTemplate entity) {
        entity = mdmPdfTemplateService.selectById(entity.getId());
        return R.ok(entity);
    }

    /**
     * @api {post} /pdf/template/create 3. PDF-模板-新增
     * @apiGroup PDF
     *
     * @apiVersion 0.0.1
     * @apiDescription PDF模板-新增信息
     *
     * @apiParam {String} [templateCode] <code>body</code>模板编码
     * @apiParam {String} [templateName] <code>body</code>模板名称
     * @apiParam {String} [templateContext] <code>body</code>模板内容
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "templateCode": "templateCode",
     *      "templateName": "templateName",
     *      "templateContext": "templateContext",
     *      "sort": "sort",
     *      "remark": "remark",
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.PDF_TEMPLATE_CREATE)
    public R mdmPdfTemplateCreate(@RequestBody MdmPdfTemplate entity) {
        paramCheck(entity);
        entity = mdmPdfTemplateService.create(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /pdf/template/update 4. PDF-模板-修改
     * @apiGroup PDF
     *
     * @apiVersion 0.0.1
     * @apiDescription PDF模板-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [templateCode] <code>body</code>模板编码
     * @apiParam {String} [templateName] <code>body</code>模板名称
     * @apiParam {String} [templateContext] <code>body</code>模板内容
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "templateCode": "templateCode",
     *      "templateName": "templateName",
     *      "templateContext": "templateContext",
     *      "sort": "sort",
     *      "remark": "remark",
     *      "version": "version",
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.PDF_TEMPLATE_UPDATE)
    public R mdmPdfTemplateUpdate(@RequestBody MdmPdfTemplate entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        paramCheck(entity);
        entity.setTemplateCode(null);
        entity = mdmPdfTemplateService.update(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /pdf/template/remove 5. PDF-模板-删除
     * @apiGroup PDF
     *
     * @apiVersion 0.0.1
     * @apiDescription PDF模板-删除
     *
     * @apiParam {Long} [id] <code>body</code>数据id
     *
     * @apiParamExample {json} 请求样例:
     * {
     *     "id": 1
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": 1
     * }
     *
     */
    @PostMapping(Route.PDF_TEMPLATE_REMOVE)
    public R mdmPdfTemplateRemove(@RequestBody MdmPdfTemplate entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        mdmPdfTemplateService.deleteById(entity);
        return R.ok(1);
    }



    /**
     * @api {post} /pdf/template/mock 6. PDF-模板-Mock
     * @apiGroup PDF
     *
     * @apiVersion 0.0.1
     * @apiDescription PDF模板-删除
     *
     * @apiParam {String} [templateContext] <code>body</code>模板正文
     * @apiParam {json} [mockData] <code>body</code>Mock正文内容
     *
     * @apiParamExample {json} 请求样例:
     * {
     *     "templateContext": "<html>html内容</html>",
     *     "mockData": "{json数据}"
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": 文件流
     * }
     *
     */
    @PostMapping(Route.PDF_TEMPLATE_MOCK)
    public void mdmPdfTemplateMock(@RequestBody MdmPdfTemplate entity, HttpServletResponse response) {
        Assert.notNull(entity.getTemplateContext(), "templateContext 不能为空");
        String mockData = entity.getMockData();

        // 创建上下文并设置参数
        Context context = PdfHelper.getContext(mockData);
        String htmlContent = PdfHelper.thymeleafRenderer(entity.getTemplateContext(), context);
        String pdfPath = pdfHelper.pdfRenderer(htmlContent);

        String name = "Mock_" + System.currentTimeMillis();
        // 响应 pdf
        PdfHelper.pdfResponse(response, pdfPath, name);
    }


    private static void paramCheck(MdmPdfTemplate entity) {
        Assert.notNull(entity.getTemplateName(), "templateName 不能为空");
        Assert.notNull(entity.getTemplateContext(), "templateContext 不能为空");
        PdfHelper.getContext(entity.getMockData());
    }

}

