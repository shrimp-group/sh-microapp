package com.wkclz.micro.dbview.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dbview.bean.dto.ColumnDefinition;
import com.wkclz.micro.dbview.bean.dto.DdlExecuteRequest;
import com.wkclz.micro.dbview.bean.dto.DdlPreview;
import com.wkclz.micro.dbview.bean.dto.DdlRequest;
import com.wkclz.micro.dbview.bean.dto.IndexDefinition;
import com.wkclz.micro.dbview.bean.enums.DdlType;
import com.wkclz.micro.dbview.service.DbviewDdlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "DDL管理", description = "DDL操作管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class DdlRest {

    @Autowired
    private DbviewDdlService ddlService;

    @Operation(summary = "1. DDL-添加字段")
    @PostMapping(Route.DDL_ADD_COLUMN)
    public R<Void> addColumn(@Valid @RequestBody DdlRequest request) {
        request.setDdlType(DdlType.ADD_COLUMN);
        ddlService.execute(request);
        return R.ok();
    }

    @Operation(summary = "2. DDL-删除字段")
    @PostMapping(Route.DDL_DROP_COLUMN)
    public R<Void> dropColumn(@Valid @RequestBody DdlRequest request) {
        request.setDdlType(DdlType.DROP_COLUMN);
        ddlService.execute(request);
        return R.ok();
    }

    @Operation(summary = "3. DDL-修改字段")
    @PostMapping(Route.DDL_MODIFY_COLUMN)
    public R<Void> modifyColumn(@Valid @RequestBody DdlRequest request) {
        request.setDdlType(DdlType.MODIFY_COLUMN);
        ddlService.execute(request);
        return R.ok();
    }

    @Operation(summary = "4. DDL-添加索引")
    @PostMapping(Route.DDL_ADD_INDEX)
    public R<Void> addIndex(@Valid @RequestBody DdlRequest request) {
        request.setDdlType(DdlType.ADD_INDEX);
        ddlService.execute(request);
        return R.ok();
    }

    @Operation(summary = "5. DDL-删除索引")
    @PostMapping(Route.DDL_DROP_INDEX)
    public R<Void> dropIndex(@Valid @RequestBody DdlRequest request) {
        request.setDdlType(DdlType.DROP_INDEX);
        ddlService.execute(request);
        return R.ok();
    }

    @Operation(summary = "6. DDL-重命名表")
    @PostMapping(Route.DDL_RENAME_TABLE)
    public R<Void> renameTable(@Valid @RequestBody DdlRequest request) {
        request.setDdlType(DdlType.RENAME_TABLE);
        ddlService.execute(request);
        return R.ok();
    }

    @Operation(summary = "7. DDL-修改表注释")
    @PostMapping(Route.DDL_COMMENT_TABLE)
    public R<Void> commentTable(@Valid @RequestBody DdlRequest request) {
        request.setDdlType(DdlType.COMMENT_TABLE);
        ddlService.execute(request);
        return R.ok();
    }

    @Operation(summary = "8. DDL-修改字段注释")
    @PostMapping(Route.DDL_COMMENT_COLUMN)
    public R<Void> commentColumn(@Valid @RequestBody DdlRequest request) {
        request.setDdlType(DdlType.COMMENT_COLUMN);
        ddlService.execute(request);
        return R.ok();
    }

    @Operation(summary = "9. DDL-预览")
    @PostMapping(Route.DDL_PREVIEW)
    public R<DdlPreview> preview(@Valid @RequestBody DdlRequest request) {
        DdlPreview preview = ddlService.preview(request);
        return R.ok(preview);
    }

    @Operation(summary = "10. DDL-执行DDL语句")
    @PostMapping(Route.DDL_EXECUTE_DDL)
    public R<Void> executeDdl(@Valid @RequestBody DdlExecuteRequest request) {
        ddlService.executeDdl(request.getDatasourceId(), request.getSchemaName(),
                request.getDdl(), request.getConfirmDangerous());
        return R.ok();
    }
}
