package com.wkclz.micro.flowable.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.flowable.bean.entity.FlowableErrorLog;
import com.wkclz.micro.flowable.bean.enums.ErrorType;
import com.wkclz.micro.flowable.config.FlowableErrorLogProperties;
import com.wkclz.micro.flowable.service.FlowableErrorLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.service.annotation.GetExchange;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * FlowableClient 调用切面。
 * 统一处理 client 调用异常：落库 error_log + 抛出业务异常。
 * ErrorType 按约定推导，无需在 client 接口上标注：
 * GET → QUERY_ERROR；POST 且方法名 start 前缀 → START_ERROR；deploy、remove 前缀 → DEPLOY_ERROR；其余 POST → APPROVE_ERROR
 */
@Aspect
@Component
public class FlowableClientAspect {

    private static final Logger log = LoggerFactory.getLogger(FlowableClientAspect.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private FlowableErrorLogService errorLogService;
    @Autowired
    private FlowableErrorLogProperties errorLogProperties;

    /**
     * 拦截 sh-flowable client 全部接口调用：入口打日志，失败/异常时落库 error_log 并抛业务异常
     */
    @Around("execution(* com.wkclz.flowable.client.flowable.*Client.*(..))")
    public Object aroundClientCall(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        String clientMethod = signature.getDeclaringType().getSimpleName() + "#" + method.getName();
        ErrorType errorType = resolveErrorType(method);
        Object[] args = pjp.getArgs();
        Object requestData = args.length > 0 ? args[0] : null;
        try {
            log.info("调用 flowable client: method={}", clientMethod);
            Object result = pjp.proceed();
            if (!(result instanceof R<?> r)) {
                String msg = "client 返回 null";
                log.error("flowable client 调用失败: method={}, msg={}", clientMethod, msg);
                saveErrorLog(errorType, clientMethod, requestData, msg, null);
                throw ValidationException.of("流程服务调用失败: {}", msg);
            }
            if (r.getCode() != 200) {
                String msg = r.getMsg();
                log.error("flowable client 调用失败: method={}, msg={}", clientMethod, msg);
                saveErrorLog(errorType, clientMethod, requestData, msg, null);
                throw ValidationException.of("流程服务调用失败: {}", msg);
            }
            return result;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            String detail = e.getMessage();
            if (e instanceof RestClientResponseException restEx) {
                String body = restEx.getResponseBodyAsString();
                if (body != null && body.length() > 500) {
                    body = body.substring(0, 500);
                }
                detail = "httpStatus=" + restEx.getStatusCode() + ", body=" + body;
            }
            log.error("flowable client 调用异常: method={}, detail={}", clientMethod, detail, e);
            saveErrorLog(errorType, clientMethod, requestData, detail, e);
            throw ValidationException.of("流程服务调用异常: {}", detail);
        }
    }

    /**
     * 按约定推导错误类型：GET → QUERY_ERROR；POST 且方法名 start 前缀 → START_ERROR；
     * deploy、remove 前缀 → DEPLOY_ERROR；其余 POST → APPROVE_ERROR
     */
    private ErrorType resolveErrorType(Method method) {
        if (method.isAnnotationPresent(GetExchange.class)) {
            return ErrorType.QUERY_ERROR;
        }
        String name = method.getName();
        if (name.startsWith("start")) {
            return ErrorType.START_ERROR;
        }
        if (name.startsWith("deploy") || name.startsWith("remove")) {
            return ErrorType.DEPLOY_ERROR;
        }
        return ErrorType.APPROVE_ERROR;
    }

    /**
     * 保存调用异常日志（语义迁移自 FlowableClientWrapper.saveErrorLog）
     */
    private void saveErrorLog(ErrorType errorType, String clientMethod, Object requestData, String errorMessage, Exception e) {
        if (errorLogProperties.getIncludeStack() != 1) {
            return;
        }
        try {
            FlowableErrorLog errorLog = new FlowableErrorLog();
            errorLog.setErrorType(errorType.name());
            errorLog.setClientMethod(clientMethod);
            errorLog.setErrorMessage(errorMessage != null && errorMessage.length() > 1023 ? errorMessage.substring(0, 1023) : errorMessage);
            errorLog.setOccurTime(LocalDateTime.now());
            errorLog.setHandleStatus("PENDING");
            if (requestData != null) {
                errorLog.setRequestData(OBJECT_MAPPER.writeValueAsString(requestData));
            }
            if (e != null && errorLogProperties.getIncludeStack() == 1) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                errorLog.setErrorStack(sw.toString());
            }
            errorLogService.insert(errorLog);
        } catch (Exception ex) {
            log.error("保存异常日志失败", ex);
        }
    }
}
