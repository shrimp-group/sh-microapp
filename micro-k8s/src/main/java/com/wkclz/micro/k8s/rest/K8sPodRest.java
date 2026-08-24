package com.wkclz.micro.k8s.rest;

import com.wkclz.micro.k8s.Route;
import com.wkclz.micro.k8s.bean.req.K8sPodLogReq;
import com.wkclz.micro.k8s.service.K8sPodLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "4.K8s Pod操作", description = "K8s Pod操作接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class K8sPodRest {

    @Autowired
    private K8sPodLogService k8sPodLogService;

    @Operation(summary = "1.Pod-滚动查看日志", description = "以SSE流式方式滚动查看Pod日志")
    @GetMapping(value = Route.POD_LOG, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter podLog(@Valid K8sPodLogReq req) {
        return k8sPodLogService.stream(req);
    }
}
