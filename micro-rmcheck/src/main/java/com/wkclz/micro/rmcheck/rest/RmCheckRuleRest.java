package com.wkclz.micro.rmcheck.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.rmcheck.bean.dto.RmCheckRuleDto;
import com.wkclz.micro.rmcheck.bean.entity.RmCheckRule;
import com.wkclz.micro.rmcheck.bean.req.RmCheckRuleCreateReq;
import com.wkclz.micro.rmcheck.bean.req.RmCheckRuleInfoReq;
import com.wkclz.micro.rmcheck.bean.req.RmCheckRulePageReq;
import com.wkclz.micro.rmcheck.bean.req.RmCheckRuleUpdateReq;
import com.wkclz.micro.rmcheck.bean.resp.RmCheckRulePageResp;
import com.wkclz.micro.rmcheck.bean.resp.RmCheckRuleResp;
import com.wkclz.micro.rmcheck.service.RmCheckRuleService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table rm_check_rule (删除检查规则) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "1.删除检查规则", description = "删除检查规则管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class RmCheckRuleRest {

    @Autowired
    private RmCheckRuleService rmCheckRuleService;

    @Operation(summary = "1.删除检查规则-分页查询", description = "根据条件分页查询删除检查规则列表")
    @GetMapping(Route.RM_CHECK_RULE_PAGE)
    public R<PageData<RmCheckRulePageResp>> rmCheckRulePage(@Valid RmCheckRulePageReq req) {
        RmCheckRuleDto dto = BeanUtil.cp(req, RmCheckRuleDto.class);
        PageData<RmCheckRuleDto> page = rmCheckRuleService.getRmCheckRulePage(dto);
        PageData<RmCheckRulePageResp> newPage = page.convert(RmCheckRulePageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.删除检查规则-详情", description = "根据ID查询删除检查规则详情")
    @GetMapping(Route.RM_CHECK_RULE_INFO)
    public R<RmCheckRuleResp> rmCheckRuleInfo(@Valid RmCheckRuleInfoReq req) {
        RmCheckRule entity = rmCheckRuleService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        RmCheckRuleResp resp = BeanUtil.cp(entity, RmCheckRuleResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.删除检查规则-创建", description = "新增删除检查规则")
    @PostMapping(Route.RM_CHECK_RULE_CREATE)
    public R<RmCheckRuleResp> rmCheckRuleCreate(@Valid @RequestBody RmCheckRuleCreateReq req) {
        RmCheckRule entity = BeanUtil.cp(req, RmCheckRule.class);
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(1);
        }
        entity = rmCheckRuleService.create(entity);
        RmCheckRuleResp resp = BeanUtil.cp(entity, RmCheckRuleResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.删除检查规则-修改", description = "修改删除检查规则")
    @PostMapping(Route.RM_CHECK_RULE_UPDATE)
    public R<RmCheckRuleResp> rmCheckRuleUpdate(@Valid @RequestBody RmCheckRuleUpdateReq req) {
        RmCheckRule entity = BeanUtil.cp(req, RmCheckRule.class);
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(1);
        }
        entity = rmCheckRuleService.update(entity);
        RmCheckRuleResp resp = BeanUtil.cp(entity, RmCheckRuleResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.删除检查规则-删除", description = "删除删除检查规则")
    @PostMapping(Route.RM_CHECK_RULE_REMOVE)
    public R<Integer> rmCheckRuleRemove(@Valid @RequestBody RemoveReq req) {
        RmCheckRule entity = new RmCheckRule();
        entity.setId(req.getId());
        Integer i = rmCheckRuleService.customRemove(entity);
        return R.ok(i);
    }

}
