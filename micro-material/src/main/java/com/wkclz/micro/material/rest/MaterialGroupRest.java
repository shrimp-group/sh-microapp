package com.wkclz.micro.material.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.material.bean.entity.MdmMaterialGroup;
import com.wkclz.micro.material.bean.req.MaterialGroupCreateReq;
import com.wkclz.micro.material.bean.req.MaterialGroupMoveReq;
import com.wkclz.micro.material.bean.req.MaterialGroupRemoveReq;
import com.wkclz.micro.material.bean.req.MaterialGroupSortReq;
import com.wkclz.micro.material.bean.req.MaterialGroupUpdateReq;
import com.wkclz.micro.material.bean.resp.MaterialGroupResp;
import com.wkclz.micro.material.bean.resp.MaterialGroupTreeResp;
import com.wkclz.micro.material.service.MdmMaterialGroupService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "2.素材分组", description = "素材分组管理接口")
@Slf4j
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class MaterialGroupRest {

    @Autowired
    private MdmMaterialGroupService mdmMaterialGroupService;

    @Operation(summary = "13.分组-树", description = "获取分组树形结构")
    @GetMapping(Route.GROUP_TREE)
    public R<List<MaterialGroupTreeResp>> tree() {
        List<MdmMaterialGroup> groups = mdmMaterialGroupService.getTree();
        List<MaterialGroupTreeResp> tree = buildTree(groups);
        return R.ok(tree);
    }

    @Operation(summary = "14.分组-详情", description = "根据ID查询分组详情")
    @GetMapping(Route.GROUP_INFO)
    public R<MaterialGroupTreeResp> info(@RequestParam @NotNull(message = "ID不存在") Long id) {
        MdmMaterialGroup group = mdmMaterialGroupService.selectById(id);
        if (group == null) {
            return R.error("分组不存在");
        }
        MaterialGroupTreeResp resp = BeanUtil.cp(group, MaterialGroupTreeResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "15.分组-新增", description = "新增素材分组")
    @PostMapping(Route.GROUP_CREATE)
    public R<MaterialGroupResp> create(@Valid @RequestBody MaterialGroupCreateReq req) {
        log.info("分组-新增, groupName: {}", req.getGroupName());
        MdmMaterialGroup entity = new MdmMaterialGroup();
        entity.setParentCode(req.getParentCode() != null ? req.getParentCode() : "0");
        entity.setGroupName(req.getGroupName());
        entity.setGroupType(req.getGroupType() != null ? req.getGroupType() : "PERSONAL");
        entity = mdmMaterialGroupService.create(entity);
        MaterialGroupResp resp = BeanUtil.cp(entity, MaterialGroupResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "16.分组-修改", description = "修改素材分组")
    @PostMapping(Route.GROUP_UPDATE)
    public R<MaterialGroupResp> update(@Valid @RequestBody MaterialGroupUpdateReq req) {
        log.info("分组-修改, id: {}", req.getId());
        MdmMaterialGroup entity = BeanUtil.cp(req, MdmMaterialGroup.class);
        entity = mdmMaterialGroupService.update(entity);
        MaterialGroupResp resp = BeanUtil.cp(entity, MaterialGroupResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "17.分组-删除", description = "删除素材分组")
    @PostMapping(Route.GROUP_REMOVE)
    public R<Integer> remove(@Valid @RequestBody MaterialGroupRemoveReq req) {
        log.info("分组-删除, id: {}", req.getId());
        MdmMaterialGroup entity = new MdmMaterialGroup();
        entity.setId(req.getId());
        Integer result = mdmMaterialGroupService.remove(entity);
        return R.ok(result);
    }

    @Operation(summary = "18.分组-移动", description = "移动素材分组到指定父级")
    @PostMapping(Route.GROUP_MOVE)
    public R<Integer> move(@Valid @RequestBody MaterialGroupMoveReq req) {
        log.info("分组-移动, id: {}, parentCode: {}", req.getId(), req.getParentCode());
        Integer result = mdmMaterialGroupService.move(req.getId(), req.getVersion(), req.getParentCode());
        return R.ok(result);
    }

    @Operation(summary = "19.分组-排序", description = "素材分组排序")
    @PostMapping(Route.GROUP_SORT)
    public R<Integer> sort(@Valid @RequestBody MaterialGroupSortReq req) {
        log.info("分组-排序, ids: {}", req.getIds());
        Integer result = mdmMaterialGroupService.sort(req.getIds());
        return R.ok(result);
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
