package com.wkclz.micro.msg.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.msg.pojo.dto.MsgUserRecordDto;
import com.wkclz.micro.msg.pojo.entity.MsgUserRecord;
import com.wkclz.micro.msg.service.MsgUserRecordService;
import org.apache.commons.collections4.CollectionUtils;
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
 * @table msg_user_record (用户消息记录) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
public class PersonalUserRecordRest {

    @Autowired
    private MsgUserRecordService msgUserRecordService;



    /**
     * @api {get} /personal/msg/list 11. 个人消息列表(最多99,展示为99+)
     * @apiGroup MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 用户消息记录-获取分页
     *
     * @apiParam {String} [noticeNo] <code>param</code>消息编码
     * @apiParam {Integer} [readStatus] <code>param</code>阅读状态
     * @apiParam {String} [title] <code>param</code>标题(模糊查询)
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [userCode] 用户编码
     * @apiSuccess {String} [noticeNo] 消息编码
     * @apiSuccess {Integer} [readStatus] 阅读状态
     * @apiSuccess {Date} [readTime] 阅读时间
     * @apiSuccess {String} [sender] 发送人用户编码
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *             {
     *                 "id": "id",
     *                 "userCode": "userCode",
     *                 "noticeNo": "noticeNo",
     *                 "readStatus": "readStatus",
     *                 "readTime": "readTime",
     *                 "updateTime": "updateTime",
     *                 "sender": "sender"
     *             },
     *             ...
     *         ]
     * }
     *
     */
    @GetMapping(Route.PERSONAL_MSG_LIST)
    public R personalMsgList(MsgUserRecordDto dto) {
        dto.setUserCode(SessionHelper.getUserCode());
        if (dto.getSize() == null) {
            dto.setSize(100L);
        }
        List<MsgUserRecordDto> list = msgUserRecordService.getPersonalRecordList(dto);
        return R.ok(list);
    }


    /**
     * @api {get} /personal/msg/page 12. 个人消息分页
     * @apiGroup MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 用户消息记录-获取分页
     *
     * @apiParam {String} [noticeNo] <code>param</code>消息编码
     * @apiParam {Integer} [readStatus] <code>param</code>阅读状态
     * @apiParam {Date} [readTime] <code>param</code>阅读时间
     * @apiParam {String} [title] <code>param</code>标题(模糊查询)
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [userCode] 用户编码
     * @apiSuccess {String} [noticeNo] 消息编码
     * @apiSuccess {Integer} [readStatus] 阅读状态
     * @apiSuccess {Date} [readTime] 阅读时间
     * @apiSuccess {String} [sender] 发送人用户编码
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "userCode": "userCode",
     *                 "noticeNo": "noticeNo",
     *                 "readStatus": "readStatus",
     *                 "readTime": "readTime",
     *                 "sender": "sender"
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
    @GetMapping(Route.PERSONAL_MSG_PAGE)
    public R personalMsgPage(MsgUserRecordDto dto) {
        dto.setUserCode(SessionHelper.getUserCode());
        PageData<MsgUserRecordDto> page = msgUserRecordService.getPersonalRecordPage(dto);
        return R.ok(page);
    }


    /**
     * @api {get} /personal/msg/info 13. 个人消息详情(阅读)
     * @apiGroup MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 用户消息记录-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [userCode] 用户编码
     * @apiSuccess {String} [noticeNo] 消息编码
     * @apiSuccess {Integer} [readStatus] 阅读状态
     * @apiSuccess {Date} [readTime] 阅读时间
     * @apiSuccess {Date} [updateTime] 更新时间
     * @apiSuccess {String} [title] 通知标题
     * @apiSuccess {String} [content] 通知正文
     * @apiSuccess {String} [extUrl] 扩展URL
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *          "id": "id",
     *          "userCode": "userCode",
     *          "noticeNo": "noticeNo",
     *          "readStatus": "readStatus",
     *          "readTime": "readTime",
     *          "updateTime": "updateTime",
     *          "title": "title",
     *          "content": "content",
     *          "extUrl": "extUrl"
     *     }
     * }
     *
     */
    @GetMapping(Route.PERSONAL_MSG_INFO)
    public R personalMsgInfo(MsgUserRecordDto dto) {
        Assert.notNull(dto.getNoticeNo(), "noticeNo 不能为空！");
        dto = msgUserRecordService.getNotice(dto.getNoticeNo());
        return R.ok(dto);
    }



    /**
     * @api {post} /personal/msg/readed 14. 个人消息，批量标注已读
     * @apiGroup MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 用户消息记录-删除
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
    @PostMapping(Route.PERSONAL_MSG_READED)
    public R personalMsgReaded(@RequestBody MsgUserRecord entity) {
        if (entity.getId() == null && CollectionUtils.isEmpty(entity.getIds())) {
            return R.error("请传 id 或者 ids");
        }
        Integer count = msgUserRecordService.userMarkRecodeReaded(entity);
        return R.ok(count);
    }

}

