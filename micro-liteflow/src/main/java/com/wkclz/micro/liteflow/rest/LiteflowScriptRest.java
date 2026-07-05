package com.wkclz.micro.liteflow.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.liteflow.bean.req.LiteflowScriptCreateReq;
import com.wkclz.micro.liteflow.bean.req.LiteflowScriptInfoReq;
import com.wkclz.micro.liteflow.bean.req.LiteflowScriptPageReq;
import com.wkclz.micro.liteflow.bean.req.LiteflowScriptUpdateReq;
import com.wkclz.micro.liteflow.bean.resp.LiteflowScriptPageResp;
import com.wkclz.micro.liteflow.bean.resp.LiteflowScriptResp;
import com.wkclz.micro.liteflow.bean.entity.LiteflowScript;
import com.wkclz.micro.liteflow.service.LiteflowScriptService;
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
 * @table liteflow_script (liteflow-脚本) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "2.脚本", description = "LiteFlow脚本管理接口")
@Slf4j
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class LiteflowScriptRest {

    @Autowired
    private LiteflowScriptService liteflowScriptService;

    @Operation(summary = "1.脚本-分页查询", description = "根据条件分页查询脚本列表")
    @GetMapping(Route.SCRIPT_PAGE)
    public R<PageData<LiteflowScriptPageResp>> liteflowScriptPage(@Valid LiteflowScriptPageReq req) {
        log.info("脚本分页查询, scriptId: {}, scriptName: {}", req.getScriptId(), req.getScriptName());
        LiteflowScript entity = BeanUtil.cp(req, LiteflowScript.class);
        PageData<LiteflowScript> page = liteflowScriptService.getLiteflowScriptPage(entity);
        PageData<LiteflowScriptPageResp> newPage = page.convert(LiteflowScriptPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.脚本-详情", description = "根据ID查询脚本详情")
    @GetMapping(Route.SCRIPT_INFO)
    public R<LiteflowScriptResp> liteflowScriptInfo(@Valid LiteflowScriptInfoReq req) {
        log.info("脚本详情查询, id: {}", req.getId());
        LiteflowScript entity = liteflowScriptService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        LiteflowScriptResp resp = BeanUtil.cp(entity, LiteflowScriptResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.脚本-创建", description = "新增脚本")
    @PostMapping(Route.SCRIPT_CREATE)
    public R<LiteflowScriptResp> liteflowScriptCreate(@Valid @RequestBody LiteflowScriptCreateReq req) {
        log.info("脚本创建, scriptId: {}, scriptName: {}", req.getScriptId(), req.getScriptName());
        LiteflowScript entity = BeanUtil.cp(req, LiteflowScript.class);
        if (entity.getEnable() == null) {
            entity.setEnable(1);
        }
        entity = liteflowScriptService.create(entity);
        LiteflowScriptResp resp = BeanUtil.cp(entity, LiteflowScriptResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.脚本-修改", description = "修改脚本")
    @PostMapping(Route.SCRIPT_UPDATE)
    public R<LiteflowScriptResp> liteflowScriptUpdate(@Valid @RequestBody LiteflowScriptUpdateReq req) {
        log.info("脚本修改, id: {}, scriptId: {}", req.getId(), req.getScriptId());
        LiteflowScript entity = BeanUtil.cp(req, LiteflowScript.class);
        entity = liteflowScriptService.update(entity);
        LiteflowScriptResp resp = BeanUtil.cp(entity, LiteflowScriptResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.脚本-删除", description = "删除脚本")
    @PostMapping(Route.SCRIPT_REMOVE)
    public R<Integer> liteflowScriptRemove(@Valid @RequestBody RemoveReq req) {
        log.info("脚本删除, id: {}", req.getId());
        LiteflowScript entity = new LiteflowScript();
        entity.setId(req.getId());
        liteflowScriptService.deleteById(entity);
        return R.ok(1);
    }

}
