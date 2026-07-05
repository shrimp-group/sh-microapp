package com.wkclz.micro.msg.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.msg.bean.entity.MsgTemplate;
import com.wkclz.micro.msg.bean.req.MsgTemplateCreateReq;
import com.wkclz.micro.msg.bean.req.MsgTemplateInfoReq;
import com.wkclz.micro.msg.bean.req.MsgTemplatePageReq;
import com.wkclz.micro.msg.bean.req.MsgTemplateUpdateReq;
import com.wkclz.micro.msg.bean.resp.MsgTemplatePageResp;
import com.wkclz.micro.msg.bean.resp.MsgTemplateResp;
import com.wkclz.micro.msg.service.MsgTemplateService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table msg_template (消息模板) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "1.消息模板", description = "消息模板管理接口")
@RestController
@RequestMapping(Route.PREFIX)
public class ManagerMsgTemplateRest {

    @Resource
    private MsgTemplateService msgTemplateService;

    @Operation(summary = "1.消息模板-分页查询", description = "根据条件分页查询消息模板列表")
    @GetMapping(Route.MANAGER_TEMPLATE_PAGE)
    public R<PageData<MsgTemplatePageResp>> msgTemplatePage(MsgTemplatePageReq req) {
        MsgTemplate entity = BeanUtil.cp(req, MsgTemplate.class);
        PageData<MsgTemplate> page = msgTemplateService.selectPage(entity);
        PageData<MsgTemplatePageResp> newPage = page.convert(MsgTemplatePageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.消息模板-详情", description = "根据ID查询消息模板详情")
    @GetMapping(Route.MANAGER_TEMPLATE_INFO)
    public R<MsgTemplateResp> msgTemplateInfo(@Valid MsgTemplateInfoReq req) {
        MsgTemplate entity = msgTemplateService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        MsgTemplateResp resp = BeanUtil.cp(entity, MsgTemplateResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.消息模板-创建", description = "新增消息模板")
    @PostMapping(Route.MANAGER_TEMPLATE_CREATE)
    public R<MsgTemplateResp> msgTemplateCreate(@RequestBody MsgTemplateCreateReq req) {
        MsgTemplate entity = BeanUtil.cp(req, MsgTemplate.class);
        entity = msgTemplateService.create(entity);
        MsgTemplateResp resp = BeanUtil.cp(entity, MsgTemplateResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.消息模板-修改", description = "修改消息模板")
    @PostMapping(Route.MANAGER_TEMPLATE_UPDATE)
    public R<MsgTemplateResp> msgTemplateUpdate(@RequestBody MsgTemplateUpdateReq req) {
        MsgTemplate entity = BeanUtil.cp(req, MsgTemplate.class);
        entity = msgTemplateService.update(entity);
        MsgTemplateResp resp = BeanUtil.cp(entity, MsgTemplateResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.消息模板-删除", description = "删除消息模板")
    @PostMapping(Route.MANAGER_TEMPLATE_REMOVE)
    public R<Integer> msgTemplateRemove(@Valid @RequestBody RemoveReq req) {
        MsgTemplate entity = new MsgTemplate();
        entity.setId(req.getId());
        msgTemplateService.deleteById(entity);
        return R.ok(1);
    }

}
