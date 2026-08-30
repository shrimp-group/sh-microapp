# micro-k8s 模块开发指南

## 模块概述

micro-k8s 是 sh-microapp 微应用集合中的 Kubernetes 管理模块，通过 io.kubernetes.client-java（v26.0.0）连接 K8s 集群，提供集群配置维护、资源 Kind 通用 CRUD、Pod 滚动日志查看能力。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-k8s`
- **API 前缀**: `/micro-k8s`
- **外部依赖**: `io.kubernetes:client-java`（版本由 sh-bom 统一管理）

## 架构设计

```
micro-k8s（管理端）
  ├── K8sConfig          # 集群配置（kubeConfig 落库）
  ├── K8sKind CRUD       # 按 Kind 分发到 K8sApi 实现（Pod/Deployment/Service 等 30+ 种）
  ├── K8sCluster         # 集群级查询（节点/命名空间）
  └── Pod 滚动日志        # SSE 流式输出（PodLogs + SseEmitter）
        ↓ io.kubernetes.client-java
K8s 集群
```

## 目录结构

```
src/main/java/com/wkclz/micro/k8s/
├── K8sAutoConfig.java              # 自动配置（@ComponentScan + @MapperScan）
├── Route.java                      # 路由常量
├── bean/
│   ├── entity/                     # K8sConfig 实体
│   ├── kube/                       # K8sParam / Kind 枚举
│   ├── req/                        # 请求 Bean（含 K8sPodLogReq）
│   └── resp/                       # 响应 Bean
├── custom/
│   ├── K8sApi.java                 # Kind 统一接口（list/yaml/create/update/delete）
│   └── impl/                       # 各 Kind 实现（K8sPodImpl 等）
├── helper/
│   ├── KubeConfigHelper.java       # ApiClient 构建与缓存（10 分钟）
│   └── K8sHelper.java              # kind -> impl Bean 分发
├── mapper/                         # K8sConfigMapper
├── rest/
│   ├── K8sConfigRest.java          # 集群配置 CRUD
│   ├── K8sRest.java                # 集群查询
│   ├── K8sKindRest.java            # Kind 资源 CRUD
│   └── K8sPodRest.java             # Pod 操作（滚动日志 SSE）
├── service/
│   ├── K8sConfigService.java
│   ├── K8sClusterService.java
│   ├── K8sKindService.java
│   └── K8sPodLogService.java       # Pod 日志流式转发
└── utils/
    └── YamlUtil.java
