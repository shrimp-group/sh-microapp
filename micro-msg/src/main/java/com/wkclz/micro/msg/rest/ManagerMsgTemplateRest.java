package com.wkclz.micro.msg.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.msg.bean.entity.MsgTemplate;
import com.wkclz.micro.msg.service.MsgTemplateService;
import jakarta.annotation.Resource;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table msg_template (消息模板) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class ManagerMsgTemplateRest {

    @Resource
    private MsgTemplateService msgTemplateService;

    /**
     * @api {get} /manager/template/page 1. 消息模板-获取分页
     * @apiGroup MSG_TEMPLATE
     *
     * @apiVersion 0.0.1
     * @apiDescription 消息模板-获取分页
     *
     * @apiParam {String} [templateCode] <code>param</code>模板编码
     * @apiParam {String} [templateName] <code>param</code>模板名称
     * @apiParam {String} [title] <code>param</code>消息标题
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [templateCode] 模板编码
     * @apiSuccess {String} [templateName] 模板名称
     * @apiSuccess {String} [title] 消息标题
     * @apiSuccess {Integer} [sort] 排序
     * @apiSuccess {LocalDateTime} [createTime] 创建时间
     * @apiSuccess {String} [createBy] 创建人
     * @apiSuccess {LocalDateTime} [updateTime] 更新时间
     * @apiSuccess {String} [updateBy] 更新人
     * @apiSuccess {String} [remark] 备注
     * @apiSuccess {Integer} [version] 版本号
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
     *                 "title": "title",
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
    @GetMapping(Route.MANAGER_TEMPLATE_PAGE)
    public R msgTemplatePage(MsgTemplate entity) {
        PageData<MsgTemplate> page = msgTemplateService.selectPage(entity);
        return R.ok(page);
    }



    /**
     * @api {get} /manager/template/info 2. 消息模板-获取详情
     * @apiGroup MSG_TEMPLATE
     *
     * @apiVersion 0.0.1
     * @apiDescription 消息模板-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [templateCode] 模板编码
     * @apiSuccess {String} [templateName] 模板名称
     * @apiSuccess {String} [title] 消息标题
     * @apiSuccess {String} [content] 消息内容
     * @apiSuccess {Integer} [sort] 排序
     * @apiSuccess {LocalDateTime} [createTime] 创建时间
     * @apiSuccess {String} [createBy] 创建人
     * @apiSuccess {LocalDateTime} [updateTime] 更新时间
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
     *          "title": "title",
     *          "content": "content",
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
    @GetMapping(Route.MANAGER_TEMPLATE_INFO)
    public R msgTemplateInfo(MsgTemplate entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        entity = msgTemplateService.selectById(entity.getId());
        return R.ok(entity);
    }




    /**
     * @api {post} /manager/template/create 3. 消息模板-创建
     * @apiGroup MSG_TEMPLATE
     *
     * @apiVersion 0.0.1
     * @apiDescription 消息模板-新增信息
     *
     * @apiParam {String} [templateCode] <code>body</code>模板编码
     * @apiParam {String} [templateName] <code>body</code>模板名称
     * @apiParam {String} [title] <code>body</code>消息标题
     * @apiParam {String} [content] <code>body</code>消息内容
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "templateCode": "templateCode",
     *      "templateName": "templateName",
     *      "title": "title",
     *      "content": "content",
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
    @PostMapping(Route.MANAGER_TEMPLATE_CREATE)
    public R msgTemplateCreate(@RequestBody MsgTemplate entity) {
        entity = msgTemplateService.create(entity);
        return R.ok(entity);
    }


    /**
     * @api {post} /manager/template/update 4. 消息模板-更新
     * @apiGroup MSG_TEMPLATE
     *
     * @apiVersion 0.0.1
     * @apiDescription 消息模板-更新信息
     *
     * @apiParam {String} [templateCode] <code>body</code>模板编码
     * @apiParam {String} [templateName] <code>body</code>模板名称
     * @apiParam {String} [title] <code>body</code>消息标题
     * @apiParam {String} [content] <code>body</code>消息内容
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [updateBy] <code>body</code>更新人
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "templateCode": "templateCode",
     *      "templateName": "templateName",
     *      "title": "title",
     *      "content": "content",
     *      "sort": "sort",
     *      "updateBy": "updateBy",
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
    @PostMapping(Route.MANAGER_TEMPLATE_UPDATE)
    public R msgTemplateUpdate(@RequestBody MsgTemplate entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        entity = msgTemplateService.update(entity);
        return R.ok(entity);
    }


    /**
     * @api {post} /manager/template/remove 6. 消息模板-删除
     * @apiGroup MSG_TEMPLATE
     *
     * @apiVersion 0.0.1
     * @apiDescription 消息模板-删除
     *
     * @apiParam {Long} [id] <code>body</code>数据id
     * @apiParam {Long[]} [ids] <code>body</code>数据ids(当支持批量删除时)
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
    @PostMapping(Route.MANAGER_TEMPLATE_REMOVE)
    public R msgTemplateRemove(@RequestBody MsgTemplate entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        msgTemplateService.deleteById(entity);
        return R.ok(1);
    }


}

