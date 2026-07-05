package com.wkclz.micro.rmcheck.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.rmcheck.bean.dto.RmCheckRuleItemDto;
import com.wkclz.micro.rmcheck.bean.entity.RmCheckRuleItem;
import com.wkclz.micro.rmcheck.bean.req.RmCheckRuleItemCreateReq;
import com.wkclz.micro.rmcheck.bean.req.RmCheckRuleItemInfoReq;
import com.wkclz.micro.rmcheck.bean.req.RmCheckRuleItemListReq;
import com.wkclz.micro.rmcheck.bean.req.RmCheckRuleItemUpdateReq;
import com.wkclz.micro.rmcheck.bean.resp.RmCheckRuleItemResp;
import com.wkclz.micro.rmcheck.service.RmCheckRuleItemService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table rm_check_rule_item (删除检查规则-检查项) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "2.删除检查规则检查项", description = "删除检查规则检查项管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class RmCheckRuleItemRest {

    @Autowired
    private RmCheckRuleItemService rmCheckRuleItemService;

    @Operation(summary = "1.删除检查规则检查项-列表", description = "根据规则编码查询检查项列表")
    @GetMapping(Route.RM_CHECK_RULE_ITEM_LIST)
    public R<List<RmCheckRuleItemResp>> rmCheckRuleItemList(@Valid RmCheckRuleItemListReq req) {
        RmCheckRuleItemDto dto = BeanUtil.cp(req, RmCheckRuleItemDto.class);
        List<RmCheckRuleItemDto> list = rmCheckRuleItemService.getRmCheckRuleItemList(dto);
        List<RmCheckRuleItemResp> respList = BeanUtil.cp(list, RmCheckRuleItemResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "2.删除检查规则检查项-详情", description = "根据ID查询检查项详情")
    @GetMapping(Route.RM_CHECK_RULE_ITEM_INFO)
    public R<RmCheckRuleItemResp> rmCheckRuleItemInfo(@Valid RmCheckRuleItemInfoReq req) {
        RmCheckRuleItem entity = rmCheckRuleItemService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        RmCheckRuleItemResp resp = BeanUtil.cp(entity, RmCheckRuleItemResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.删除检查规则检查项-创建", description = "新增删除检查规则检查项")
    @PostMapping(Route.RM_CHECK_RULE_ITEM_CREATE)
    public R<RmCheckRuleItemResp> rmCheckRuleItemCreate(@Valid @RequestBody RmCheckRuleItemCreateReq req) {
        RmCheckRuleItem entity = BeanUtil.cp(req, RmCheckRuleItem.class);
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(1);
        }
        entity = rmCheckRuleItemService.create(entity);
        RmCheckRuleItemResp resp = BeanUtil.cp(entity, RmCheckRuleItemResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.删除检查规则检查项-修改", description = "修改删除检查规则检查项")
    @PostMapping(Route.RM_CHECK_RULE_ITEM_UPDATE)
    public R<RmCheckRuleItemResp> rmCheckRuleItemUpdate(@Valid @RequestBody RmCheckRuleItemUpdateReq req) {
        RmCheckRuleItem entity = BeanUtil.cp(req, RmCheckRuleItem.class);
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(1);
        }
        entity = rmCheckRuleItemService.update(entity);
        RmCheckRuleItemResp resp = BeanUtil.cp(entity, RmCheckRuleItemResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.删除检查规则检查项-删除", description = "删除删除检查规则检查项")
    @PostMapping(Route.RM_CHECK_RULE_ITEM_REMOVE)
    public R<Integer> rmCheckRuleItemRemove(@Valid @RequestBody RemoveReq req) {
        RmCheckRuleItem entity = new RmCheckRuleItem();
        entity.setId(req.getId());
        rmCheckRuleItemService.deleteById(entity);
        return R.ok(1);
    }

}
