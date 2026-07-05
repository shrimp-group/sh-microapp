package com.wkclz.micro.liteflow.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.liteflow.bean.req.LiteflowChainCreateReq;
import com.wkclz.micro.liteflow.bean.req.LiteflowChainInfoReq;
import com.wkclz.micro.liteflow.bean.req.LiteflowChainPageReq;
import com.wkclz.micro.liteflow.bean.req.LiteflowChainUpdateReq;
import com.wkclz.micro.liteflow.bean.resp.LiteflowChainPageResp;
import com.wkclz.micro.liteflow.bean.resp.LiteflowChainResp;
import com.wkclz.micro.liteflow.bean.entity.LiteflowChain;
import com.wkclz.micro.liteflow.service.LiteflowChainService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table liteflow_chain (liteflow-规则) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "1.规则链", description = "LiteFlow规则链管理接口")
@Slf4j
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class LiteflowChainRest {

    @Autowired
    private LiteflowChainService liteflowChainService;

    @Operation(summary = "1.规则链-分页查询", description = "根据条件分页查询规则链列表")
    @GetMapping(Route.CHAIN_PAGE)
    public R<PageData<LiteflowChainPageResp>> liteflowChainPage(@Valid LiteflowChainPageReq req) {
        log.info("规则链分页查询, chainName: {}, namespace: {}", req.getChainName(), req.getNamespace());
        LiteflowChain entity = BeanUtil.cp(req, LiteflowChain.class);
        PageData<LiteflowChain> page = liteflowChainService.getLiteflowChainPage(entity);
        PageData<LiteflowChainPageResp> newPage = page.convert(LiteflowChainPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.规则链-详情", description = "根据ID查询规则链详情")
    @GetMapping(Route.CHAIN_INFO)
    public R<LiteflowChainResp> liteflowChainInfo(@Valid LiteflowChainInfoReq req) {
        log.info("规则链详情查询, id: {}", req.getId());
        LiteflowChain entity = liteflowChainService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        LiteflowChainResp resp = BeanUtil.cp(entity, LiteflowChainResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.规则链-创建", description = "新增规则链")
    @PostMapping(Route.CHAIN_CREATE)
    public R<LiteflowChainResp> liteflowChainCreate(@Valid @RequestBody LiteflowChainCreateReq req) {
        log.info("规则链创建, chainName: {}", req.getChainName());
        LiteflowChain entity = BeanUtil.cp(req, LiteflowChain.class);
        if (entity.getEnable() == null) {
            entity.setEnable(1);
        }
        entity = liteflowChainService.create(entity);
        LiteflowChainResp resp = BeanUtil.cp(entity, LiteflowChainResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.规则链-修改", description = "修改规则链")
    @PostMapping(Route.CHAIN_UPDATE)
    public R<LiteflowChainResp> liteflowChainUpdate(@Valid @RequestBody LiteflowChainUpdateReq req) {
        log.info("规则链修改, id: {}, chainName: {}", req.getId(), req.getChainName());
        LiteflowChain entity = BeanUtil.cp(req, LiteflowChain.class);
        entity = liteflowChainService.update(entity);
        LiteflowChainResp resp = BeanUtil.cp(entity, LiteflowChainResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.规则链-删除", description = "删除规则链")
    @PostMapping(Route.CHAIN_REMOVE)
    public R<Integer> liteflowChainRemove(@Valid @RequestBody RemoveReq req) {
        log.info("规则链删除, id: {}", req.getId());
        LiteflowChain entity = new LiteflowChain();
        entity.setId(req.getId());
        liteflowChainService.deleteById(entity);
        return R.ok(1);
    }

}
