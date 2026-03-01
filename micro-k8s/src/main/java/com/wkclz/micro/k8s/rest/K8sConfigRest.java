package com.wkclz.micro.k8s.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.k8s.Route;
import com.wkclz.micro.k8s.bean.entity.K8sConfig;
import com.wkclz.micro.k8s.service.K8sConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table k8s_config (k8s配置) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class K8sConfigRest {

    @Autowired
    private K8sConfigService k8sConfigService;

    /**
     * @api {get} /config/page 1. k8s配置-获取分页
     * @apiGroup CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription k8s配置-获取分页
     *
     * @apiParam {String} [clusterName] <code>param</code>集群名称
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [clusterName] 集群名称
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "clusterName": "clusterName",
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
    @GetMapping(Route.CONFIG_PAGE)
    public R configPage(K8sConfig entity) {
        PageData<K8sConfig> page = k8sConfigService.getClusterPage(entity);
        return R.ok(page);
    }

    /**
     * @api {get} /config/info 2. k8s配置-获取详情
     * @apiGroup CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription k8s配置-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据Id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] id
     * @apiSuccess {String} [clusterName] 集群名称
     * @apiSuccess {String} [kubeConfig] 配置信息
     * @apiSuccess {Integer} [sort] 排序
     * @apiSuccess {Date} [createTime] 创建时间
     * @apiSuccess {String} [createBy] 创建人
     * @apiSuccess {Date} [updateTime] 更新时间
     * @apiSuccess {String} [updateBy] 更新人
     * @apiSuccess {String} [remark] 备注
     * @apiSuccess {Integer} [version] 版本号
     * @apiSuccess {Integer} [status] status
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *          "id": "id",
     *          "clusterName": "clusterName",
     *          "kubeConfig": "kubeConfig",
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
    @GetMapping(Route.CONFIG_INFO)
    public R configInfo(K8sConfig entity) {
        Assert.notNull(entity.getId(), "id不能为空");
        entity = k8sConfigService.selectById(entity.getId());
        return R.ok(entity);
    }

    /**
     * @api {post} /config/create 3. k8s配置-创建
     * @apiGroup CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription k8s配置-新增信息
     *
     * @apiParam {String} [clusterName] <code>body</code>集群名称
     * @apiParam {String} [kubeConfig] <code>body</code>配置信息
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {Date} [createTime] <code>body</code>创建时间
     * @apiParam {String} [createBy] <code>body</code>创建人
     * @apiParam {Date} [updateTime] <code>body</code>更新时间
     * @apiParam {String} [updateBy] <code>body</code>更新人
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} [version] <code>body</code>版本号
     * @apiParam {Integer} [status] <code>body</code>status
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "clusterName": "clusterName",
     *      "kubeConfig": "kubeConfig",
     *      "sort": "sort",
     *      "createTime": "createTime",
     *      "createBy": "createBy",
     *      "updateTime": "updateTime",
     *      "updateBy": "updateBy",
     *      "remark": "remark",
     *      "version": "version",
     *      "status": "status",
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.CONFIG_CREATE)
    public R configCreate(@RequestBody K8sConfig entity) {
        checkParam(entity);
        entity = k8sConfigService.create(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /config/update 4. k8s配置-更新
     * @apiGroup CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription k8s配置-更新信息
     *
     * @apiParam {Long} id <code>body</code>id
     * @apiParam {String} [clusterName] <code>body</code>集群名称
     * @apiParam {String} [kubeConfig] <code>body</code>配置信息
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {Date} [createTime] <code>body</code>创建时间
     * @apiParam {String} [createBy] <code>body</code>创建人
     * @apiParam {Date} [updateTime] <code>body</code>更新时间
     * @apiParam {String} [updateBy] <code>body</code>更新人
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} [version] <code>body</code>版本号
     * @apiParam {Integer} [status] <code>body</code>status
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "clusterName": "clusterName",
     *      "kubeConfig": "kubeConfig",
     *      "sort": "sort",
     *      "createTime": "createTime",
     *      "createBy": "createBy",
     *      "updateTime": "updateTime",
     *      "updateBy": "updateBy",
     *      "remark": "remark",
     *      "version": "version",
     *      "status": "status",
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.CONFIG_UPDATE)
    public R configUpdate(@RequestBody K8sConfig entity) {
        checkParam(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        entity = k8sConfigService.update(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /config/remove 6. k8s配置-删除
     * @apiGroup CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription k8s配置-删除
     *
     * @apiParam {Long} [id] <code>body</code> 主键 id
     *
     * @apiParamExample {json} 请求样例:
     * {
     *     "id": 1,
     *     "ids": [1]
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": 1
     * }
     *
     */
    @PostMapping(Route.CONFIG_REMOVE)
    public R configRemove(@RequestBody K8sConfig entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        k8sConfigService.deleteById(entity);
        return R.ok(1);
    }


    @GetMapping(Route.CONFIG_OPTIONS)
    public R configRemove() {
        List<String> clusterOptions = k8sConfigService.getClusterOptions();
        return R.ok(clusterOptions);
    }


    private void checkParam(K8sConfig entity) {
        if (entity == null) {
            throw ValidationException.of("entity can not be null");
        }
        Assert.notNull(entity.getClusterName(), "clusterName 不能为空");
        Assert.notNull(entity.getKubeConfig(), "kubeConfig 不能为空");

    }

}

