package com.wkclz.micro.fun.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.fun.engine.ScriptEngine;
import com.wkclz.micro.fun.engine.ScriptService;
import com.wkclz.micro.fun.pojo.dto.FunFunctionDto;
import com.wkclz.micro.fun.pojo.entity.FunFunction;
import com.wkclz.micro.fun.service.FunFunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_function (函数-函数体) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
public class FunFunctionRest {

    @Autowired
    private ScriptService scriptService;
    @Autowired
    private FunFunctionService funFunctionService;

    /**
     * @api {get} /fun/function/page 1. 函数体-分页
     * @apiGroup FUN_FUNCTION
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数-函数体-获取分页
     *
     * @apiParam {String} [categoryCode] <code>param</code>分类编码
     * @apiParam {String} [funCode] <code>param</code>函数编码
     * @apiParam {String} [funName] <code>param</code>函数名称
     * @apiParam {String} [funLanguage] <code>param</code>函数语言
     * @apiParam {Integer} [visible] <code>param</code>可见1/0
     * @apiParam {Integer} [defaultFlag] <code>param</code>内置
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [categoryCode] 分类编码
     * @apiSuccess {String} [funCode] 函数编码
     * @apiSuccess {String} [funName] 函数名称
     * @apiSuccess {String} [funParams] 参数列表
     * @apiSuccess {String} [funLanguage] 函数语言
     * @apiSuccess {String} [funReturn] 返回类型
     * @apiSuccess {String} [funMockData] 模拟数据
     * @apiSuccess {Integer} [visible] 可见1/0
     * @apiSuccess {Integer} [defaultFlag] 内置
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "categoryCode": "categoryCode",
     *                 "funCode": "funCode",
     *                 "funName": "funName",
     *                 "funParams": "funParams",
     *                 "funLanguage": "funLanguage",
     *                 "funReturn": "funReturn",
     *                 "funMockData": "funMockData",
     *                 "visible": "visible",
     *                 "defaultFlag": "defaultFlag",
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
    @GetMapping(Route.FUN_FUNCTION_PAGE)
    public R funFunctionPage(FunFunctionDto dto) {
        PageData<FunFunctionDto> page = funFunctionService.getFunctionPage(dto);
        return R.ok(page);
    }

    /**
     * @api {get} /fun/function/info 2. 函数体-详情
     * @apiGroup FUN_FUNCTION
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数-函数体-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [categoryCode] 分类编码
     * @apiSuccess {String} [funCode] 函数编码
     * @apiSuccess {String} [funName] 函数名称
     * @apiSuccess {String} [funParams] 参数列表
     * @apiSuccess {String} [funLanguage] 函数语言
     * @apiSuccess {String} [funBody] 函数体
     * @apiSuccess {String} [funReturn] 返回类型
     * @apiSuccess {String} [funDesc] 函数说明
     * @apiSuccess {String} [funMockData] 模拟数据
     * @apiSuccess {Integer} [visible] 可见1/0
     * @apiSuccess {Integer} [defaultFlag] 内置
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
     *          "categoryCode": "categoryCode",
     *          "funCode": "funCode",
     *          "funName": "funName",
     *          "funParams": "funParams",
     *          "funLanguage": "funLanguage",
     *          "funBody": "funBody",
     *          "funReturn": "funReturn",
     *          "funDesc": "funDesc",
     *          "funMockData": "funMockData",
     *          "visible": "visible",
     *          "defaultFlag": "defaultFlag",
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
    @GetMapping(Route.FUN_FUNCTION_INFO)
    public R funFunctionInfo(FunFunction entity) {
        entity = funFunctionService.selectById(entity.getId());
        return R.ok(entity);
    }

    /**
     * @api {post} /fun/function/create 3. 函数体-创建
     * @apiGroup FUN_FUNCTION
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数-函数体-新增信息
     *
     * @apiParam {String} [categoryCode] <code>body</code>分类编码
     * @apiParam {String} [funCode] <code>body</code>函数编码
     * @apiParam {String} [funName] <code>body</code>函数名称
     * @apiParam {String} [funParams] <code>body</code>参数列表
     * @apiParam {String} [funLanguage] <code>body</code>函数语言
     * @apiParam {String} [funBody] <code>body</code>函数体
     * @apiParam {String} [funReturn] <code>body</code>返回类型
     * @apiParam {String} [funDesc] <code>body</code>函数说明
     * @apiParam {String} [funMockData] <code>body</code>模拟数据
     * @apiParam {Integer} [visible] <code>body</code>可见1/0
     * @apiParam {Integer} [defaultFlag] <code>body</code>内置
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "categoryCode": "categoryCode",
     *      "funCode": "funCode",
     *      "funName": "funName",
     *      "funParams": "funParams",
     *      "funLanguage": "funLanguage",
     *      "funBody": "funBody",
     *      "funReturn": "funReturn",
     *      "funDesc": "funDesc",
     *      "funMockData": "funMockData",
     *      "visible": "visible",
     *      "defaultFlag": "defaultFlag",
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
    @PostMapping(Route.FUN_FUNCTION_CREATE)
    public R funFunctionCreate(@RequestBody FunFunction entity) {
        paramCheck(entity);
        entity = funFunctionService.create(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /fun/function/update 4. 函数体-修改
     * @apiGroup FUN_FUNCTION
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数-函数体-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [categoryCode] <code>body</code>分类编码
     * @apiParam {String} [funCode] <code>body</code>函数编码
     * @apiParam {String} [funName] <code>body</code>函数名称
     * @apiParam {String} [funParams] <code>body</code>参数列表
     * @apiParam {String} [funLanguage] <code>body</code>函数语言
     * @apiParam {String} [funBody] <code>body</code>函数体
     * @apiParam {String} [funReturn] <code>body</code>返回类型
     * @apiParam {String} [funDesc] <code>body</code>函数说明
     * @apiParam {String} [funMockData] <code>body</code>模拟数据
     * @apiParam {Integer} [visible] <code>body</code>可见1/0
     * @apiParam {Integer} [defaultFlag] <code>body</code>内置
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "categoryCode": "categoryCode",
     *      "funCode": "funCode",
     *      "funName": "funName",
     *      "funParams": "funParams",
     *      "funLanguage": "funLanguage",
     *      "funBody": "funBody",
     *      "funReturn": "funReturn",
     *      "funDesc": "funDesc",
     *      "funMockData": "funMockData",
     *      "visible": "visible",
     *      "defaultFlag": "defaultFlag",
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
    @PostMapping(Route.FUN_FUNCTION_UPDATE)
    public R funFunctionUpdate(@RequestBody FunFunction entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        paramCheck(entity);
        entity = funFunctionService.update(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /fun/function/remove 5. 函数体-删除
     * @apiGroup FUN_FUNCTION
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数-函数体-删除
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
    @PostMapping(Route.FUN_FUNCTION_REMOVE)
    public R funFunctionRemove(@RequestBody FunFunction entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        funFunctionService.deleteById(entity);
        return R.ok(1);
    }

    /**
     * @api {get} /fun/function/options 6. 函数体-选项
     * @apiGroup FUN_FUNCTION
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数-函数体-获取分页
     *
     * @apiParam {String} [categoryCode] <code>param</code>分类编码
     * @apiParam {String} [funCode] <code>param</code>函数编码
     * @apiParam {String} [funLanguage] <code>param</code>函数语言
     * @apiParam {Integer} [defaultFlag] <code>param</code>内置
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [categoryCode] 分类编码
     * @apiSuccess {String} [funCode] 函数编码
     * @apiSuccess {String} [funName] 函数名称
     * @apiSuccess {String} [funParams] 参数列表
     * @apiSuccess {String} [funLanguage] 函数语言
     * @apiSuccess {String} [funReturn] 返回类型
     * @apiSuccess {Integer} [defaultFlag] 内置
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *         {
     *             "id": "id",
     *             "categoryCode": "categoryCode",
     *             "funCode": "funCode",
     *             "funName": "funName",
     *             "funParams": "funParams",
     *             "funLanguage": "funLanguage",
     *             "funReturn": "funReturn",
     *             "funMockData": "funMockData",
     *             "visible": "visible",
     *             "defaultFlag": "defaultFlag",
     *             "sort": "sort",
     *             "createTime": "createTime",
     *             "createBy": "createBy",
     *             "updateTime": "updateTime",
     *             "updateBy": "updateBy",
     *             "remark": "remark",
     *             "version": "version",
     *         },
     *         ...
     *     ]
     * }
     *
     */
    @GetMapping(Route.FUN_FUNCTION_OPTIONS)
    public R funFunctionOptions(FunFunctionDto dto) {
        List<FunFunctionDto> list = funFunctionService.getFunctionOption(dto);
        return R.ok(list);
    }


