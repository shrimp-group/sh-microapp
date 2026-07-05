package com.wkclz.micro.material.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.material.bean.entity.MdmMaterialTransferLog;
import com.wkclz.micro.material.bean.req.MaterialTransferCreateReq;
import com.wkclz.micro.material.bean.req.MaterialTransferLogReq;
import com.wkclz.micro.material.bean.resp.MaterialTransferLogResp;
import com.wkclz.micro.material.service.MdmMaterialTransferLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "5.素材转移", description = "素材转移管理接口")
@Slf4j
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class MaterialTransferRest {

    @Autowired
    private MdmMaterialTransferLogService mdmMaterialTransferLogService;

    @Operation(summary = "26.转移-执行", description = "执行素材所有权转移")
    @PostMapping(Route.TRANSFER_CREATE)
    public R<Integer> create(@Valid @RequestBody MaterialTransferCreateReq req) {
        log.info("转移-执行, ids: {}, toUserCode: {}", req.getIds(), req.getToUserCode());
        Integer result = mdmMaterialTransferLogService.transfer(req.getIds(), req.getToUserCode());
        return R.ok(result);
    }

    @Operation(summary = "27.转移-记录", description = "查询素材转移记录")
    @GetMapping(Route.TRANSFER_LOG)
    public R<List<MaterialTransferLogResp>> log(@Valid MaterialTransferLogReq req) {
        List<MdmMaterialTransferLog> logs = mdmMaterialTransferLogService.listByMaterialCode(req.getMaterialCode());
        List<MaterialTransferLogResp> respList = logs.stream().map(logEntry -> {
            MaterialTransferLogResp resp = new MaterialTransferLogResp();
            resp.setId(logEntry.getId());
            resp.setMaterialCode(logEntry.getMaterialCode());
            resp.setFromUserCode(logEntry.getFromUserCode());
            resp.setToUserCode(logEntry.getToUserCode());
            resp.setOperatorCode(logEntry.getOperatorCode());
            resp.setCreateTime(logEntry.getCreateTime() != null ? logEntry.getCreateTime().toString() : null);
            return resp;
        }).collect(Collectors.toList());
        return R.ok(respList);
    }
}
