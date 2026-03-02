package com.wkclz.micro.form.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.form.pojo.dto.MdmFormDto;
import com.wkclz.micro.form.pojo.entity.MdmForm;
import com.wkclz.micro.form.service.MdmFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form (表单) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class CommonFormRest {

    @Autowired
    private MdmFormService mdmFormService;


    /**
     * @api {get} /common/form/list 11. common-表单列表【用于生成下拉选项】
     * @apiGroup FORM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单-获取分页
     *
     * @apiParam {String} [fromCode] <code>param</code>表单编码
     * @apiParam {String} [fromName] <code>param</code>表单名称
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [fromCode] 表单编码
     * @apiSuccess {String} [fromName] 表单名称
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "fromCode": "fromCode",
     *                 "fromName": "fromName",
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
    @GetMapping(Route.COMMON_FORM_LIST)
    public R commonFormList() {
        List<MdmForm> list = mdmFormService.getFormOptions();
        return R.ok(list);
    }


    /**
     * @api {get} /common/form/detail 12. common-表单详情【用于构造输入表单】
     * @apiGroup FORM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [fromCode] 表单编码
     * @apiSuccess {String} [fromName] 表单名称
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
     *          "fromCode": "fromCode",
     *          "fromName": "fromName",
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
    @GetMapping(Route.COMMON_FORM_DETAIL)
    public R commonFormInfo(MdmForm entity) {
        Assert.notNull(entity.getFormCode(), "formCode 不能为空");
        MdmFormDto detail = mdmFormService.getCustomFormDetail(entity);
        return R.ok(detail);
    }

}

