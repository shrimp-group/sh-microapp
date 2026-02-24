package com.wkclz.micro.k8s.helper;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.k8s.bean.entity.K8sConfig;
import com.wkclz.micro.k8s.mapper.K8sConfigMapper;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class KubeConfigHelper {

    private final static long CACHE_TIME = 10 * 60 * 1000L;
    private final static Map<String, Long> LAST_CACHE_TIME = new HashMap<>();
    private final static Map<String, ApiClient> CACHE_CLIENT = new HashMap<>();

    @Autowired
    private K8sConfigMapper k8sConfigMapper;

    public CoreV1Api getCoreV1Api(String clusterName) {
        ApiClient apiClient = getApiClient(clusterName);
        return new CoreV1Api(apiClient);
    }

    public AppsV1Api getAppsV1Api(String clusterName) {
        ApiClient apiClient = getApiClient(clusterName);
        return new AppsV1Api(apiClient);
    }

    public PolicyV1Api getPolicyV1Api(String clusterName) {
        ApiClient apiClient = getApiClient(clusterName);
        return new PolicyV1Api(apiClient);
    }

    public BatchV1Api getBatchV1Api(String clusterName) {
        ApiClient apiClient = getApiClient(clusterName);
        return new BatchV1Api(apiClient);
    }

    public StorageV1Api getStorageV1Api(String clusterName) {
        ApiClient apiClient = getApiClient(clusterName);
        return new StorageV1Api(apiClient);
    }

    public ApiextensionsV1Api getApiextensionsV1Api(String clusterName) {
        ApiClient apiClient = getApiClient(clusterName);
        return new ApiextensionsV1Api(apiClient);
    }

    public RbacAuthorizationV1Api getRbacAuthorizationV1Api(String clusterName) {
        ApiClient apiClient = getApiClient(clusterName);
        return new RbacAuthorizationV1Api(apiClient);
    }




    public NetworkingV1Api getNetworkingV1Api(String clusterName) {
        ApiClient apiClient = getApiClient(clusterName);
        return new NetworkingV1Api(apiClient);
    }


    public ApiClient getApiClient(String clusterName) {
        if (StringUtils.isBlank(clusterName)) {
            throw ValidationException.of("clusterName 不能为空");
        }

        long now = System.currentTimeMillis();
        Long cacheTime = LAST_CACHE_TIME.get(clusterName);
        ApiClient apiClient = CACHE_CLIENT.get(clusterName);
        if (cacheTime != null && apiClient != null && now - cacheTime <= CACHE_TIME) {
            return apiClient;
        }

        synchronized (KubeConfigHelper.class.getName().intern()) {
            now = System.currentTimeMillis();
            cacheTime = LAST_CACHE_TIME.get(clusterName);
            apiClient = CACHE_CLIENT.get(clusterName);
            if (cacheTime != null && apiClient != null && now - cacheTime <= CACHE_TIME) {
                return apiClient;
            }

            K8sConfig config = new K8sConfig();
            config.setClusterName(clusterName);
            config = k8sConfigMapper.selectOneByEntity(config);
            if (config == null) {
                throw ValidationException.of("{} 不存在，请先完成配置", clusterName);
            }

            String configSer = config.getKubeConfig();
            if (StringUtils.isBlank(configSer)) {
                throw ValidationException.of("{} 配置中没有包含授权信息，请检查", clusterName);
            }

            String configFile = writeConfigInfo(clusterName, config.getKubeConfig());
            try {
                FileReader fileReader = new FileReader(configFile);
                KubeConfig kubeConfig = KubeConfig.loadKubeConfig(fileReader);
                apiClient = ClientBuilder.kubeconfig(kubeConfig).build();

                LAST_CACHE_TIME.put(clusterName, now);
                CACHE_CLIENT.put(clusterName, apiClient);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (apiClient == null) {
            throw ValidationException.of("集群 {} 没有维护，请先添加集群", clusterName);
        }
        return apiClient;
    }



    private static String writeConfigInfo(String name, String ctx) {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(ctx)) {
            throw ValidationException.of("信息不全无法生成配置信息");
        }

        Object o = System.getProperties().get("user.dir");
        String userDir = o.toString();
        String savePath = userDir + "/tmp/kube/";
        String filePath = savePath + name;

        FileWriter writer = null;
        try {
            //文件保存位置
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(file);
            writer.write("");
            writer.write(ctx);
            writer.flush();
        } catch (IOException e) {
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    //
                }
            }
        }
        return filePath;
    }

}
