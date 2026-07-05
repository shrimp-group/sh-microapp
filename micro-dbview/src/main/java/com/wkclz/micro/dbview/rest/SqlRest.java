package com.wkclz.micro.dbview.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.dbview.bean.dto.SqlExecuteRequest;
import com.wkclz.micro.dbview.bean.dto.SqlResult;
import com.wkclz.micro.dbview.bean.entity.DbviewSqlHistory;
import com.wkclz.micro.dbview.bean.req.SqlHistoryPageReq;
import com.wkclz.micro.dbview.bean.resp.SqlHistoryResp;
import com.wkclz.micro.dbview.service.DbviewSqlService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "SQL执行", description = "SQL执行与历史查询接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class SqlRest {

    @Autowired
    private DbviewSqlService sqlService;

    @Operation(summary = "1. SQL-执行")
    @PostMapping(Route.SQL_EXECUTE)
    public R<SqlResult> execute(@Valid @RequestBody SqlExecuteRequest request) {
        SqlResult result = sqlService.execute(request);
        return R.ok(result);
    }

    @Operation(summary = "2. SQL-执行历史分页")
    @GetMapping(Route.SQL_HISTORY_PAGE)
    public R<PageData<SqlHistoryResp>> historyPage(@Valid SqlHistoryPageReq req) {
        DbviewSqlHistory entity = BeanUtil.cp(req, DbviewSqlHistory.class);
        PageData<DbviewSqlHistory> page = sqlService.getHistoryPage(entity);
        PageData<SqlHistoryResp> newPage = page.convert(SqlHistoryResp.class);
        return R.ok(newPage);
    }
}
