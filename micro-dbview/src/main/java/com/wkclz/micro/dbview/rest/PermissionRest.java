package com.wkclz.micro.dbview.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.dbview.bean.entity.DbviewDatasourcePermission;
import com.wkclz.micro.dbview.bean.req.*;
import com.wkclz.micro.dbview.bean.resp.PermissionResp;
import com.wkclz.micro.dbview.service.DbviewDatasourcePermissionService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据源权限管理", description = "数据源权限管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class PermissionRest {

    @Autowired
    private DbviewDatasourcePermissionService permissionService;

    @Operation(summary = "1. 权限-分页查询")
    @GetMapping(Route.PERMISSION_PAGE)
    public R<PageData<PermissionResp>> page(@Valid PermissionPageReq req) {
        DbviewDatasourcePermission entity = BeanUtil.cp(req, DbviewDatasourcePermission.class);
        PageData<DbviewDatasourcePermission> page = permissionService.getPermissionPage(entity);
        PageData<PermissionResp> newPage = page.convert(PermissionResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2. 权限-新增")
    @PostMapping(Route.PERMISSION_CREATE)
    public R<PermissionResp> create(@Valid @RequestBody PermissionCreateReq req) {
        DbviewDatasourcePermission entity = BeanUtil.cp(req, DbviewDatasourcePermission.class);
        entity = permissionService.create(entity);
        PermissionResp resp = BeanUtil.cp(entity, PermissionResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3. 权限-更新")
    @PostMapping(Route.PERMISSION_UPDATE)
    public R<PermissionResp> update(@Valid @RequestBody PermissionUpdateReq req) {
        DbviewDatasourcePermission entity = BeanUtil.cp(req, DbviewDatasourcePermission.class);
        entity = permissionService.update(entity);
        PermissionResp resp = BeanUtil.cp(entity, PermissionResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4. 权限-删除")
    @PostMapping(Route.PERMISSION_REMOVE)
    public R<Integer> remove(@Valid @RequestBody PermissionRemoveReq req) {
        DbviewDatasourcePermission entity = new DbviewDatasourcePermission();
        entity.setId(req.getId());
        permissionService.remove(entity);
        return R.ok(1);
    }

    @Operation(summary = "5. 权限-当前用户权限列表")
    @GetMapping(Route.PERMISSION_MY)
    public R<List<PermissionResp>> myPermissions() {
        List<DbviewDatasourcePermission> permissions = permissionService.getMyPermissions();
        List<PermissionResp> respList = BeanUtil.cp(permissions, PermissionResp.class);
        return R.ok(respList);
    }
}
