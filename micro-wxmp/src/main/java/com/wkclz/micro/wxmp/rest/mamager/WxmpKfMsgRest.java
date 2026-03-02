package com.wkclz.micro.wxmp.rest.mamager;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.wxmp.pojo.dto.WxmpKfMsgDto;
import com.wkclz.micro.wxmp.pojo.entity.WxmpKfMsg;
import com.wkclz.micro.wxmp.rest.Route;
import com.wkclz.micro.wxmp.service.WxmpKfMsgService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_kf_msg (公众号-客服消息) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class WxmpKfMsgRest {

    @Resource
    private WxmpKfMsgService wxmpKfMsgService;

    /**
     * @api {get} /micro-wxmp/kf/msg/page 1. 公众号-客服消息-获取分页
     * @apiGroup WXMP_KF_MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 公众号-客服消息-获取分页
     *
     * @apiParam {String} [tenantCode] <code>param</code>租户编码
     * @apiParam {String} [appId] <code>param</code>公众号appid
     * @apiParam {String} [msgType] <code>param</code>消息类型
     * @apiParam {String} [fromUser] <code>param</code>发送方
     * @apiParam {String} [toUser] <code>param</code>接收方
     * @apiParam {Long} [msgId] <code>param</code>消息ID
     * @apiParam {Date} [msgTime] <code>param</code>消息时间
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [appId] 公众号appid
     * @apiSuccess {String} [msgType] 消息类型
     * @apiSuccess {String} [fromUser] 发送方
     * @apiSuccess {String} [toUser] 接收方
     * @apiSuccess {Long} [msgId] 消息ID
     * @apiSuccess {Date} [msgTime] 消息时间
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "tenantCode": "tenantCode",
     *                 "appId": "appId",
     *                 "msgType": "msgType",
     *                 "fromUser": "fromUser",
     *                 "toUser": "toUser",
     *                 "msgId": "msgId",
     *                 "msgTime": "msgTime",
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
    @GetMapping(Route.WXMP_KF_MSG_PAGE)
    public R wxmpKfMsgPage(WxmpKfMsgDto dto) {
        PageData<WxmpKfMsgDto> page = wxmpKfMsgService.getKfMsgPage(dto);
        return R.ok(page);
    }

    /**
     * @api {get} /micro-wxmp/kf/msg/info 2. 公众号-客服消息-获取详情
     * @apiGroup WXMP_KF_MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 公众号-客服消息-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [appId] 公众号appid
     * @apiSuccess {String} [msgType] 消息类型
     * @apiSuccess {String} [fromUser] 发送方
     * @apiSuccess {String} [toUser] 接收方
     * @apiSuccess {String} [content] 消息内容
     * @apiSuccess {Long} [msgId] 消息ID
     * @apiSuccess {Date} [msgTime] 消息时间
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
     *          "appId": "appId",
     *          "msgType": "msgType",
     *          "fromUser": "fromUser",
     *          "toUser": "toUser",
     *          "content": "content",
     *          "msgId": "msgId",
     *          "msgTime": "msgTime",
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
    @GetMapping(Route.WXMP_KF_MSG_INFO)
    public R wxmpKfMsgInfo(WxmpKfMsg entity) {
        entity = wxmpKfMsgService.selectById(entity.getId());
        return R.ok(entity);
    }

}
