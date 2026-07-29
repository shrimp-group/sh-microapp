package com.wkclz.micro.flowable.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.flowable.client.config.FlowableClient;
import com.wkclz.micro.flowable.bean.entity.MdmFlowableErrorLog;
import com.wkclz.micro.flowable.bean.enums.ErrorType;
import com.wkclz.micro.flowable.config.FlowableErrorLogProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * FlowableClient 调用包装层。
 * 统一处理 client 调用异常：落库 error_log + 抛出业务异常。
 */
@Service
public class FlowableClientWrapper {

    private static final Logger log = LoggerFactory.getLogger(FlowableClientWrapper.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private FlowableClient flowableClient;
    @Autowired
    private MdmFlowableErrorLogService errorLogService;
    @Autowired
    private FlowableErrorLogProperties errorLogProperties;

    public FlowableClient getClient() {
        return flowableClient;
    }

    /**
     * 包装 client 调用，异常时落库 error_log 并抛出业务异常
     */
    public <T> R<T> call(ErrorType errorType, String clientMethod, Object requestData, java.util.function.Supplier<R<T>> supplier) {
        try {
            log.info("调用 flowable client: method={}", clientMethod);
            R<T> result = supplier.get();
            if (result == null || result.getCode() != 200) {
                String msg = result != null ? result.getMsg() : "client 返回 null";
                log.error("flowable client 调用失败: method={}, msg={}", clientMethod, msg);
                saveErrorLog(errorType, clientMethod, requestData, msg, null);
                throw ValidationException.of("流程服务调用失败: {}", msg);
            }
            return result;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("flowable client 调用异常: method={}", clientMethod, e);
            saveErrorLog(errorType, clientMethod, requestData, e.getMessage(), e);
            throw ValidationException.of("流程服务调用异常: {}", e.getMessage());
        }
    }

    private void saveErrorLog(ErrorType errorType, String clientMethod, Object requestData, String errorMessage, Exception e) {
        if (!errorLogProperties.isEnabled()) {
            return;
        }
        try {
            MdmFlowableErrorLog errorLog = new MdmFlowableErrorLog();
            errorLog.setErrorType(errorType.name());
            errorLog.setClientMethod(clientMethod);
            errorLog.setErrorMessage(errorMessage != null && errorMessage.length() > 1023 ? errorMessage.substring(0, 1023) : errorMessage);
            errorLog.setOccurTime(LocalDateTime.now());
            errorLog.setHandleStatus("PENDING");
            if (requestData != null) {
                errorLog.setRequestData(OBJECT_MAPPER.writeValueAsString(requestData));
            }
            if (e != null && errorLogProperties.isIncludeStack()) {
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
