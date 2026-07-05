package com.wkclz.micro.material.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.material.bean.entity.MdmMaterialVersion;
import com.wkclz.micro.material.bean.req.MaterialVersionListReq;
import com.wkclz.micro.material.bean.req.MaterialVersionRollbackReq;
import com.wkclz.micro.material.bean.resp.MaterialVersionResp;
import com.wkclz.micro.material.service.MdmMaterialVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "4.素材版本", description = "素材版本管理接口")
@Slf4j
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class MaterialVersionRest {

    @Autowired
    private MdmMaterialVersionService mdmMaterialVersionService;

    @Operation(summary = "24.版本-列表", description = "查询素材版本列表")
    @GetMapping(Route.VERSION_LIST)
    public R<List<MaterialVersionResp>> list(@Valid MaterialVersionListReq req) {
        List<MdmMaterialVersion> versions = mdmMaterialVersionService.listByMaterialCode(req.getMaterialCode());
        List<MaterialVersionResp> respList = versions.stream().map(v -> {
            MaterialVersionResp resp = new MaterialVersionResp();
            resp.setId(v.getId());
            resp.setMaterialCode(v.getMaterialCode());
            resp.setVersionNo(v.getVersionNo());
            resp.setFileId(v.getFileId());
            resp.setFileName(v.getFileName());
            resp.setFileSize(v.getFileSize());
            resp.setCreateTime(v.getCreateTime() != null ? v.getCreateTime().toString() : null);
            resp.setCreateBy(v.getCreateBy());
            return resp;
        }).collect(Collectors.toList());
        return R.ok(respList);
    }

    @Operation(summary = "25.版本-回滚", description = "回滚素材版本")
    @PostMapping(Route.VERSION_ROLLBACK)
    public R<String> rollback(@Valid @RequestBody MaterialVersionRollbackReq req) {
        log.info("版本-回滚, materialCode: {}", req.getMaterialCode());
        return R.ok("版本回滚需通过替换文件实现");
    }
}
