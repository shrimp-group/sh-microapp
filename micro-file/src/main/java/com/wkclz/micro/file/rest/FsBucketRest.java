package com.wkclz.micro.file.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.file.pojo.entity.MdmFsBucket;
import com.wkclz.micro.file.service.MdmFsBucketService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_fs_bucket (Bucket管理) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class FsBucketRest {

    @Autowired
    private MdmFsBucketService mdmFsBucketService;

    /**
     * @api {get} /micro-fs/bucket/page 1. Bucket管理-获取分页
     * @apiGroup FS
     *
     * @apiVersion 0.0.1
     * @apiDescription Bucket管理-获取分页
     *
     * @apiParam {String} [bucket] <code>param</code>Bucket
     * @apiParam {String} [ossSp] <code>param</code>OSS服务商
     * @apiParam {String} [endpointInner] <code>param</code>内网Endpoint
     * @apiParam {String} [endpointOuter] <code>param</code>外网Endpoint
     * @apiParam {String} [region] <code>param</code>区域
     * @apiParam {Integer} [defaultFlag] <code>param</code>默认标识
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [bucket] Bucket
     * @apiSuccess {String} [ossSp] OSS服务商
     * @apiSuccess {String} [endpointInner] 内网Endpoint
     * @apiSuccess {String} [endpointOuter] 外网Endpoint
     * @apiSuccess {String} [region] 区域
     * @apiSuccess {String} [accessKey] Access Key
     * @apiSuccess {Integer} [defaultFlag] 默认标识
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "tenantCode": "tenantCode",
     *                 "bucket": "bucket",
     *                 "ossSp": "ossSp",
     *                 "endpointInner": "endpointInner",
     *                 "endpointOuter": "endpointOuter",
     *                 "region": "region",
     *                 "accessKey": "accessKey",
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
    @GetMapping(Route.BUCKET_PAGE)
    public R mdmFsBucketPage(MdmFsBucket entity) {
        PageData page = mdmFsBucketService.getPage(entity);
        return R.ok(page);
    }

    /**
     * @api {get} /micro-fs/bucket/info 2. Bucket管理-获取详情
     * @apiGroup FS
     *
     * @apiVersion 0.0.1
     * @apiDescription Bucket管理-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [bucket] Bucket
     * @apiSuccess {String} [ossSp] OSS服务商
     * @apiSuccess {String} [endpointInner] 内网Endpoint
     * @apiSuccess {String} [endpointOuter] 外网Endpoint
     * @apiSuccess {String} [region] 区域
     * @apiSuccess {String} [accessKey] Access Key
     * @apiSuccess {Integer} [defaultFlag] 默认标识
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
     *          "tenantCode": "tenantCode",
     *          "bucket": "bucket",
     *          "ossSp": "ossSp",
     *          "endpointInner": "endpointInner",
     *          "endpointOuter": "endpointOuter",
     *          "region": "region",
     *          "accessKey": "accessKey",
     *          "defaultFlag": "defaultFlag",
     *          "sort": "sort",
     *          "createTime": "createTime",
     *          "createBy": "createBy",
     *          "updateTime": "updateTime",
     *          "updateBy": "updateBy",
     *          "remark": "remark",
     *          "version": "version",
     *     }
     * }
     *
     */
    @GetMapping(Route.BUCKET_INFO)
    public R mdmFsBucketInfo(MdmFsBucket entity) {
        entity = mdmFsBucketService.getInfo(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /micro-fs/bucket/create 3. Bucket管理-创建
     * @apiGroup FS
     *
     * @apiVersion 0.0.1
     * @apiDescription Bucket管理-新增信息
     *
     * @apiParam {String} [tenantCode] <code>body</code>租户编码
     * @apiParam {String} [bucket] <code>body</code>Bucket
     * @apiParam {String} [ossSp] <code>body</code>OSS服务商
     * @apiParam {String} [endpointInner] <code>body</code>内网Endpoint
     * @apiParam {String} [endpointOuter] <code>body</code>外网Endpoint
     * @apiParam {String} [region] <code>body</code>区域
     * @apiParam {String} [accessKey] <code>body</code>Access Key
     * @apiParam {String} [secretKey] <code>body</code>Secret Key
     * @apiParam {Integer} [defaultFlag] <code>body</code>默认标识
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "tenantCode": "tenantCode",
     *      "bucket": "bucket",
     *      "ossSp": "ossSp",
     *      "endpointInner": "endpointInner",
     *      "endpointOuter": "endpointOuter",
     *      "region": "region",
     *      "accessKey": "accessKey",
     *      "secretKey": "secretKey",
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
    @PostMapping(Route.BUCKET_CREATE)
    public R mdmFsBucketCreate(@RequestBody MdmFsBucket entity) {
        Assert.notNull(entity.getBucket(), "bucket 不能为空");
        Assert.notNull(entity.getOssSp(), "ossSp 不能为空");
        Assert.notNull(entity.getEndpointInner(), "endpointInner 不能为空");
        Assert.notNull(entity.getEndpointOuter(), "endpointOuter 不能为空");
        Assert.notNull(entity.getAccessKey(), "accessKey 不能为空");
        if (entity.getDefaultFlag() == null || entity.getDefaultFlag() != 1) {
            entity.setDefaultFlag(0);
        }

        entity.setTenantCode(SessionHelper.getTenantCode());
        mdmFsBucketService.insert(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /micro-fs/bucket/update 4. Bucket管理-更新
     * @apiGroup FS
     *
     * @apiVersion 0.0.1
     * @apiDescription Bucket管理-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [tenantCode] <code>body</code>租户编码
     * @apiParam {String} [bucket] <code>body</code>Bucket
     * @apiParam {String} [ossSp] <code>body</code>OSS服务商
     * @apiParam {String} [endpointInner] <code>body</code>内网Endpoint
     * @apiParam {String} [endpointOuter] <code>body</code>外网Endpoint
     * @apiParam {String} [region] <code>body</code>区域
     * @apiParam {String} [accessKey] <code>body</code>Access Key
     * @apiParam {String} [secretKey] <code>body</code>Secret Key
     * @apiParam {Integer} [defaultFlag] <code>body</code>默认标识
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "tenantCode": "tenantCode",
     *      "bucket": "bucket",
     *      "endpoint": "endpoint",
     *      "accessKey": "accessKey",
     *      "secretKey": "secretKey",
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
    @PostMapping(Route.BUCKET_UPDATE)
    public R mdmFsBucketUpdate(@RequestBody MdmFsBucket entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());

        Assert.notNull(entity.getBucket(), "bucket 不能为空");
        Assert.notNull(entity.getOssSp(), "ossSp 不能为空");
        Assert.notNull(entity.getEndpointInner(), "endpointInner 不能为空");
        Assert.notNull(entity.getEndpointOuter(), "endpointOuter 不能为空");
        Assert.notNull(entity.getAccessKey(), "accessKey 不能为空");
        if (entity.getDefaultFlag() == null || entity.getDefaultFlag() != 1) {
            entity.setDefaultFlag(0);
        }
        if (StringUtils.isBlank(entity.getSecretKey())) {
            entity.setSecretKey(null);
        }
        entity = mdmFsBucketService.update(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /micro-fs/bucket/remove 6. Bucket管理-删除
     * @apiGroup FS
     *
     * @apiVersion 0.0.1
     * @apiDescription Bucket管理-删除
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
    @PostMapping(Route.BUCKET_REMOVE)
    public R mdmFsBucketRemove(@RequestBody MdmFsBucket entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        mdmFsBucketService.remove(entity);
        return R.ok(1);
    }


    /**
     * @api {get} /micro-fs/bucket/options 6. 文件系统-Bucket-选项
     * @apiGroup FS
     *
     * @apiVersion 0.0.1
     * @apiDescription Bucket管理-获取分页
     *
     * @apiParam {String} [ossSp] <code>param</code>OSS服务商
     *
     * @apiParamExample {param} 请求样例:
     * ?ossSp=xxx
     *
     * @apiSuccess {String} [tenantCode] 租房编码
     * @apiSuccess {String} [bucket] Bucket
     * @apiSuccess {String} [ossSp] OSS服务商
     * @apiSuccess {Integer} [defaultFlag] 默认标识
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *         {
     *             "tenantCode": "tenantCode",
     *             "bucket": "bucket",
     *             "ossSp": "ossSp",
     *             "defaultFlag": "defaultFlag",
     *         },
     *         ...
     *     ],
     * }
     *
     */
    @GetMapping(Route.BUCKET_OPTIONS)
    public R fsBucketOptions(MdmFsBucket entity) {
        List<MdmFsBucket> options = mdmFsBucketService.getBucketOptions(entity);
        return R.ok(options);
    }

}

