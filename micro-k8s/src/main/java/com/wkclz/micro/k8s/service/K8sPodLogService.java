package com.wkclz.micro.k8s.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.k8s.bean.req.K8sPodLogReq;
import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class K8sPodLogService {

    private static final ExecutorService LOG_STREAM_EXECUTOR = Executors.newCachedThreadPool();

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public SseEmitter stream(K8sPodLogReq req) {
        log.info("Pod日志滚动查询开始, clusterName: {}, namespace: {}, name: {}, containerName: {}, tailLines: {}, timestamps: {}",
            req.getClusterName(), req.getNamespace(), req.getName(), req.getContainerName(), req.getTailLines(), req.getTimestamps());

        SseEmitter emitter = new SseEmitter(0L);
        AtomicReference<InputStream> streamRef = new AtomicReference<>();

        emitter.onCompletion(() -> closeStream(streamRef));
        emitter.onTimeout(() -> closeStream(streamRef));
        emitter.onError(e -> closeStream(streamRef));

        LOG_STREAM_EXECUTOR.submit(() -> streamLog(req, emitter, streamRef));
        return emitter;
    }

    private void streamLog(K8sPodLogReq req, SseEmitter emitter, AtomicReference<InputStream> streamRef) {
        try {
            ApiClient apiClient = kubeConfigHelper.getApiClient(req.getClusterName());

            String containerName = req.getContainerName();
            if (containerName == null || containerName.isBlank()) {
                V1Pod pod = new CoreV1Api(apiClient).readNamespacedPod(req.getName(), req.getNamespace()).execute();
                List<V1Container> containers = pod.getSpec().getContainers();
                if (containers == null || containers.isEmpty()) {
                    throw ValidationException.of("Pod {} 没有可用的容器", req.getName());
                }
                containerName = containers.get(0).getName();
            }

            InputStream inputStream = new PodLogs(apiClient).streamNamespacedPodLog(
                req.getNamespace(), req.getName(), containerName,
                req.getSinceSeconds(), req.getTailLines(), Boolean.TRUE.equals(req.getTimestamps()));
            streamRef.set(inputStream);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        emitter.send(line);
                    } catch (IllegalStateException | IOException e) {
                        log.info("Pod日志SSE连接已断开, namespace: {}, name: {}, container: {}",
                            req.getNamespace(), req.getName(), containerName);
                        break;
                    }
                }
            }
        } catch (ApiException e) {
            log.error("Pod日志查询失败, clusterName: {}, namespace: {}, name: {}, code: {}, body: {}",
                req.getClusterName(), req.getNamespace(), req.getName(), e.getCode(), e.getResponseBody(), e);
            sendError(emitter, "Pod日志查询失败: " + e.getResponseBody());
        } catch (Exception e) {
            log.error("Pod日志查询失败, clusterName: {}, namespace: {}, name: {}",
                req.getClusterName(), req.getNamespace(), req.getName(), e);
            sendError(emitter, "Pod日志查询失败: " + e.getMessage());
        } finally {
            closeStream(streamRef);
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("Pod日志SSE complete 异常, namespace: {}, name: {}", req.getNamespace(), req.getName(), e);
            }
        }
    }

    private void sendError(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
        } catch (Exception e) {
            log.warn("Pod日志SSE 发送错误事件失败: {}", e.getMessage());
        }
    }

    private void closeStream(AtomicReference<InputStream> streamRef) {
        InputStream inputStream = streamRef.getAndSet(null);
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("关闭Pod日志流失败", e);
            }
        }
    }
}
