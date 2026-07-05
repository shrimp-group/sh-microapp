package com.wkclz.micro.material.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.api.FileosSignApi;
import com.wkclz.micro.material.bean.entity.MdmMaterial;
import com.wkclz.micro.material.bean.entity.MdmMaterialGroup;
import com.wkclz.micro.material.bean.req.MaterialPickerListReq;
import com.wkclz.micro.material.bean.resp.MaterialGroupTreeResp;
import com.wkclz.micro.material.bean.resp.MaterialPageResp;
import com.wkclz.micro.material.service.MdmMaterialGroupService;
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

import java.util.*;

@Tag(name = "7.素材选择器", description = "素材选择器接口")
@Slf4j
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class MaterialPickerRest {

    @Autowired
    private FileosSignApi fileosSignApi;
    @Autowired
    private MdmMaterialService mdmMaterialService;
    @Autowired
    private MdmMaterialGroupService mdmMaterialGroupService;

    @Operation(summary = "30.选择器-素材列表", description = "选择器素材分页列表")
    @GetMapping(Route.PICKER_LIST)
    public R<PageData<MaterialPageResp>> list(@Valid MaterialPickerListReq req) {
        MdmMaterial entity = BeanUtil.cp(req, MdmMaterial.class);
        PageData<MdmMaterial> page = mdmMaterialService.getPickerPage(entity);
        PageData<MaterialPageResp> convert = page.convert(MaterialPageResp.class);
        fileosSignApi.sign(convert.getRecords(), MaterialPageResp::getFileId, MaterialPageResp::setSignedUrl);
        return R.ok(convert);
    }

    @Operation(summary = "31.选择器-分组树", description = "选择器分组树形结构")
    @GetMapping(Route.PICKER_GROUPS)
    public R<List<MaterialGroupTreeResp>> groups() {
        List<MdmMaterialGroup> groups = mdmMaterialGroupService.getPickerTree();
        List<MaterialGroupTreeResp> tree = buildTree(groups);
        return R.ok(tree);
    }

    private List<MaterialGroupTreeResp> buildTree(List<MdmMaterialGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, MaterialGroupTreeResp> map = new LinkedHashMap<>();
        for (MdmMaterialGroup g : groups) {
            MaterialGroupTreeResp node = BeanUtil.cp(g, MaterialGroupTreeResp.class);
            node.setChildren(new ArrayList<>());
            map.put(g.getGroupCode(), node);
        }
        List<MaterialGroupTreeResp> roots = new ArrayList<>();
        for (MaterialGroupTreeResp node : map.values()) {
            if ("0".equals(node.getParentCode()) || !map.containsKey(node.getParentCode())) {
                roots.add(node);
            } else {
                map.get(node.getParentCode()).getChildren().add(node);
            }
        }
        return roots;
    }
}
