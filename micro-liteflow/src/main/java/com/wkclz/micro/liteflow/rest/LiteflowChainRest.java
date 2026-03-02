package com.wkclz.micro.liteflow.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.liteflow.pojo.entity.LiteflowChain;
import com.wkclz.micro.liteflow.service.LiteflowChainService;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table liteflow_chain (liteflow-规则) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class LiteflowChainRest {

    @Autowired
    private LiteflowChainService liteflowChainService;

    /**
     * @api {get} /micro-liteflow/chain/page 1. liteflow-规则-获取分页
     * @apiGroup LITEFLOW_CHAIN
     *
     * @apiVersion 0.0.1
     * @apiDescription liteflow-规则-获取分页
     *
     * @apiParam {String} [chainName] <code>param</code>规则名称
     * @apiParam {String} [chainDesc] <code>param</code>规则描述
     * @apiParam {String} [namespace] <code>param</code>命名空间
     * @apiParam {Integer} [enable] <code>param</code>状态
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [chainName] 规则名称
     * @apiSuccess {String} [chainDesc] 规则描述
     * @apiSuccess {String} [namespace] 命名空间
     * @apiSuccess {Integer} [enable] 状态
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "chainName": "chainName",
     *                 "chainDesc": "chainDesc",
     *                 "namespace": "namespace",
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
    @GetMapping(Route.CHAIN_PAGE)
    public R liteflowChainPage(LiteflowChain entity) {
        PageData<LiteflowChain> page = liteflowChainService.getLiteflowChainPage(entity);
        return R.ok(page);
    }

    /**
     * @api {get} /micro-liteflow/chain/info 2. liteflow-规则-获取详情
     * @apiGroup LITEFLOW_CHAIN
     *
     * @apiVersion 0.0.1
     * @apiDescription liteflow-规则-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [chainName] 规则名称
     * @apiSuccess {String} [chainDesc] 规则描述
     * @apiSuccess {String} [elData] 规则数据
     * @apiSuccess {String} [route] 路由
     * @apiSuccess {String} [namespace] 命名空间
     * @apiSuccess {Integer} [enable] 状态
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
     *          "chainName": "chainName",
     *          "chainDesc": "chainDesc",
     *          "elData": "elData",
     *          "route": "route",
     *          "namespace": "namespace",
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
    @GetMapping(Route.CHAIN_INFO)
    public R liteflowChainInfo(LiteflowChain entity) {
        entity = liteflowChainService.selectById(entity.getId());
        return R.ok(entity);
    }

    /**
     * @api {post} /micro-liteflow/chain/create 3. liteflow-规则-创建
     * @apiGroup LITEFLOW_CHAIN
     *
     * @apiVersion 0.0.1
     * @apiDescription liteflow-规则-新增信息
     *
     * @apiParam {String} [chainName] <code>body</code>规则名称
     * @apiParam {String} [chainDesc] <code>body</code>规则描述
     * @apiParam {String} [elData] <code>body</code>规则数据
     * @apiParam {String} [route] <code>body</code>路由
     * @apiParam {String} [namespace] <code>body</code>命名空间
     * @apiParam {Integer} [enable] <code>body</code>状态
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "chainName": "chainName",
     *      "chainDesc": "chainDesc",
     *      "elData": "elData",
     *      "route": "route",
     *      "namespace": "namespace",
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
    @PostMapping(Route.CHAIN_CREATE)
    public R liteflowChainCreate(@RequestBody LiteflowChain entity) {
        paramCheck(entity);
        entity = liteflowChainService.create(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /micro-liteflow/chain/update 4. liteflow-规则-更新
     * @apiGroup LITEFLOW_CHAIN
     *
     * @apiVersion 0.0.1
     * @apiDescription liteflow-规则-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [chainName] <code>body</code>规则名称
     * @apiParam {String} [chainDesc] <code>body</code>规则描述
     * @apiParam {String} [elData] <code>body</code>规则数据
     * @apiParam {String} [route] <code>body</code>路由
     * @apiParam {String} [namespace] <code>body</code>命名空间
     * @apiParam {Integer} [enable] <code>body</code>状态
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "chainName": "chainName",
     *      "chainDesc": "chainDesc",
     *      "elData": "elData",
     *      "route": "route",
     *      "namespace": "namespace",
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
    @PostMapping(Route.CHAIN_UPDATE)
    public R liteflowChainUpdate(@RequestBody LiteflowChain entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        paramCheck(entity);
        entity = liteflowChainService.update(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /micro-liteflow/chain/remove 6. liteflow-规则-删除
     * @apiGroup LITEFLOW_CHAIN
     *
     * @apiVersion 0.0.1
     * @apiDescription liteflow-规则-删除
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
    @PostMapping(Route.CHAIN_REMOVE)
    public R liteflowChainRemove(@RequestBody LiteflowChain entity) {
        org.springframework.util.Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        liteflowChainService.deleteById(entity);
        return R.ok(1);
    }



    private static void paramCheck(LiteflowChain entity) {
        if (entity == null) {
            throw ValidationException.of("entity 不能为空");
        }
        Assert.notNull(entity.getChainName(), "chainName 不能为空");
        if (entity.getEnable() == null) {
            entity.setEnable(1);
        }
    }

}

