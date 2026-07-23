package com.wkclz.micro.msg.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.micro.msg.bean.dto.MsgUserRecordDto;
import com.wkclz.micro.msg.bean.entity.MsgUserRecord;
import com.wkclz.micro.msg.bean.req.MsgUserRecordInfoReq;
import com.wkclz.micro.msg.bean.req.MsgUserRecordListReq;
import com.wkclz.micro.msg.bean.req.MsgUserRecordPageReq;
import com.wkclz.micro.msg.bean.req.MsgUserRecordReadedReq;
import com.wkclz.micro.msg.bean.resp.MsgUserRecordPageResp;
import com.wkclz.micro.msg.bean.resp.MsgUserRecordResp;
import com.wkclz.micro.msg.service.MsgUserRecordService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_user_record (用户消息记录) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "3.个人消息记录", description = "个人消息记录管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class PersonalUserRecordRest {

    @Autowired
    private MsgUserRecordService msgUserRecordService;

    @Operation(summary = "1.个人消息-列表", description = "获取个人消息列表(最多99条,展示为99+)")
    @GetMapping(Route.PERSONAL_MSG_LIST)
    public R<List<MsgUserRecordResp>> personalMsgList(MsgUserRecordListReq req) {
        MsgUserRecordDto dto = BeanUtil.cp(req, MsgUserRecordDto.class);
        dto.setUserCode(IdentityContext.getUserCode());
        if (dto.getSize() == null) {
            dto.setSize(100L);
        }
        List<MsgUserRecordDto> list = msgUserRecordService.getPersonalRecordList(dto);
        List<MsgUserRecordResp> respList = BeanUtil.cp(list, MsgUserRecordResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "2.个人消息-分页", description = "分页查询个人消息记录")
    @GetMapping(Route.PERSONAL_MSG_PAGE)
    public R<PageData<MsgUserRecordPageResp>> personalMsgPage(MsgUserRecordPageReq req) {
        MsgUserRecordDto dto = BeanUtil.cp(req, MsgUserRecordDto.class);
        dto.setUserCode(IdentityContext.getUserCode());
        PageData<MsgUserRecordDto> page = msgUserRecordService.getPersonalRecordPage(dto);
        PageData<MsgUserRecordPageResp> newPage = page.convert(MsgUserRecordPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "3.个人消息-详情(阅读)", description = "查看个人消息详情并标记阅读")
    @GetMapping(Route.PERSONAL_MSG_INFO)
    public R<MsgUserRecordResp> personalMsgInfo(MsgUserRecordInfoReq req) {
        MsgUserRecordDto dto = msgUserRecordService.getNoticeById(req.getId());
        MsgUserRecordResp resp = BeanUtil.cp(dto, MsgUserRecordResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.个人消息-批量已读", description = "批量标注个人消息为已读")
    @PostMapping(Route.PERSONAL_MSG_READED)
    public R<Integer> personalMsgReaded(@Valid @RequestBody MsgUserRecordReadedReq req) {
        if (req.getId() == null && CollectionUtils.isEmpty(req.getIds())) {
            return R.error("请传 id 或者 ids");
        }
        MsgUserRecord entity = new MsgUserRecord();
        entity.setId(req.getId());
        entity.setIds(req.getIds());
        Integer count = msgUserRecordService.userMarkRecodeReaded(entity);
        return R.ok(count);
    }

}
