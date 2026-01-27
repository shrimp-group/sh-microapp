package com.wkclz.micro.audit.rest;

import com.alibaba.fastjson2.JSONObject;
import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.audit.pojo.dto.ChangeLogMap;
import com.wkclz.micro.audit.pojo.entity.MdmChangeLog;
import com.wkclz.micro.audit.service.MdmChangeLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_change_log (变更记录) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
public class ChangeLogRest {

    @Autowired
    private MdmChangeLogService mdmChangeLogService;


    /**
     * @api {get} /change/log/page 1. 变更记录-获取分页
     * @apiGroup AUDIT
     *
     * @apiVersion 0.0.1
     * @apiDescription 变更记录-获取分页
     *
     * @apiParam {String} [batchNo] <code>param</code>批次
     * @apiParam {String} [tableName] <code>param</code>表名
     * @apiParam {Long} [dataId] <code>param</code>数据ID
     * @apiParam {String} [operateType] <code>param</code>操作类型
     * @apiParam {String} [dataFrom] <code>param</code>原数据
     * @apiParam {String} [dataTo] <code>param</code>目标数据
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [batchNo] 批次
     * @apiSuccess {String} [tableName] 表名
     * @apiSuccess {Long} [dataId] 数据ID
     * @apiSuccess {String} [operateType] 操作类型
     * @apiSuccess {String} [dataFrom] 原数据
     * @apiSuccess {String} [dataTo] 目标数据
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "batchNo": "batchNo",
     *                 "tableName": "tableName",
     *                 "dataId": "dataId",
     *                 "operateType": "operateType",
     *                 "dataFrom": "dataFrom",
     *                 "dataTo": "dataTo",
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
    @GetMapping(Route.CHANGE_LOG_PAGE)
    public R mdmChangeLogPage(MdmChangeLog entity) {
        PageData<MdmChangeLog> page = mdmChangeLogService.getChangeLogPage(entity);
        List<ChangeLogMap> mapList = page.getRecords().stream().map(t -> {
            ChangeLogMap copy = ChangeLogMap.copy(t);
            String from = copy.getDataFrom();
            String to = copy.getDataTo();
            if (StringUtils.isNotBlank(from)) {
                copy.setDataFromEntity(JSONObject.parseObject(from));
            }
            if (StringUtils.isNotBlank(to)) {
                copy.setDataToEntity(JSONObject.parseObject(to));
            }
            return copy;
        }).toList();
        PageData<ChangeLogMap> newPage = PageData.of(mapList, page.getTotal(), page.getCurrent(), page.getSize());
        return R.ok(newPage);
    }

    /**
     * @api {get} /change/log/info 2. 变更记录-获取详情
     * @apiGroup AUDIT
     *
     * @apiVersion 0.0.1
     * @apiDescription 变更记录-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [batchNo] 批次
     * @apiSuccess {String} [tableName] 表名
     * @apiSuccess {Long} [dataId] 数据ID
     * @apiSuccess {String} [operateType] 操作类型
     * @apiSuccess {String} [dataFrom] 原数据
     * @apiSuccess {String} [dataTo] 目标数据
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
     *          "batchNo": "batchNo",
     *          "tableName": "tableName",
     *          "dataId": "dataId",
     *          "operateType": "operateType",
     *          "dataFrom": "dataFrom",
     *          "dataTo": "dataTo",
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
    @GetMapping(Route.CHANGE_LOG_INFO)
    public R mdmChangeLogInfo(MdmChangeLog entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        entity = mdmChangeLogService.selectById(entity.getId());
        ChangeLogMap copy = ChangeLogMap.copy(entity);
        String from = copy.getDataFrom();
        String to = copy.getDataTo();
        if (StringUtils.isNotBlank(from)) {
            copy.setDataFromEntity(JSONObject.parseObject(from));
        }
        if (StringUtils.isNotBlank(to)) {
            copy.setDataToEntity(JSONObject.parseObject(to));
        }
        return R.ok(copy);
    }

}

