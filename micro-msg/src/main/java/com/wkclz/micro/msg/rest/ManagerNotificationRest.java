package com.wkclz.micro.msg.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.msg.bean.dto.MsgNotificationDto;
import com.wkclz.micro.msg.bean.dto.MsgUserRecordDto;
import com.wkclz.micro.msg.bean.entity.MsgNotification;
import com.wkclz.micro.msg.bean.entity.MsgUserRecord;
import com.wkclz.micro.msg.bean.req.MsgNotificationInfoReq;
import com.wkclz.micro.msg.bean.req.MsgNotificationPageReq;
import com.wkclz.micro.msg.bean.req.MsgNotificationRecordPageReq;
import com.wkclz.micro.msg.bean.req.MsgNotificationSentReq;
import com.wkclz.micro.msg.bean.resp.MsgNotificationPageResp;
import com.wkclz.micro.msg.bean.resp.MsgNotificationRecordPageResp;
import com.wkclz.micro.msg.bean.resp.MsgNotificationResp;
import com.wkclz.micro.msg.service.MsgNotificationService;
import com.wkclz.micro.msg.service.MsgUserRecordService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_notification (消息通知) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "2.消息通知", description = "消息通知管理接口")
@RestController
@RequestMapping(Route.PREFIX)
public class ManagerNotificationRest {

    @Autowired
    private MsgUserRecordService msgUserRecordService;
    @Autowired
    private MsgNotificationService msgNotificationService;

    @Operation(summary = "1.消息通知-分页查询", description = "根据条件分页查询消息通知列表")
    @GetMapping(Route.MANAGER_NOTIFICATION_PAGE)
    public R<PageData<MsgNotificationPageResp>> managerNotificationPage(MsgNotificationPageReq req) {
        MsgNotificationDto dto = BeanUtil.cp(req, MsgNotificationDto.class);
        PageData<MsgNotificationDto> page = msgNotificationService.getNotificationPage(dto);
        PageData<MsgNotificationPageResp> newPage = page.convert(MsgNotificationPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.消息通知-发布", description = "发布消息通知")
    @PostMapping(Route.MANAGER_NOTIFICATION_SENT)
    public R<Integer> managerNotificationSent(@RequestBody MsgNotificationSentReq req) {
        if (StringUtils.isBlank(req.getSentToUser()) && CollectionUtils.isEmpty(req.getSentToUsers())) {
            return R.error("请指定发送用户，个人或多人");
        }
        MsgNotificationDto dto = BeanUtil.cp(req, MsgNotificationDto.class);
        dto.setUserCode(PrincipalContext.getUserCode());
        Integer rt = msgNotificationService.createNotification(dto);
        return R.ok(rt);
    }

    @Operation(summary = "3.消息通知-详情", description = "根据ID查询消息通知详情")
    @GetMapping(Route.MANAGER_NOTIFICATION_INFO)
    public R<MsgNotificationResp> managerNotificationInfo(@Valid MsgNotificationInfoReq req) {
        MsgNotification entity = msgNotificationService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        MsgNotificationResp resp = BeanUtil.cp(entity, MsgNotificationResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.消息通知-阅读记录", description = "查询消息通知的阅读记录分页")
    @GetMapping(Route.MANAGER_NOTIFICATION_RECORD_PAGE)
    public R<PageData<MsgNotificationRecordPageResp>> managerNotificationRecordPage(@Valid MsgNotificationRecordPageReq req) {
        MsgUserRecord entity = BeanUtil.cp(req, MsgUserRecord.class);
        PageData<MsgUserRecord> page = msgUserRecordService.selectPage(entity);
        PageData<MsgNotificationRecordPageResp> newPage = page.convert(MsgNotificationRecordPageResp.class);
        return R.ok(newPage);
    }

}
