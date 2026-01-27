package com.wkclz.micro.liteflow.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.liteflow.pojo.entity.LiteflowScript;
import com.wkclz.micro.liteflow.service.LiteflowScriptService;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table liteflow_script (liteflow-脚本) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
public class LiteflowScriptRest {

    @Autowired
    private LiteflowScriptService liteflowScriptService;

    /**
     * @api {get} /liteflow/script/page 1. liteflow-脚本-获取分页
     * @apiGroup LITEFLOW_SCRIPT
     *
     * @apiVersion 0.0.1
     * @apiDescription liteflow-脚本-获取分页
     *
     * @apiParam {String} [scriptId] <code>param</code>脚本ID
     * @apiParam {String} [scriptName] <code>param</code>脚本名称
     * @apiParam {String} [scriptType] <code>param</code>脚本类型
     * @apiParam {String} [scriptLanguage] <code>param</code>脚本语言
     * @apiParam {Integer} [enable] <code>param</code>可用状态
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [scriptId] 脚本ID
     * @apiSuccess {String} [scriptName] 脚本名称
     * @apiSuccess {String} [scriptType] 脚本类型
     * @apiSuccess {String} [scriptLanguage] 脚本语言
     * @apiSuccess {Integer} [enable] 可用状态
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "scriptId": "scriptId",
     *                 "scriptName": "scriptName",
     *                 "scriptType": "scriptType",
     *                 "scriptLanguage": "scriptLanguage",
     *                 "enable": "enable",
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
    @GetMapping(Route.SCRIPT_PAGE)
    public R liteflowScriptPage(LiteflowScript entity) {
        PageData<LiteflowScript> page = liteflowScriptService.getLiteflowScriptPage(entity);
        return R.ok(page);
    }




    /**
     * @api {get} /liteflow/script/info 2. liteflow-脚本-获取详情
     * @apiGroup LITEFLOW_SCRIPT
     *
     * @apiVersion 0.0.1
     * @apiDescription liteflow-脚本-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [scriptId] 脚本ID
     * @apiSuccess {String} [scriptName] 脚本名称
     * @apiSuccess {String} [scriptData] 脚本数据
     * @apiSuccess {String} [scriptType] 脚本类型
     * @apiSuccess {String} [scriptLanguage] 脚本语言
     * @apiSuccess {Integer} [enable] 可用状态
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
     *          "scriptId": "scriptId",
     *          "scriptName": "scriptName",
     *          "scriptData": "scriptData",
     *          "scriptType": "scriptType",
     *          "scriptLanguage": "scriptLanguage",
     *          "enable": "enable",
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
    @GetMapping(Route.SCRIPT_INFO)
    public R liteflowScriptInfo(LiteflowScript entity) {
        entity = liteflowScriptService.selectById(entity.getId());
        return R.ok(entity);
    }

    /**
     * @api {post} /liteflow/script/create 3. liteflow-脚本-创建
     * @apiGroup LITEFLOW_SCRIPT
     *
     * @apiVersion 0.0.1
     * @apiDescription liteflow-脚本-新增信息
     *
     * @apiParam {String} [scriptId] <code>body</code>脚本ID
     * @apiParam {String} [scriptName] <code>body</code>脚本名称
     * @apiParam {String} [scriptData] <code>body</code>脚本数据
     * @apiParam {String} [scriptType] <code>body</code>脚本类型
     * @apiParam {String} [scriptLanguage] <code>body</code>脚本语言
     * @apiParam {Integer} [enable] <code>body</code>可用状态
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "scriptId": "scriptId",
     *      "scriptName": "scriptName",
     *      "scriptData": "scriptData",
     *      "scriptType": "scriptType",
     *      "scriptLanguage": "scriptLanguage",
     *      "enable": "enable",
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
    @PostMapping(Route.SCRIPT_CREATE)
    public R liteflowScriptCreate(@RequestBody LiteflowScript entity) {
        paramCheck(entity);
        entity = liteflowScriptService.create(entity);
        return R.ok(entity);
    }



    /**
     * @api {post} /liteflow/script/update 4. liteflow-脚本-更新
     * @apiGroup LITEFLOW_SCRIPT
     *
     * @apiVersion 0.0.1
     * @apiDescription liteflow-脚本-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [scriptId] <code>body</code>脚本ID
     * @apiParam {String} [scriptName] <code>body</code>脚本名称
     * @apiParam {String} [scriptData] <code>body</code>脚本数据
     * @apiParam {String} [scriptType] <code>body</code>脚本类型
     * @apiParam {String} [scriptLanguage] <code>body</code>脚本语言
     * @apiParam {Integer} [enable] <code>body</code>可用状态
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "scriptId": "scriptId",
     *      "scriptName": "scriptName",
     *      "scriptData": "scriptData",
     *      "scriptType": "scriptType",
     *      "scriptLanguage": "scriptLanguage",
     *      "enable": "enable",
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
    @PostMapping(Route.SCRIPT_UPDATE)
    public R liteflowScriptUpdate(@RequestBody LiteflowScript entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        paramCheck(entity);
        entity = liteflowScriptService.update(entity);
        return R.ok(entity);
    }


    /**
     * @api {post} /liteflow/script/remove 5. liteflow-脚本-删除
     * @apiGroup LITEFLOW_SCRIPT
     *
     * @apiVersion 0.0.1
     * @apiDescription liteflow-脚本-删除
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
    @PostMapping(Route.SCRIPT_REMOVE)
    public R liteflowScriptRemove(@RequestBody LiteflowScript entity) {
        org.springframework.util.Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        liteflowScriptService.deleteById(entity);
        return R.ok(1);
    }



    private static void paramCheck(LiteflowScript entity) {
        if (entity == null) {
            throw ValidationException.of("entity 不能为空");
        }
        Assert.notNull(entity.getScriptId(), "scriptId 不能为空");
        Assert.notNull(entity.getScriptName(), "scriptName 不能为空");
        if (entity.getEnable() == null) {
            entity.setEnable(1);
        }
    }


}

