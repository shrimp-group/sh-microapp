package com.wkclz.micro.dbview.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.dbview.bean.entity.DbviewDatasource;
import com.wkclz.micro.dbview.bean.req.*;
import com.wkclz.micro.dbview.bean.resp.DatasourcePageResp;
import com.wkclz.micro.dbview.bean.resp.DatasourceResp;
import com.wkclz.micro.dbview.service.DbviewConnectionService;
import com.wkclz.micro.dbview.service.DbviewDatasourceService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据源管理", description = "数据源管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class DatasourceRest {

    @Autowired
    private DbviewDatasourceService datasourceService;

    @Autowired
    private DbviewConnectionService connectionService;

    @Operation(summary = "1. 数据源-分页查询")
    @GetMapping(Route.DATASOURCE_PAGE)
    public R<PageData<DatasourcePageResp>> page(@Valid DatasourcePageReq req) {
        DbviewDatasource entity = BeanUtil.cp(req, DbviewDatasource.class);
        PageData<DbviewDatasource> page = datasourceService.getDatasourcePage(entity);
        PageData<DatasourcePageResp> newPage = page.convert(DatasourcePageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2. 数据源-详情")
    @GetMapping(Route.DATASOURCE_INFO)
    public R<DatasourceResp> info(@Valid DatasourceInfoReq req) {
        DbviewDatasource entity = datasourceService.selectById(req.getId());
        DatasourceResp resp = BeanUtil.cp(entity, DatasourceResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3. 数据源-新增")
    @PostMapping(Route.DATASOURCE_CREATE)
    public R<DatasourceResp> create(@Valid @RequestBody DatasourceCreateReq req) {
        DbviewDatasource entity = BeanUtil.cp(req, DbviewDatasource.class);
        entity = datasourceService.create(entity);
        DatasourceResp resp = BeanUtil.cp(entity, DatasourceResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4. 数据源-更新")
    @PostMapping(Route.DATASOURCE_UPDATE)
    public R<DatasourceResp> update(@Valid @RequestBody DatasourceUpdateReq req) {
        DbviewDatasource entity = BeanUtil.cp(req, DbviewDatasource.class);
        entity = datasourceService.update(entity);
        DatasourceResp resp = BeanUtil.cp(entity, DatasourceResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5. 数据源-删除")
    @PostMapping(Route.DATASOURCE_REMOVE)
    public R<Integer> remove(@Valid @RequestBody DatasourceRemoveReq req) {
        DbviewDatasource entity = new DbviewDatasource();
        entity.setId(req.getId());
        datasourceService.remove(entity);
        return R.ok(1);
    }

    @Operation(summary = "6. 数据源-选项列表")
    @GetMapping(Route.DATASOURCE_OPTIONS)
    public R<List<String>> options() {
        List<String> options = datasourceService.getDatasourceOptions();
        return R.ok(options);
    }

    @Operation(summary = "7. 数据源-测试连接")
    @PostMapping(Route.DATASOURCE_TEST_CONNECTION)
    public R<Boolean> testConnection(@Valid @RequestBody DatasourceTestConnectionReq req) {
        boolean success;
        if (req.getId() != null) {
            success = connectionService.testConnection(req.getId());
        } else {
            DbviewDatasource entity = BeanUtil.cp(req, DbviewDatasource.class);
            success = connectionService.testConnection(entity);
        }
        return R.ok(success);
    }
}
