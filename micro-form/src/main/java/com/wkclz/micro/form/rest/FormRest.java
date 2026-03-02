package com.wkclz.micro.form.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.form.cache.FormCache;
import com.wkclz.micro.form.pojo.dto.MdmFormDto;
import com.wkclz.micro.form.pojo.entity.MdmForm;
import com.wkclz.micro.form.pojo.entity.MdmFormItem;
import com.wkclz.micro.form.service.FormTableInfoService;
import com.wkclz.micro.form.service.MdmFormService;
import com.wkclz.mybatis.bean.ColumnQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form (表单) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class FormRest {

    @Autowired
    private FormCache formCache;
    @Autowired
    private MdmFormService mdmFormService;
    @Autowired
    private FormTableInfoService formTableInfoService;

    /**
     * @api {get} /micro-form/page 01. 表单-分页
     * @apiGroup FORM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单-获取分页
     *
     * @apiParam {String} [fromCode] <code>param</code>表单编码(模糊搜索)
     * @apiParam {String} [fromName] <code>param</code>表单名称(模糊搜索)
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [fromCode] 表单编码
     * @apiSuccess {String} [fromName] 表单名称
     * @apiSuccess {Integer} [itemCount] 表单项数量
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
    @GetMapping(Route.FORM_PAGE)
    public R mdmFormPage(MdmFormDto dto) {
        PageData<MdmFormDto> page = mdmFormService.getFormPage(dto);
        return R.ok(page);
    }


    /**
     * @api {get} /micro-form/info 02. 表单-详情
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
    @GetMapping(Route.FORM_INFO)
    public R mdmFormInfo(MdmForm entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        MdmFormDto dto = mdmFormService.getFormInfo(entity);
        return R.ok(dto);
    }


    /**
     * @api {post} /micro-form/create 03. 表单创建
     * @apiGroup FORM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单-新增信息
     *
     * @apiParam {String} [fromCode] <code>body</code>表单编码
     * @apiParam {String} [fromName] <code>body</code>表单名称
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Array} [items] <code>body</code>表单项
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "fromCode": "fromCode",
     *      "fromName": "fromName",
     *      "sort": "sort",
     *      "remark": "remark",
     *      "items": []
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.FORM_CREATE)
    public R mdmFormCreate(@RequestBody MdmFormDto dto) {
        parmCheck(dto);
        MdmForm entity = mdmFormService.create(dto);
        formCache.clearCache();
        return R.ok(entity);
    }

    /**
     * @api {post} /micro-form/update 04. 表单-修改
     * @apiGroup FORM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [fromCode] <code>body</code>表单编码
     * @apiParam {String} [fromName] <code>body</code>表单名称
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     * @apiParam {Array} [items] <code>body</code>表单项
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "fromCode": "fromCode",
     *      "fromName": "fromName",
     *      "sort": "sort",
     *      "remark": "remark",
     *      "version": "version",
     *      "items": []
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.FORM_UPDATE)
    public R mdmFormUpdate(@RequestBody MdmFormDto dto) {
        Assert.notNull(dto.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(dto.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        parmCheck(dto);
        dto.setFormCode(null);
        MdmForm entity = mdmFormService.update(dto);
        formCache.clearCache();
        return R.ok(entity);
    }


    /**
     * @api {post} /micro-form/remove 05. 表单-删除
     * @apiGroup FORM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单-删除
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
    @PostMapping(Route.FORM_REMOVE)
    public R mdmFormRemove(@RequestBody MdmForm entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        mdmFormService.customRemove(entity);
        formCache.clearCache();
        return R.ok(1);
    }


    /**
     * @api {get} /micro-form/db/columns 07. 表单输入项-数据库字段
     * @apiGroup FORM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单-输入项-获取分页
     *
     * @apiSuccess {String} [columnName] 字段名称
     * @apiSuccess {String} [dataType] 字段类型
     * @apiSuccess {String} [tsType] 类型（type）
     * @apiSuccess {String} [inputType] 类型（type）
     * @apiSuccess {String} [columnComment] 类型（type）
     *
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *         {
     *             "id": "id",
     *             "inputCode": "inputCode",
     *             "inputName": "inputName",
     *             "inputType": "inputType",
     *             "modelKey": "modelKey",
     *             "placeholder": "placeholder",
     *         },
     *         ...
     *     ]
     * }
     */
    @GetMapping(Route.FORM_DB_COLUMNS)
    public R formDbColumns() {
        List<ColumnQuery> infos = formTableInfoService.getColumnInfos();
        return R.ok(infos);
    }

    private static void parmCheck(MdmFormDto dto) {
        if (dto == null) {
            return;
        }
        Assert.notNull(dto.getFormName(), "formName 不能为空");
        List<MdmFormItem> items = dto.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        for (MdmFormItem item : items) {
            item.setFormCode(dto.getFormCode());
            Assert.notNull(item.getInputType(), "输入项类型 不能为空");
            Assert.notNull(item.getFieldType(), "字段类型 不能为空");
            Assert.notNull(item.getItemName(), "表单项名称 不能为空");
            Assert.notNull(item.getLabel(), "输入项标签 不能为空");
            if (item.getClearable() == null) {
                item.setClearable(1);
            }
            if (item.getSort() == null) {
                item.setSort(99);
            }
        }
    }
}

