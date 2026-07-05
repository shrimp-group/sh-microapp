package com.wkclz.micro.mask.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.mask.bean.req.MaskRuleCreateReq;
import com.wkclz.micro.mask.bean.req.MaskRuleInfoReq;
import com.wkclz.micro.mask.bean.req.MaskRulePageReq;
import com.wkclz.micro.mask.bean.req.MaskRuleUpdateReq;
import com.wkclz.micro.mask.bean.resp.MaskRulePageResp;
import com.wkclz.micro.mask.bean.resp.MaskRuleResp;
import com.wkclz.micro.mask.cache.MaskCache;
import com.wkclz.micro.mask.config.MaskResponseAdvice;
import com.wkclz.micro.mask.bean.entity.MdmMaskRule;
import com.wkclz.micro.mask.service.MdmMaskRuleService;
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
 * @table mdm_mask_rule (脱敏规则) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "1.脱敏规则", description = "脱敏规则管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class MaskRuleRest {

    @Autowired
    private MaskCache maskCache;
    @Autowired
    private MdmMaskRuleService mdmMaskRuleService;

    @Operation(summary = "1.脱敏规则-分页查询", description = "根据条件分页查询脱敏规则列表")
    @GetMapping(Route.RULE_PAGE)
    public R<PageData<MaskRulePageResp>> maskRulePage(@Valid MaskRulePageReq req) {
        MdmMaskRule entity = BeanUtil.cp(req, MdmMaskRule.class);
        PageData<MdmMaskRule> page = mdmMaskRuleService.getMaskRulePage(entity);
        PageData<MaskRulePageResp> newPage = page.convert(MaskRulePageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.脱敏规则-详情", description = "根据ID查询脱敏规则详情")
    @GetMapping(Route.RULE_INFO)
    public R<MaskRuleResp> maskRuleInfo(@Valid MaskRuleInfoReq req) {
        MdmMaskRule entity = mdmMaskRuleService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        MaskRuleResp resp = BeanUtil.cp(entity, MaskRuleResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.脱敏规则-创建", description = "新增脱敏规则")
    @PostMapping(Route.RULE_CREATE)
    public R<MaskRuleResp> maskRuleCreate(@Valid @RequestBody MaskRuleCreateReq req) {
        MdmMaskRule entity = BeanUtil.cp(req, MdmMaskRule.class);
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(1);
        }
        entity = mdmMaskRuleService.create(entity);
        maskCache.clearCache();
        MaskResponseAdvice.clearCache();
        MaskRuleResp resp = BeanUtil.cp(entity, MaskRuleResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.脱敏规则-修改", description = "修改脱敏规则")
    @PostMapping(Route.RULE_UPDATE)
    public R<MaskRuleResp> maskRuleUpdate(@Valid @RequestBody MaskRuleUpdateReq req) {
        MdmMaskRule entity = BeanUtil.cp(req, MdmMaskRule.class);
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(1);
        }
        entity = mdmMaskRuleService.update(entity);
        maskCache.clearCache();
        MaskResponseAdvice.clearCache();
        MaskRuleResp resp = BeanUtil.cp(entity, MaskRuleResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.脱敏规则-删除", description = "删除脱敏规则")
    @PostMapping(Route.RULE_REMOVE)
    public R<Integer> maskRuleRemove(@Valid @RequestBody RemoveReq req) {
        mdmMaskRuleService.deleteById(req.getId());
        maskCache.clearCache();
        MaskResponseAdvice.clearCache();
        return R.ok(1);
    }

}