    @PostMapping(Route.FUN_FUNCTION_TEST)
    public R funFunctionTest(@RequestBody FunFunctionDto dto) {
        Assert.notNull(dto.getFunCode(), "funCode 不能为空");
        Assert.notNull(dto.getFunName(), "funName 不能为空");
        Assert.notNull(dto.getFunLanguage(), "funLanguage 不能为空");
        Assert.notNull(dto.getFunBody(), "funBody 不能为空");
        Assert.notNull(dto.getFunReturn(), "funReturn 不能为空");
        ScriptEngine engine = scriptService.getEngineTest(dto);

        Object rt = engine.exec(dto.getParam());
        return R.ok(rt);
    }



    private static void paramCheck(FunFunction entity) {
        Assert.notNull(entity.getCategoryCode(), "categoryCode 不能为空");
        Assert.notNull(entity.getFunCode(), "funCode 不能为空");
        Assert.notNull(entity.getFunName(), "funName 不能为空");
        Assert.notNull(entity.getFunLanguage(), "funLanguage 不能为空");
        Assert.notNull(entity.getFunBody(), "funBody 不能为空");
        Assert.notNull(entity.getFunReturn(), "funReturn 不能为空");

        if (entity.getVisible() == null) {
            entity.setVisible(1);
        }
        if (entity.getDefaultFlag() == null) {
            entity.setDefaultFlag(0);
        }
        if (entity.getSort() == null) {
            entity.setSort(99);
        }

    }

}

