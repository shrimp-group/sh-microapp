package com.wkclz.micro.audit.rest;

import com.alibaba.fastjson2.JSONObject;
import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.audit.bean.entity.MdmChangeLog;
import com.wkclz.micro.audit.bean.req.ChangeLogInfoReq;
import com.wkclz.micro.audit.bean.req.ChangeLogPageReq;
import com.wkclz.micro.audit.bean.resp.ChangeLogInfoResp;
import com.wkclz.micro.audit.bean.resp.ChangeLogPageResp;
import com.wkclz.micro.audit.service.MdmChangeLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import com.wkclz.tool.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "1.变更记录", description = "变更记录查询接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class ChangeLogRest {

    @Autowired
    private MdmChangeLogService mdmChangeLogService;

    @Operation(summary = "1.变更记录-分页查询", description = "根据条件分页查询变更记录列表")
    @GetMapping(Route.CHANGE_LOG_PAGE)
    public R<PageData<ChangeLogPageResp>> mdmChangeLogPage(@Valid ChangeLogPageReq req) {
        MdmChangeLog entity = BeanUtil.cp(req, MdmChangeLog.class);
        PageData<MdmChangeLog> page = mdmChangeLogService.getChangeLogPage(entity);
        List<ChangeLogPageResp> respList = page.getRecords().stream().map(e -> {
            ChangeLogPageResp resp = BeanUtil.cp(e, ChangeLogPageResp.class);
            if (StringUtils.isNotBlank(e.getDataFrom())) {
                resp.setDataFromEntity(JSONObject.parseObject(e.getDataFrom()));
            }
            if (StringUtils.isNotBlank(e.getDataTo())) {
                resp.setDataToEntity(JSONObject.parseObject(e.getDataTo()));
            }
            return resp;
        }).toList();
        PageData<ChangeLogPageResp> newPage = PageData.of(respList, page.getTotal(), page.getCurrent(), page.getSize());
        return R.ok(newPage);
    }

    @Operation(summary = "2.变更记录-详情", description = "根据ID查询变更记录详情")
    @GetMapping(Route.CHANGE_LOG_INFO)
    public R<ChangeLogInfoResp> mdmChangeLogInfo(@Valid ChangeLogInfoReq req) {
        MdmChangeLog entity = mdmChangeLogService.selectById(req.getId());
        ChangeLogInfoResp resp = BeanUtil.cp(entity, ChangeLogInfoResp.class);
        if (StringUtils.isNotBlank(entity.getDataFrom())) {
            resp.setDataFromEntity(JSONObject.parseObject(entity.getDataFrom()));
        }
        if (StringUtils.isNotBlank(entity.getDataTo())) {
            resp.setDataToEntity(JSONObject.parseObject(entity.getDataTo()));
        }
        return R.ok(resp);
    }

}
