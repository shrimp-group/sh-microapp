package com.wkclz.micro.msg.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.msg.pojo.dto.MsgNotificationDto;
import com.wkclz.micro.msg.pojo.entity.MsgNotification;
import com.wkclz.micro.msg.pojo.entity.MsgUserRecord;
import com.wkclz.micro.msg.service.MsgNotificationService;
import com.wkclz.micro.msg.service.MsgUserRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_notification (消息通知) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class ManagerNotificationRest {

    @Autowired
    private MsgUserRecordService msgUserRecordService;
    @Autowired
    private MsgNotificationService msgNotificationService;


    /**
     * @api {get} /micro-msg/manager/page 01. 管理员消息分页
     * @apiGroup MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 消息通知-获取分页
     *
     * @apiParam {String} [noticeNo] <code>param</code>消息编码
     * @apiParam {String} [userCode] <code>param</code>通知发送人
     * @apiParam {String} [title] <code>param</code>通知标题(模糊搜索)
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [noticeNo] 消息编码
     * @apiSuccess {String} [userCode] 通知发送人
     * @apiSuccess {String} [title] 通知标题
     * @apiSuccess {String} [extUrl] 扩展URL
     * @apiSuccess {Integer} [recordCount] 消息发送人数
     * @apiSuccess {Integer} [readCount] 消息阅读人数
     *
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "noticeNo": "noticeNo",
     *                 "userCode": "userCode",
     *                 "title": "title",
     *                 "extUrl": "extUrl",
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
    @GetMapping(Route.MANAGER_MSG_PAGE)
    public R managerMsgPage(MsgNotificationDto entity) {
        PageData<MsgNotificationDto> page = msgNotificationService.getNotificationPage(entity);
        return R.ok(page);
    }


    /**
     * @api {post} /micro-msg/manager/sent 02. 管理员消息发布
     * @apiGroup MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 消息通知-新增信息
     *
     * @apiParam {String} title <code>body</code>通知标题
     * @apiParam {String} content <code>body</code>通知正文
     * @apiParam {String} [extUrl] <code>body</code>扩展URL
     * @apiParam {String} sentToUser <code>body</code>发送目标
     * @apiParam {String[]} sentToUsers <code>body</code>发送目标s
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "noticeNo": "noticeNo",
     *      "userCode": "userCode",
     *      "title": "title",
     *      "content": "content",
     *      "extUrl": "extUrl",
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
    @PostMapping(Route.MANAGER_MSG_SENT)
    public R managerMsgSent(@RequestBody MsgNotificationDto dto) {
        Assert.notNull(dto.getTitle(), "title 不能为空");
        Assert.notNull(dto.getContent(), "content 不能为空");
        dto.setUserCode(SessionHelper.getUserCode());
        if (StringUtils.isBlank(dto.getSentToUser()) && CollectionUtils.isEmpty(dto.getSentToUsers())) {
            return R.error("请指定发送用户，个人或多人");
        }
        Integer rt = msgNotificationService.createNotification(dto);
        return R.ok(rt);
    }


    /**
     * @api {get} /micro-msg/manager/info 03. 管理员消息详情
     * @apiGroup MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 消息通知-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [noticeNo] 消息编码
     * @apiSuccess {String} [userCode] 通知发送人
     * @apiSuccess {String} [title] 通知标题
     * @apiSuccess {String} [content] 通知正文
     * @apiSuccess {String} [extUrl] 扩展URL
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
     *          "noticeNo": "noticeNo",
     *          "userCode": "userCode",
     *          "title": "title",
     *          "content": "content",
     *          "extUrl": "extUrl",
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
    @GetMapping(Route.MANAGER_MSG_INFO)
    public R managerMsgInfo(MsgNotification entity) {
        entity = msgNotificationService.selectById(entity.getId());
        return R.ok(entity);
    }


    /**
     * @api {get} /micro-msg/manager/record/page 04. 管理员消息阅读记录
     * @apiGroup MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 消息通知-获取分页
     *
     * @apiParam {String} [noticeNo] <code>param</code>消息编码
     * @apiParam {String} [userCode] <code>param</code>接收人用户名
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [userCode] 用户名
     * @apiSuccess {String} [noticeNo] 消息编码
     * @apiSuccess {Integer} [readStatus] 阅读状态
     * @apiSuccess {Date} [readTime] 阅读时间
     * @apiSuccess {Integer} [showTimes] 消息展示次数
     *
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "noticeNo": "noticeNo",
     *                 "userCode": "userCode",
     *                 "title": "title",
     *                 "extUrl": "extUrl",
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
    @GetMapping(Route.MANAGER_MSG_RECORD_PAGE)
    public R managerMsgRecordPage(MsgUserRecord entity) {
        Assert.notNull(entity.getNoticeNo(), "noticeNo 不能为空");
        PageData<MsgUserRecord> page = msgUserRecordService.selectPage(entity);
        return R.ok(page);
    }

}

