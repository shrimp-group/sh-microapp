package com.wkclz.micro.material.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.micro.material.bean.entity.MdmMaterial;
import com.wkclz.micro.material.bean.req.MaterialPageReq;
import com.wkclz.micro.material.bean.resp.MaterialDistributionResp;
import com.wkclz.micro.material.bean.resp.MaterialPageResp;
import com.wkclz.micro.material.service.MdmMaterialService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "6.素材统计", description = "素材统计接口")
@Slf4j
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class MaterialStatsRest {

    @Autowired
    private MdmMaterialService mdmMaterialService;

    @Operation(summary = "28.统计-热门", description = "热门素材分页查询")
    @GetMapping(Route.STATS_HOT)
    public R<PageData<MaterialPageResp>> hot(@Valid MaterialPageReq req) {
        MdmMaterial entity = BeanUtil.cp(req, MdmMaterial.class);
        PageData<MdmMaterial> page = mdmMaterialService.getHotPage(entity);
        return R.ok(page.convert(MaterialPageResp.class));
    }

    @Operation(summary = "29.统计-分布", description = "素材类型分布统计")
    @GetMapping(Route.STATS_DISTRIBUTION)
    public R<List<MaterialDistributionResp>> distribution() {
        MdmMaterial param = new MdmMaterial();
        param.setTenantCode(IdentityContext.getTenantCode());
        List<MdmMaterial> all = mdmMaterialService.selectByEntity(param);
        Map<String, Long> typeCount = all.stream()
                .filter(m -> m.getMaterialType() != null)
                .collect(Collectors.groupingBy(MdmMaterial::getMaterialType, Collectors.counting()));
        List<MaterialDistributionResp> result = typeCount.entrySet().stream().map(e -> {
            MaterialDistributionResp resp = new MaterialDistributionResp();
            resp.setMaterialType(e.getKey());
            resp.setCount(e.getValue());
            return resp;
        }).collect(Collectors.toList());
        return R.ok(result);
    }
}