```

## API 端点清单

| 分组 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 配置 | GET | `/micro-k8s/config/page` | 集群配置分页 |
| 配置 | POST | `/micro-k8s/config/create` | 新增集群配置 |
| 集群 | GET | `/micro-k8s/cluster/nodes` | 节点列表 |
| 集群 | GET | `/micro-k8s/cluster/namespaces/briefly` | 命名空间简要列表 |
| Kind | GET | `/micro-k8s/cluster/kind/list` | Kind 资源列表 |
| Kind | GET | `/micro-k8s/cluster/kind/yaml` | Kind 资源 YAML |
| Kind | POST | `/micro-k8s/cluster/kind/create` | 按 YAML 创建 |
| Kind | POST | `/micro-k8s/cluster/kind/update` | 按 YAML 更新 |
| Kind | POST | `/micro-k8s/cluster/kind/delete` | 删除资源 |
| Pod | GET | `/micro-k8s/pod/log` | Pod 滚动日志（SSE） |

## Pod 滚动日志约定

接口：`GET /micro-k8s/pod/log`（`produces = text/event-stream`，返回 `SseEmitter`）

参数（`K8sPodLogReq`）：

| 参数 | 必填 | 说明 |
|------|------|------|
| clusterName | 是 | 集群名称 |
| namespace | 是 | 命名空间 |
| name | 是 | Pod 名称 |
| containerName | 否 | 容器名称，为空时取第一个容器 |
| tailLines | 否 | 尾部行数 |
| timestamps | 否 | 是否携带时间戳 |
| sinceSeconds | 否 | 从 N 秒前开始 |

约定：

1. 底层使用 `io.kubernetes.client.PodLogs.streamNamespacedPodLog(namespace, name, container, sinceSeconds, tailLines, timestamps)`（内部 `follow=true`）获取 InputStream
2. 正常日志以 SSE `data:` 帧逐行下发；异常通过 SSE `event: error` 帧下发后结束
3. `SseEmitter(0L)` 不超时；onCompletion/onTimeout/onError 均关闭底层 InputStream，避免连接泄漏
4. 流式读取在独立线程池 `LOG_STREAM_EXECUTOR` 中执行，控制器立即返回 emitter
5. **流式客户端**：日志读取必须使用 `KubeConfigHelper.getStreamLogApiClient(clusterName)`，该客户端基于缓存客户端克隆并关闭读超时（`readTimeout=0`），且**强制 HTTP/1.1**（`protocols(HTTP_1_1)`）。缓存客户端默认 10s 读超时，follow 流在无新日志时静默超过 10s 会触发 OkHttp HTTP/2 StreamTimeout（`SocketTimeoutException`）导致流中断；HTTP/2 长连接下 apiserver 的 follow 数据推送不稳定，强制 HTTP/1.1（chunked）可保证新日志实时下发
6. **响应缓存过滤器**：宿主应用若引入 iam-session，其 `RequestRecordFilter` 的 `ContentCachingResponseWrapper` 有两个与 SSE 不兼容的问题——写入先进缓存（异步线程在 `copyBodyToResponse()` 之后写入的数据滞留，前端收不到新日志）；且 `flushBuffer()` 为 no-op（SseEmitter 每次 send 后调用的 `flushBuffer()` 无法到达容器，数据攒满 8KB 才发送，页面日志不及时）。现已按 `Accept: text/event-stream` 识别流式请求并**跳过 response 包装**（SSE 直连原始 response，flush 实时生效），普通请求仍缓存记录；前端 `streamPodLog()` 需携带 `Accept: text/event-stream` 头。需宿主重新 install iam-session 生效
7. **客户端断开处理**：前端停止/关闭页面时 SSE 连接断开属预期行为——`emitter.send` 抛 `IllegalStateException/IOException`（记 info 并 break 释放流）；同时 sh-web 的 `ErrorHandler` 需对 `AsyncRequestNotUsableException`/`ClientAbortException`/`Broken pipe` 等"客户端断开"类异常降级为 warn，避免误报 ERROR 与告警邮件
8. 前端配套：`streamPodLog()`（原生 fetch + ReadableStream 解析 SSE，规避 axios 超时）与 `PodLogDialog` 组件

## 依赖关系

- **io.kubernetes:client-java**：K8s API 客户端（CoreV1Api / PodLogs 等），版本由 sh-bom 统一管理
- **sh-framework**：BaseEntity / BaseService / BaseMapper / R / ValidationException 等基础能力

## 开发注意事项

1. **集群配置**：kubeConfig 以字符串落库，`KubeConfigHelper` 解析并构建 ApiClient，按 clusterName 缓存 10 分钟；支持客户端证书、CA 单向认证与 Bearer Token（`setApiKey` + `setApiKeyPrefix("Bearer")`，client-java v26 的 `setAccessToken` 已废弃抛异常，不可用）
2. **Kind 分发**：新增 Kind 需在 `Kind` 枚举登记，并实现 `K8sApi` 接口，Bean 名约定 `k8s{Kind}Impl`
3. **命名空间**：ClusterRole/ClusterRoleBinding/Namespace/CustomResourceDefinition 等集群级资源无需 namespace，其余 Kind 必传
4. **SSE 端点**：不经过 `R<T>` 包装，返回体为 `text/event-stream`；错误事件用 `event: error` 下发
5. **异常处理**：统一使用 `ValidationException.of("消息")`，REST 返回 `R<T>`
