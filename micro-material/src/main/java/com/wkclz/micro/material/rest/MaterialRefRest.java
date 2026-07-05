package com.wkclz.micro.material.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.material.bean.entity.MdmMaterialRef;
import com.wkclz.micro.material.bean.req.MaterialRefBindReq;
import com.wkclz.micro.material.bean.req.MaterialRefListReq;
import com.wkclz.micro.material.bean.req.MaterialRefUnbindReq;
import com.wkclz.micro.material.bean.resp.MaterialRefResp;
import com.wkclz.micro.material.service.MdmMaterialRefService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "3.素材引用", description = "素材引用管理接口")
@Slf4j
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class MaterialRefRest {

    @Autowired
    private MdmMaterialRefService mdmMaterialRefService;

    @Operation(summary = "20.引用-注册", description = "注册素材与业务的引用关系")
    @PostMapping(Route.REF_BIND)
    public R<Integer> bind(@RequestBody MaterialRefBindReq req) {
        log.info("引用-注册, materialCode: {}, bizType: {}, bizCode: {}", req.getMaterialCode(), req.getBizType(), req.getBizCode());
        Integer result = mdmMaterialRefService.bind(req.getMaterialCode(), req.getBizType(), req.getBizCode(), req.getRefDesc());
        return R.ok(result);
    }

    @Operation(summary = "21.引用-解绑", description = "解除素材与业务的引用关系")
    @PostMapping(Route.REF_UNBIND)
    public R<Integer> unbind(@Valid @RequestBody MaterialRefUnbindReq req) {
        log.info("引用-解绑, materialCode: {}, bizType: {}, bizCode: {}", req.getMaterialCode(), req.getBizType(), req.getBizCode());
        Integer result = mdmMaterialRefService.unbind(req.getMaterialCode(), req.getBizType(), req.getBizCode());
        return R.ok(result);
    }

    @Operation(summary = "22.引用-列表", description = "查询素材引用列表")
    @GetMapping(Route.REF_LIST)
    public R<List<MaterialRefResp>> list(@Valid MaterialRefListReq req) {
        List<MdmMaterialRef> refs = mdmMaterialRefService.listByMaterialCode(req.getMaterialCode());
        List<MaterialRefResp> respList = refs.stream().map(ref -> BeanUtil.cp(ref, MaterialRefResp.class)).collect(Collectors.toList());
        return R.ok(respList);
    }

    @Operation(summary = "23.引用-检测", description = "检测素材引用状态")
    @GetMapping(Route.REF_CHECK)
    public R<Map<String, Object>> check(@RequestParam @NotBlank(message = "materialCode 不能为空") String materialCode) {
        Map<String, Object> result = mdmMaterialRefService.check(materialCode);
        return R.ok(result);
    }
}
