package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.flowable.bean.entity.MdmFlowableErrorLog;
import com.wkclz.micro.flowable.bean.req.ErrorHandleReq;
import com.wkclz.micro.flowable.bean.req.ErrorPageReq;
import com.wkclz.micro.flowable.bean.resp.ErrorLogResp;
import com.wkclz.micro.flowable.service.MdmFlowableErrorLogService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "异常监控", description = "异常日志查询与处理")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class ErrorLogRest {

    private static final Logger log = LoggerFactory.getLogger(ErrorLogRest.class);

    @Autowired
    private MdmFlowableErrorLogService errorLogService;

    @Operation(summary = "异常日志分页")
    @GetMapping(Route.ERROR_PAGE)
    public R<PageData<ErrorLogResp>> page(@Valid ErrorPageReq req) {
        MdmFlowableErrorLog entity = BeanUtil.cp(req, MdmFlowableErrorLog.class);
        PageData<MdmFlowableErrorLog> page = errorLogService.getErrorLogPage(entity);
        return R.ok(page.convert(ErrorLogResp.class));
    }

    @Operation(summary = "异常日志详情")
    @GetMapping(Route.ERROR_INFO)
    public R<ErrorLogResp> info(@Valid IdReq req) {
        MdmFlowableErrorLog errorLog = errorLogService.selectById(req.getId());
        if (errorLog == null) {
            throw ValidationException.of("异常日志不存在");
        }
        return R.ok(BeanUtil.cp(errorLog, ErrorLogResp.class));
    }

    @Operation(summary = "标记异常处理状态")
    @PostMapping(Route.ERROR_HANDLE)
    public R<Integer> handle(@Valid @RequestBody ErrorHandleReq req) {
        log.info("处理异常日志: id={}, handleStatus={}", req.getId(), req.getHandleStatus());
        MdmFlowableErrorLog errorLog = errorLogService.selectById(req.getId());
        if (errorLog == null) {
            throw ValidationException.of("异常日志不存在");
        }
        MdmFlowableErrorLog update = new MdmFlowableErrorLog();
        update.setId(req.getId());
        update.setHandleStatus(req.getHandleStatus());
        update.setRemark(req.getRemark());
        update.setVersion(errorLog.getVersion());
        errorLogService.updateByIdSelective(update);
        return R.ok(1);
    }
}
