package com.wkclz.micro.material.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.api.FileosSignApi;
import com.wkclz.micro.material.bean.entity.MdmMaterial;
import com.wkclz.micro.material.bean.req.*;
import com.wkclz.micro.material.bean.resp.MaterialBatchCreateResp;
import com.wkclz.micro.material.bean.resp.MaterialPageResp;
import com.wkclz.micro.material.bean.resp.MaterialResp;
import com.wkclz.micro.material.service.MdmMaterialService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "1.素材管理", description = "素材管理接口")
@Slf4j
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class MaterialRest {

    @Autowired
    private FileosSignApi fileosSignApi;
    @Autowired
    private MdmMaterialService mdmMaterialService;

    @Operation(summary = "1.素材-分页查询", description = "分页查询素材列表")
    @GetMapping(Route.MATERIAL_PAGE)
    public R<PageData<MaterialPageResp>> page(@Valid MaterialPageReq req) {
        MdmMaterial entity = BeanUtil.cp(req, MdmMaterial.class);
        PageData<MdmMaterial> page = mdmMaterialService.getPage(entity);
        PageData<MaterialPageResp> convert = page.convert(MaterialPageResp.class);
        fileosSignApi.sign(convert.getRecords(), MaterialPageResp::getFileId, MaterialPageResp::setSignedUrl);
        return R.ok(convert);
    }

    @Operation(summary = "2.素材-详情", description = "根据ID查询素材详情")
    @GetMapping(Route.MATERIAL_INFO)
    public R<MaterialResp> info(@Valid MaterialInfoReq req) {
        MdmMaterial material = mdmMaterialService.getInfo(req.getId());
        MaterialResp resp = BeanUtil.cp(material, MaterialResp.class);
        if (material.getFileId() != null) {
            resp.setSignedUrl(fileosSignApi.sign(material.getFileId()));
        }
        return R.ok(resp);
    }

    @Operation(summary = "3.素材-创建", description = "创建素材")
    @PostMapping(Route.MATERIAL_CREATE)
    public R<MaterialResp> create(@Valid @RequestBody MaterialCreateReq req) {
        log.info("素材-创建, fileId: {}", req.getFileId());
        MdmMaterial material = mdmMaterialService.create(req.getFileId(), req.getFileName(), req.getFileSize(),
                req.getMaterialName(), req.getMaterialType(), req.getGroupCode(), req.getVisibility(), req.getDescription());
        MaterialResp resp = BeanUtil.cp(material, MaterialResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.素材-批量创建", description = "批量创建素材")
    @PostMapping(Route.MATERIAL_BATCH_CREATE)
    public R<MaterialBatchCreateResp> batchCreate(@Valid @RequestBody MaterialBatchCreateReq req) {
        log.info("素材-批量创建, items size: {}", req.getItems().size());
        List<MdmMaterial> results = mdmMaterialService.batchCreate(req);
        MaterialBatchCreateResp resp = new MaterialBatchCreateResp();
        resp.setMaterials(BeanUtil.cp(results, MaterialResp.class));
        return R.ok(resp);
    }

    @Operation(summary = "5.素材-链接引入", description = "通过链接引入素材")
    @PostMapping(Route.MATERIAL_LINK_CREATE)
    public R<MaterialResp> linkCreate(@Valid @RequestBody MaterialLinkCreateReq req) {
        log.info("素材-链接引入, linkUrl: {}", req.getLinkUrl());
        MdmMaterial material = mdmMaterialService.linkCreate(req.getMaterialName(), req.getMaterialType(), req.getLinkUrl(), req.getGroupCode(), req.getVisibility(), req.getDescription());
        MaterialResp resp = BeanUtil.cp(material, MaterialResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "12.素材-链接有效性检测", description = "检测链接是否有效")
    @PostMapping(Route.MATERIAL_LINK_CHECK)
    public R<Map<String, String>> linkCheck(@Valid @RequestBody MaterialLinkCheckReq req) {
        log.info("素材-链接有效性检测, linkUrl: {}", req.getLinkUrl());
        String status = mdmMaterialService.checkLink(req.getLinkUrl());
        Map<String, String> result = new HashMap<>();
        result.put("linkStatus", status);
        return R.ok(result);
    }

    @Operation(summary = "6.素材-修改", description = "修改素材信息")
    @PostMapping(Route.MATERIAL_UPDATE)
    public R<Integer> update(@Valid @RequestBody MaterialUpdateReq req) {
        MdmMaterial entity = BeanUtil.cp(req, MdmMaterial.class);
        Integer result = mdmMaterialService.update(entity);
        return R.ok(result);
    }

    @Operation(summary = "7.素材-删除", description = "删除素材")
    @PostMapping(Route.MATERIAL_REMOVE)
    public R<Integer> remove(@Valid @RequestBody MaterialRemoveReq req) {
        log.info("素材-删除, ids: {}", req.getIds());
        Integer result = mdmMaterialService.remove(req.getIds());
        return R.ok(result);
    }

    @Operation(summary = "8.素材-恢复", description = "恢复已删除的素材")
    @PostMapping(Route.MATERIAL_RESTORE)
    public R<Integer> restore(@Valid @RequestBody MaterialRestoreReq req) {
        log.info("素材-恢复, ids: {}", req.getIds());
        Integer result = mdmMaterialService.restore(req.getIds());
        return R.ok(result);
    }

    @Operation(summary = "9.素材-移动分组", description = "将素材移动到指定分组")
    @PostMapping(Route.MATERIAL_MOVE)
    public R<Integer> move(@Valid @RequestBody MaterialMoveReq req) {
        log.info("素材-移动分组, ids: {}, groupCode: {}", req.getIds(), req.getGroupCode());
        Integer result = mdmMaterialService.move(req.getIds(), req.getGroupCode());
        return R.ok(result);
    }

    @Operation(summary = "10.素材-替换文件", description = "替换素材文件")
    @PostMapping(Route.MATERIAL_REPLACE_FILE)
    public R<MaterialResp> replaceFile(@Valid @RequestBody MaterialReplaceFileReq req) {
        log.info("素材-替换文件, id: {}, fileId: {}", req.getId(), req.getFileId());
        MdmMaterial material = mdmMaterialService.replaceFile(req.getId(), req.getVersion(), req.getFileId(), req.getFileName(), req.getFileSize());
        MaterialResp resp = BeanUtil.cp(material, MaterialResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "11.素材-修改可见性", description = "修改素材可见性")
    @PostMapping(Route.MATERIAL_VISIBILITY)
    public R<Integer> visibility(@Valid @RequestBody MaterialVisibilityReq req) {
        log.info("素材-修改可见性, ids: {}, visibility: {}", req.getIds(), req.getVisibility());
        Integer result = mdmMaterialService.updateVisibility(req.getIds(), req.getVisibility());
        return R.ok(result);
    }
}
