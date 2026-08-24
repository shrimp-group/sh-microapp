package com.wkclz.micro.k8s.helper;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.k8s.bean.entity.K8sConfig;
import com.wkclz.micro.k8s.mapper.K8sConfigMapper;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.*;
import io.kubernetes.client.util.KubeConfig;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

@Component
public class KubeConfigHelper {

    private static final Logger log = LoggerFactory.getLogger(KubeConfigHelper.class);

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

    public DiscoveryV1Api getDiscoveryV1Api(String clusterName) {
        ApiClient apiClient = getApiClient(clusterName);
        return new DiscoveryV1Api(apiClient);
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
            try (FileReader fileReader = new FileReader(configFile)) {
                KubeConfig kubeConfig = KubeConfig.loadKubeConfig(fileReader);
                apiClient = buildApiClient(kubeConfig);
                LAST_CACHE_TIME.put(clusterName, now);
                CACHE_CLIENT.put(clusterName, apiClient);
            } catch (Exception e) {
                throw new RuntimeException("创建集群 " + clusterName + " 的 ApiClient 失败", e);
            }
        }
        if (apiClient == null) {
            throw ValidationException.of("集群 {} 没有维护，请先添加集群", clusterName);
        }
        return apiClient;
    }

    private static ApiClient buildApiClient(KubeConfig kubeConfig) throws Exception {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(kubeConfig.getServer());

        Map<String, String> credentials = kubeConfig.getCredentials();
        if (credentials != null) {
            String accessToken = credentials.get(KubeConfig.CRED_TOKEN_KEY);
            if (StringUtils.isNotBlank(accessToken)) {
                apiClient.setAccessToken(accessToken);
            }
        }

        String caData = kubeConfig.getCertificateAuthorityData();
        String clientCertData = kubeConfig.getClientCertificateData();
        String clientKeyData = kubeConfig.getClientKeyData();

        boolean hasClientCert = StringUtils.isNotBlank(clientCertData) && StringUtils.isNotBlank(clientKeyData);
        boolean hasCa = StringUtils.isNotBlank(caData);

        if (hasClientCert) {
            configureClientCertSsl(apiClient, caData, clientCertData, clientKeyData);
        } else if (hasCa) {
            configureCaOnlySsl(apiClient, caData);
        } else {
            apiClient.setVerifyingSsl(false);
        }

        return apiClient;
    }

    private static void configureClientCertSsl(ApiClient apiClient, String caData, String clientCertData, String clientKeyData) {
        try {
            byte[] certPem = decodeKubeConfigData(clientCertData);
            byte[] keyPem = decodeKubeConfigData(clientKeyData);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate clientCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certPem));

            byte[] keyDer = pemToDer(keyPem);
            PrivateKey privateKey = parsePrivateKey(keyDer);

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setKeyEntry("client", privateKey, new char[0], new java.security.cert.Certificate[]{clientCert});

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, new char[0]);

            X509TrustManager trustManager;
            if (StringUtils.isNotBlank(caData)) {
                byte[] caPem = decodeKubeConfigData(caData);
                trustManager = buildTrustManager(caPem);
            } else {
                trustManager = buildInsecureTrustManager();
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), new TrustManager[]{trustManager}, new SecureRandom());

            OkHttpClient httpClient = apiClient.getHttpClient().newBuilder()
                .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                .hostnameVerifier((hostname, session) -> true)
                .build();
            apiClient.setHttpClient(httpClient);
        } catch (Exception e) {
            log.warn("客户端证书 SSL 配置失败，将禁用 SSL 验证", e);
            apiClient.setVerifyingSsl(false);
        }
    }

    private static void configureCaOnlySsl(ApiClient apiClient, String caData) {
        try {
            byte[] caPem = decodeKubeConfigData(caData);
            X509TrustManager trustManager = buildTrustManager(caPem);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());

            OkHttpClient httpClient = apiClient.getHttpClient().newBuilder()
                .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                .hostnameVerifier((hostname, session) -> true)
                .build();
            apiClient.setHttpClient(httpClient);
        } catch (Exception e) {
            log.warn("CA 证书 SSL 配置失败，将禁用 SSL 验证", e);
            apiClient.setVerifyingSsl(false);
        }
    }

    private static X509TrustManager buildTrustManager(byte[] caPem) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(caPem));

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", caCert);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }
        throw new IllegalStateException("No X509TrustManager found");
    }

    private static X509TrustManager buildInsecureTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private static byte[] decodeKubeConfigData(String data) {
        if (data == null) {
            return null;
        }
        String trimmed = data.trim();
        if (trimmed.startsWith("-----BEGIN")) {
            return trimmed.getBytes(StandardCharsets.UTF_8);
        }
        try {
            return Base64.getDecoder().decode(trimmed);
        } catch (IllegalArgumentException e) {
            return trimmed.getBytes(StandardCharsets.UTF_8);
        }
    }

    private static byte[] pemToDer(byte[] pemBytes) {
        String pem = new String(pemBytes, StandardCharsets.UTF_8);
        String content = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("-----BEGIN EC PRIVATE KEY-----", "")
            .replace("-----END EC PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        return Base64.getDecoder().decode(content);
    }

    private static PrivateKey parsePrivateKey(byte[] derBytes) throws Exception {
        Exception lastException = null;

        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(derBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            lastException = e;
        }

        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(derBytes);
            return KeyFactory.getInstance("EC").generatePrivate(keySpec);
        } catch (Exception e) {
            lastException = e;
        }

        try {
            byte[] pkcs8Wrapped = wrapRsaPkcs1ToPkcs8(derBytes);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Wrapped);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            lastException = e;
        }

        throw new RuntimeException("无法解析私钥，请确保私钥为 PKCS#8 格式", lastException);
    }

    private static byte[] wrapRsaPkcs1ToPkcs8(byte[] pkcs1Bytes) throws IOException {
        byte[] algorithmId = new byte[]{
            0x30, 0x0D,
            0x06, 0x09, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0x0D, 0x01, 0x01, 0x01,
            0x05, 0x00
        };
        byte[] version = new byte[]{0x02, 0x01, 0x00};

        ByteArrayOutputStream octetContent = new ByteArrayOutputStream();
        octetContent.write(0x04);
        octetContent.write(encodeAsn1Length(pkcs1Bytes.length));
        octetContent.write(pkcs1Bytes);

        int seqContentLen = version.length + algorithmId.length + octetContent.size();
        ByteArrayOutputStream seq = new ByteArrayOutputStream();
        seq.write(0x30);
        seq.write(encodeAsn1Length(seqContentLen));
        seq.write(version);
        seq.write(algorithmId);
        seq.write(octetContent.toByteArray());

        return seq.toByteArray();
    }

    private static byte[] encodeAsn1Length(int length) throws IOException {
        if (length < 128) {
            return new byte[]{(byte) length};
        } else if (length < 256) {
            return new byte[]{(byte) 0x81, (byte) length};
        } else if (length < 65536) {
            return new byte[]{(byte) 0x82, (byte) (length >> 8), (byte) length};
        }
        throw new IOException("ASN.1 length too long: " + length);
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
