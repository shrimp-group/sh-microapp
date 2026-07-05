package com.wkclz.micro.fun.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.fun.engine.ScriptEngine;
import com.wkclz.micro.fun.engine.ScriptService;
import com.wkclz.micro.fun.bean.dto.FunFunctionDto;
import com.wkclz.micro.fun.bean.entity.FunFunction;
import com.wkclz.micro.fun.bean.req.FunFunctionCreateReq;
import com.wkclz.micro.fun.bean.req.FunFunctionInfoReq;
import com.wkclz.micro.fun.bean.req.FunFunctionPageReq;
import com.wkclz.micro.fun.bean.req.FunFunctionTestReq;
import com.wkclz.micro.fun.bean.req.FunFunctionUpdateReq;
import com.wkclz.micro.fun.bean.resp.FunFunctionPageResp;
import com.wkclz.micro.fun.bean.resp.FunFunctionResp;
import com.wkclz.micro.fun.service.FunFunctionService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_function (函数-函数体) 示例rest 接口，代码重新生成会覆盖
 */

@Slf4j
@Tag(name = "2.函数体", description = "函数体管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class FunFunctionRest {

    @Autowired
    private ScriptService scriptService;
    @Autowired
    private FunFunctionService funFunctionService;

    @Operation(summary = "1.函数体-分页", description = "分页查询函数体列表")
    @GetMapping(Route.FUN_FUNCTION_PAGE)
    public R<PageData<FunFunctionPageResp>> funFunctionPage(@Valid FunFunctionPageReq req) {
        log.info("函数体-分页查询, req: {}", req);
        FunFunctionDto dto = BeanUtil.cp(req, FunFunctionDto.class);
        PageData<FunFunctionDto> page = funFunctionService.getFunctionPage(dto);
        PageData<FunFunctionPageResp> newPage = page.convert(FunFunctionPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.函数体-详情", description = "根据ID查询函数体详情")
    @GetMapping(Route.FUN_FUNCTION_INFO)
    public R<FunFunctionResp> funFunctionInfo(@Valid FunFunctionInfoReq req) {
        log.info("函数体-详情查询, id: {}", req.getId());
        FunFunction entity = funFunctionService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        FunFunctionResp resp = BeanUtil.cp(entity, FunFunctionResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.函数体-创建", description = "新增函数体")
    @PostMapping(Route.FUN_FUNCTION_CREATE)
    public R<FunFunctionResp> funFunctionCreate(@Valid @RequestBody FunFunctionCreateReq req) {
        log.info("函数体-创建, req: {}", req);
        FunFunction entity = BeanUtil.cp(req, FunFunction.class);
        if (entity.getVisible() == null) {
            entity.setVisible(1);
        }
        if (entity.getDefaultFlag() == null) {
            entity.setDefaultFlag(0);
        }
        if (entity.getSort() == null) {
            entity.setSort(99);
        }
        entity = funFunctionService.create(entity);
        FunFunctionResp resp = BeanUtil.cp(entity, FunFunctionResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.函数体-修改", description = "修改函数体")
    @PostMapping(Route.FUN_FUNCTION_UPDATE)
    public R<FunFunctionResp> funFunctionUpdate(@Valid @RequestBody FunFunctionUpdateReq req) {
        log.info("函数体-修改, req: {}", req);
        FunFunction entity = BeanUtil.cp(req, FunFunction.class);
        if (entity.getVisible() == null) {
            entity.setVisible(1);
        }
        if (entity.getDefaultFlag() == null) {
            entity.setDefaultFlag(0);
        }
        if (entity.getSort() == null) {
            entity.setSort(99);
        }
        entity = funFunctionService.update(entity);
        FunFunctionResp resp = BeanUtil.cp(entity, FunFunctionResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.函数体-删除", description = "删除函数体")
    @PostMapping(Route.FUN_FUNCTION_REMOVE)
    public R<Integer> funFunctionRemove(@Valid @RequestBody RemoveReq req) {
        log.info("函数体-删除, id: {}", req.getId());
        FunFunction entity = new FunFunction();
        entity.setId(req.getId());
        funFunctionService.deleteById(entity);
        return R.ok(1);
    }

    @Operation(summary = "6.函数体-选项", description = "获取函数体选项列表")
    @GetMapping(Route.FUN_FUNCTION_OPTIONS)
    public R<List<FunFunctionPageResp>> funFunctionOptions(@Valid FunFunctionPageReq req) {
        log.info("函数体-选项查询, req: {}", req);
        FunFunctionDto dto = BeanUtil.cp(req, FunFunctionDto.class);
        List<FunFunctionDto> list = funFunctionService.getFunctionOption(dto);
        List<FunFunctionPageResp> respList = BeanUtil.cp(list, FunFunctionPageResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "7.函数体-测试", description = "测试执行函数")
    @PostMapping(Route.FUN_FUNCTION_TEST)
    public R<Object> funFunctionTest(@Valid @RequestBody FunFunctionTestReq req) {
        log.info("函数体-测试, funCode: {}", req.getFunCode());
        FunFunction fun = BeanUtil.cp(req, FunFunction.class);
        ScriptEngine engine = scriptService.getEngineTest(fun);
        Object rt = engine.exec(req.getParam());
        return R.ok(rt);
    }

}
