# micro-fileos 模块开发指南

帮助开发者快速理解 `micro-fileos` 模块的架构设计、核心功能和开发规范。

## 📦 模块概述

基于 Spring Boot 的完整文件存储服务，支持阿里云 OSS、AWS S3、S3 兼容协议，通过策略模式实现无缝切换。

**核心特性**: 多存储提供商 · 多Bucket路由 · Hash去重 · 目录管理 · 分片上传下载 · 图片处理 · 前端直传

---

## 🏗️ 架构设计

```
Controller      FileosUploadRest / FileosSignRest / FileosDownloadRest / FileosDeleteRest
                FileosPresignRest / FileosBucketRest / FileosDirectoryRest / FileosRecordRest
    ↓
API Interface   FileosUploadApi / FileosSignApi / FileosDownloadApi / FileosDeleteApi / FileosPresignUploadApi
    ↓
API Impl        *ApiImpl (extends AbstractFileosApi — 共享依赖)
    ↓
Service         FileosService → AliOssServiceImpl / S3ServiceImpl
                MdmFileosBucketService / MdmFileosRecordService / MdmFileosDirectoryService / MdmFileosMultipartService
    ↓
Mapper          MdmFileosBucketMapper / MdmFileosRecordMapper / MdmFileosDirectoryMapper / MdmFileosMultipartMapper
    ↓
Cache           BucketCache (Redis Pub/Sub)
Helper          PathHelper / DirectoryHelper / FileHashHelper / FileTypeHelper / ImageProcessHelper
```

| 模式 | 应用 | 说明 |
|------|------|------|
| 策略模式 | `FileosService` + 两种实现 | 根据 bucket.oss_sp 动态路由 |
| 模板方法 | `AbstractFileosApi` | 封装 Bucket 查找、服务路由、URL 解析、文件校验、目录维护 |
| Cache | `BucketCache` | Redis Pub/Sub + 本地 Map，3 秒防抖 |

---

## 📁 目录结构

```
micro-fileos/src/main/java/com/wkclz/micro/fileos/
├── FileosAutoConfig.java              # @ComponentScan + @MapperScan
├── api/
│   ├── FileosUploadApi.java           # 上传接口（简单上传 + 分片上传）
│   ├── FileosSignApi.java             # 签名接口（单文件/批量/富文本/泛型）
│   ├── FileosDownloadApi.java         # 下载接口（全量/范围）
│   ├── FileosDeleteApi.java           # 删除接口（单个/批量）
│   ├── FileosPresignUploadApi.java    # 预签名上传接口（简单/分片/完成确认）
│   └── impl/
│       ├── AbstractFileosApi.java     # 基类：Bucket 查找 / 服务路由 / URL 解析 / 文件校验
│       ├── FileosUploadApiImpl.java
│       ├── FileosSignApiImpl.java
│       ├── FileosDownloadApiImpl.java
│       ├── FileosDeleteApiImpl.java
│       └── FileosPresignUploadApiImpl.java
├── bean/
│   ├── FileosConstant.java            # 常量（PUBLIC_PREFIX = "public/"）
│   ├── entity/
│   │   ├── MdmFileosBucket.java       # Bucket 配置
│   │   ├── MdmFileosRecord.java       # 文件记录
│   │   ├── MdmFileosDirectory.java    # 目录结构
│   │   └── MdmFileosMultipart.java    # 分片上传记录
│   ├── dto/
│   │   ├── FileosUploadRequest.java   # 上传请求
│   │   ├── MdmFileosRecordDto.java    # 文件记录 DTO
│   │   ├── MdmFileosBucketDto.java    # Bucket DTO
│   │   ├── MdmFileosDirectoryDto.java # 目录 DTO
│   │   ├── PresignUploadRequest.java  # 预签名上传请求
│   │   ├── PresignUploadResponse.java # 预签名上传响应
│   │   ├── PresignCompleteRequest.java # 预签名完成确认请求
│   │   ├── MultipartUploadInitRequest.java  # 分片上传初始化请求
│   │   ├── MultipartUploadInitResponse.java # 分片上传初始化响应
│   │   ├── MultipartCompleteRequest.java    # 分片上传完成请求
│   │   ├── PresignedPartInfo.java     # 预签名分片信息
│   │   ├── CompletedPartInfo.java     # 已完成分片信息
│   │   └── ImageProcessParam.java     # 图片处理参数（resize/crop/watermark）
│   └── enums/
│       ├── OssSpEnum.java             # ALI_OSS / AWS_S3 / S3_COMPATIBLE
│       ├── UploadTypeEnum.java        # SIMPLE / MULTIPART / PRESIGN
│       ├── UploadStatusEnum.java      # UPLOADING / COMPLETED / ABORTED
│       └── ContentTypeEnum.java       # 300+ MIME 类型映射
├── config/
│   └── FileosConfig.java             # 文件大小/预签名/Hash/分片配置
├── helper/
│   ├── BucketCache.java              # Redis Pub/Sub 缓存监听
│   ├── PathHelper.java               # 路径生成（system/env/category/day/seq）
│   ├── DirectoryHelper.java          # 目录自动维护（异步）
│   ├── FileHashHelper.java           # 文件 Hash 计算（SHA-256）
│   ├── FileTypeHelper.java           # Magic Bytes 文件验证
│   └── ImageProcessHelper.java       # 图片处理参数构建（阿里云 OSS）
├── job/
│   └── MultipartCleanupJob.java      # 分片上传过期清理（XXL-Job）
├── mapper/
│   ├── MdmFileosBucketMapper.java
│   ├── MdmFileosRecordMapper.java
│   ├── MdmFileosDirectoryMapper.java
│   └── MdmFileosMultipartMapper.java
├── rest/
│   ├── Route.java                    # 路由常量（@Router 注解）
│   ├── FileosUploadRest.java         # 上传 REST
│   ├── FileosSignRest.java           # 签名 REST
│   ├── FileosDownloadRest.java       # 下载 REST
│   ├── FileosPresignRest.java        # 预签名 REST
│   ├── FileosBucketRest.java         # Bucket 管理 REST
│   ├── FileosDirectoryRest.java      # 目录管理 REST
│   └── FileosRecordRest.java         # 文件记录 REST
├── service/
│   ├── FileosService.java            # OSS 操作接口
│   ├── impl/
│   │   ├── AliOssServiceImpl.java    # 阿里云 OSS 实现
│   │   └── S3ServiceImpl.java        # S3/S3兼容 实现
│   ├── MdmFileosBucketService.java
│   ├── MdmFileosRecordService.java
│   ├── MdmFileosDirectoryService.java
│   └── MdmFileosMultipartService.java
└── utils/
    └── OssUtil.java                  # 文件名安全处理 + ContentType 解析
```

---

## 🔑 核心组件

### REST API（前缀 `/micro-fileos`）

| 端点 | 方法 | 说明 |
|------|------|------|
| `/bucket/page` | GET | Bucket 分页 |
| `/bucket/info` | GET | Bucket 详情 |
| `/bucket/create` | POST | 创建 Bucket |
| `/bucket/update` | POST | 更新 Bucket |
| `/bucket/remove` | POST | 删除 Bucket |
| `/bucket/options` | GET | Bucket 选项列表 |
| `/directory/list` | GET | 目录列表 |
| `/directory/tree` | GET | 目录树 |
| `/directory/info` | GET | 目录详情 |
| `/upload/simple` | POST | 简单上传 |
| `/upload/simple/public` | POST | 公开上传（无需权限） |
| `/upload/multipart/init` | POST | 分片上传-初始化 |
| `/upload/multipart/complete` | POST | 分片上传-完成 |
| `/upload/multipart/abort` | POST | 分片上传-中止 |
| `/download/{fileId}` | GET | 文件下载 |
| `/presign/upload` | POST | 预签名-简单上传 |
| `/presign/upload/batch` | POST | 预签名-批量简单上传 |
| `/presign/multipart/init` | POST | 预签名-分片上传初始化 |
| `/presign/multipart/complete` | POST | 预签名-分片上传完成 |
| `/presign/multipart/abort` | POST | 预签名-分片上传中止 |
| `/presign/complete` | POST | 预签名-简单上传完成确认 |
| `/presign/complete/batch` | POST | 预签名-简单上传完成确认-批量 |
| `/sign/url` | GET | 单文件签名 |
| `/sign/urls` | GET | 多文件签名 |
| `/record/page` | GET | 文件记录分页 |
| `/record/info` | GET | 文件记录详情 |
| `/record/remove` | POST | 删除文件记录 |

### OssSpEnum → Bean 路由

```java
ALI_OSS("AliOssService")       → AliOssServiceImpl   // com.aliyun.oss.OSS
AWS_S3("S3Service")            → S3ServiceImpl        // S3Client + S3Presigner
S3_COMPATIBLE("S3Service")     → S3ServiceImpl        // 复用 S3 协议
```

Spring 通过 `@Autowired Map<String, FileosService> fileServiceMap` 自动收集所有实现，key 为首字母小写的 Bean 名。`AbstractFileosApi.getApi(ossSp)` 通过 `OssSpEnum.getServiceName()` 查找对应实现。

### BucketCache（Redis Pub/Sub）

- 监听频道 `shrimp:micro:fileos:bucket:cache:refresh`
- 两级缓存：`CACHE_BUCKET_DEFAULT` + `CACHE_BUCKET`(Map<bucketName, Bucket>)
- `clearCache()` → PUBLISH → 所有实例 `onMessage()` → `loadCache()`
- `synchronized` + 3 秒防抖

### 安全特性

| 机制 | 实现 |
|------|------|
| Magic Bytes 校验 | JPG:`FFD8FF` / PNG:`89504E470D0A1A0A` / GIF:`47494638` / WebP:`5249464657454250` / PDF:`255044462D` / ZIP:`504B0304` / MP4:`66747970` |
| 路径穿越防护 | `OssUtil.sanitizeFileName()` 过滤 `/` `\` `..` `(` `)` `+` `;` `&` |
| 大小限制 | 图片≤10MB / 视频≤500MB / 其他≤50MB（可配置） |
| 扩展名白名单 | 图片和视频分别维护 |
| 租户隔离 | Bucket 和文件记录含 `tenant_code`，操作校验归属 |
| Hash 去重 | SHA-256 计算文件摘要，相同内容复用存储 |

---

## 🛠️ 开发指南

### 使用 API — 上传

```java
@Autowired FileosUploadApi uploadApi;

// 使用默认 Bucket
uploadApi.upload(file);

// 指定业务分类
uploadApi.upload(file, "avatar");

// 指定 Bucket + 业务分类
uploadApi.upload(file, "avatar", "my-bucket");

// 指定 Bucket + 业务分类 + 是否公开
uploadApi.upload(file, "avatar", "my-bucket", true);

// 使用 FileosUploadRequest（支持 imageProcess）
FileosUploadRequest request = new FileosUploadRequest();
request.setCategory("avatar");
request.setBucketName("my-bucket");
request.setIsPublic(true);
request.setImageProcess("{\"resize\":{\"width\":200,\"height\":200}}");
uploadApi.upload(file, request);

// 分片上传
MultipartUploadInitRequest initReq = new MultipartUploadInitRequest();
initReq.setFileName("big-file.zip");
initReq.setFileSize(1024L * 1024 * 500);
initReq.setPartCount(10);
MultipartUploadInitResponse initResp = uploadApi.initMultipartUpload(initReq);

MultipartCompleteRequest completeReq = new MultipartCompleteRequest();
completeReq.setUploadId(initResp.getUploadId());
completeReq.setFileId(initResp.getFileId());
completeReq.setParts(completedParts);
uploadApi.completeMultipartUpload(completeReq);

// 中止分片上传
uploadApi.abortMultipartUpload(uploadId, fileId, bucketName, ossSp);
```

### 使用 API — 签名

```java
@Autowired FileosSignApi signApi;

// 单个文件，默认 10 分钟过期
signApi.sign(fileId);

// 自定义过期时间
signApi.sign(fileId, 30, TimeUnit.MINUTES);

// 批量签名
signApi.sign(fileIds);

// 泛型签名：对实体中 URL 字段签名
signApi.sign(user, User::getAvatarUrl, User::setAvatarUrl);

// 泛型签名：批量实体
signApi.sign(users, User::getAvatarUrl, User::setAvatarUrl);

// 富文本内容签名（自动提取 img src 并替换）
signApi.signContent(htmlContent);
```

### 使用 API — 下载

```java
@Autowired FileosDownloadApi downloadApi;

// 全量下载
InputStream is = downloadApi.download(fileId);

// 范围下载（支持断点续传）
InputStream is = downloadApi.download(fileId, offset, length);
```

### 使用 API — 删除

```java
@Autowired FileosDeleteApi deleteApi;

// 单个删除（同时删 OSS 文件 + DB 记录 + 更新目录统计）
deleteApi.delete(fileId);

// 批量删除
deleteApi.delete(fileIds);
```

### 使用 API — 预签名上传（前端直传）

```java
@Autowired FileosPresignUploadApi presignApi;

// 简单预签名上传
PresignUploadRequest req = new PresignUploadRequest();
req.setFileName("photo.jpg");
req.setFileSize(1024L * 100);
req.setCategory("avatar");
PresignUploadResponse resp = presignApi.presignUpload(req);
// 前端使用 resp.getPresignUrl() 直传到 OSS

// 前端上传完成后，后端确认
PresignCompleteRequest completeReq = new PresignCompleteRequest();
completeReq.setFileId(resp.getFileId());
completeReq.setFileName("photo.jpg");
presignApi.presignComplete(completeReq);

// 批量预签名
presignApi.presignUploadBatch(requests);

// 批量完成确认
presignApi.presignCompleteBatch(completeRequests);

// 预签名分片上传
MultipartUploadInitResponse initResp = presignApi.initMultipartUpload(multipartInitReq);
presignApi.completeMultipartUpload(multipartCompleteReq);
presignApi.abortMultipartUpload(uploadId, fileId, bucketName, ossSp);
```

### 添加新 OSS 提供商

1. 创建 `@Service("newOssService") implements FileosService`
2. `OssSpEnum` 添加 `NEW_OSS("NewOssService", "...")`
3. `mdm_fileos_bucket` 表插入 `oss_sp = "NEW_OSS"` 记录

---

## 📊 数据库表

### mdm_fileos_bucket

| 字段 | Java 字段 | 类型 | 说明 |
|------|----------|------|------|
| `id` | `id` | bigint | 主键 |
| `tenant_code` | `tenantCode` | varchar | 租户编码 |
| `bucket_name` | `bucketName` | varchar | Bucket名称 |
| `oss_sp` | `ossSp` | varchar | OSS服务商(ALI_OSS\|AWS_S3\|S3_COMPATIBLE) |
| `endpoint_inner` | `endpointInner` | varchar | 内网Endpoint |
| `endpoint_outer` | `endpointOuter` | varchar | 外网Endpoint |
| `region` | `region` | varchar | 区域 |
| `access_key` | `accessKey` | varchar | Access Key |
| `secret_key` | `secretKey` | varchar | Secret Key |
| `default_flag` | `defaultFlag` | int | 默认标识(1=默认) |
| `system` | `system` | varchar | 系统标识 |

### mdm_fileos_record

| 字段 | Java 字段 | 类型 | 说明 |
|------|----------|------|------|
| `id` | `id` | bigint | 主键 |
| `tenant_code` | `tenantCode` | varchar | 租户编码 |
| `file_id` | `fileId` | varchar | 文件存储路径 |
| `file_name` | `fileName` | varchar | 原始文件名 |
| `file_type` | `fileType` | varchar | 文件扩展名 |
| `file_size` | `fileSize` | bigint | 文件大小 |
| `file_hash` | `fileHash` | varchar | 文件Hash(SHA-256) |
| `content_type` | `contentType` | varchar | MIME类型 |
| `category` | `category` | varchar | 业务分类 |
| `dir_path` | `dirPath` | varchar | 所属目录路径 |
| `is_public` | `isPublic` | int | 是否公共读(0/1) |
| `oss_sp` | `ossSp` | varchar | OSS服务商 |
| `bucket_name` | `bucketName` | varchar | 所属Bucket |
| `upload_type` | `uploadType` | varchar | 上传方式(SIMPLE\|MULTIPART\|PRESIGN) |
| `upload_id` | `uploadId` | varchar | 分片上传ID |
| `upload_status` | `uploadStatus` | varchar | 上传状态(UPLOADING\|COMPLETED\|ABORTED) |
| `image_process` | `imageProcess` | varchar | 图片处理参数(JSON) |

### mdm_fileos_multipart

| 字段 | Java 字段 | 类型 | 说明 |
|------|----------|------|------|
| `id` | `id` | bigint | 主键 |
| `tenant_code` | `tenantCode` | varchar | 租户编码 |
| `upload_id` | `uploadId` | varchar | 分片上传ID |
| `file_id` | `fileId` | varchar | 文件存储路径 |
| `file_name` | `fileName` | varchar | 原始文件名 |
| `file_size` | `fileSize` | bigint | 文件大小 |
| `content_type` | `contentType` | varchar | MIME类型 |
| `category` | `category` | varchar | 业务分类 |
| `is_public` | `isPublic` | int | 是否公共读 |
| `oss_sp` | `ossSp` | varchar | OSS服务商 |
| `bucket_name` | `bucketName` | varchar | 所属Bucket |
| `part_count` | `partCount` | int | 分片总数 |
| `completed_parts` | `completedParts` | text | 已完成分片信息 |
| `status` | `status` | varchar | 状态(UPLOADING\|COMPLETED\|ABORTED) |
| `expire_time` | `expireTime` | datetime | 过期时间 |

### mdm_fileos_directory

| 字段 | Java 字段 | 类型 | 说明 |
|------|----------|------|------|
| `id` | `id` | bigint | 主键 |
| `tenant_code` | `tenantCode` | varchar | 租户编码 |
| `bucket_name` | `bucketName` | varchar | 所属Bucket |
| `dir_path` | `dirPath` | varchar | 目录完整路径 |
| `dir_name` | `dirName` | varchar | 目录名 |
| `parent_path` | `parentPath` | varchar | 父目录路径 |
| `dir_level` | `dirLevel` | int | 目录层级 |
| `file_count` | `fileCount` | bigint | 文件数量 |
| `total_size` | `totalSize` | bigint | 文件总大小 |

---

## 🛤️ 路径规划规则

文件存储路径由 `PathHelper.getFullName()` 生成，格式为：

```
{system}/{env}/{category}/{day}/[public/]{timestamp}_{seq}_{safeFilename}
```

| 段 | 来源 | 示例 |
|----|------|------|
| `system` | `bucket.system`，为空时默认 `default-app` | `my-app` |
| `env` | `Sys.getCurrentEnv()` | `dev` / `test` / `prod` |
| `category` | 请求参数，为空时默认 `common` | `avatar` |
| `day` | 当前日期 `yyyyMMdd` | `20260522` |
| `public/` | `isPublic=true` 时添加 | `public/` |
| `timestamp` | 当前时间 `yyyyMMddHHmmssSSS` | `20260522143025123` |
| `seq` | 毫秒内自增序列 | `1` |
| `safeFilename` | 原始文件名经 `OssUtil.sanitizeFileName()` 处理 | `photo.jpg` |

完整示例：`my-app/prod/avatar/20260522/public/20260522143025123_1_photo.jpg`

---

## 🔧 配置项

```yaml
sh:
  fileos:
    max-size-mb: 50                              # 通用文件（默认 50MB）
    image:
      max-size-mb: 10                            # 图片（默认 10MB）
      extension-names: jpg,jpeg,png,gif,webp,svg,bmp
    video:
      max-size-mb: 500                           # 视频（默认 500MB）
      extension-names: mp4,mpeg,avi,mov,wmv,rm,rmvb,mkv,flv
    presign:
      expire-minutes: 30                         # 预签名过期时间（默认 30 分钟）
      multipart:
        expire-minutes: 60                       # 预签名分片过期时间（默认 60 分钟）
        default-part-size-mb: 5                  # 默认分片大小（默认 5MB）
    multipart:
      max-age-hours: 24                          # 分片上传记录最大保留时间（默认 24 小时）
    hash:
      enabled: true                              # 是否启用 Hash 去重（默认 true）
      algorithm: SHA-256                         # Hash 算法（默认 SHA-256）
```

---

## ⚠️ 注意事项

1. `BucketCache` 依赖 `RedisMessageListenerContainer` Bean，确保全局已配置
2. 统一使用 `ValidationException.of("消息")` 抛出业务异常
3. 不指定 bucket 参数时使用 `default_flag=1` 的默认 Bucket
4. Hash 去重默认开启，相同内容的文件会复用存储路径，但会创建新的 `mdm_fileos_record` 记录
5. 分片上传需配合 `MultipartCleanupJob`（XXL-Job Handler: `fileosMultipartCleanup`）定时清理过期记录
6. 图片处理参数（`imageProcess`）仅阿里云 OSS 生效，存储为 JSON 格式，签名时自动附加 `x-oss-process` 参数
7. 目录统计（`fileCount` / `totalSize`）由 `DirectoryHelper` 异步维护，上传/删除时自动更新
8. 预签名上传流程：后端生成预签名 URL → 前端直传 OSS → 前端通知后端完成确认（`presignComplete`）
9. `OssSpEnum` 中 `AWS_S3` 和 `S3_COMPATIBLE` 共用 `S3ServiceImpl`，通过 Bucket 的 `endpointInner`/`endpointOuter` 区分

---

## 📚 依赖

- `sh-core`: BaseEntity、ValidationException、R 返回对象、UserContext
- `sh-mybatis`: BaseService、BaseMapper、PageQuery
- `sh-redis`: StringRedisTemplate、RedisMessageListenerContainer
- `sh-spring`: Sys（环境信息）、SnowflakeHelper
- `sh-web`: ErrorHandler
- `sh-xxljob`: @XxlJob（分片上传清理任务）
- `aliyun-sdk-oss` / `software.amazon.awssdk:s3`

---

## 🆘 常见问题

| 问题 | 原因/解决 |
|------|----------|
| 上传失败 "bucket 未配置" | 检查 `mdm_fileos_bucket` 表，或设置 `default_flag=1` |
| 签名链接无法访问 | 确认 Bucket 为私有读写，检查 OSS/S3 权限 |
| 多实例缓存不一致 | 确认 Redis 频道 `shrimp:micro:fileos:bucket:cache:refresh` 正常通信 |
| 文件类型校验失败 | Magic Bytes 验证实际内容，确认文件未损坏或扩展名匹配 |
| Hash 去重导致文件 ID 相同 | 这是预期行为，相同内容共享存储路径，但各自有独立的 `mdm_fileos_record` 记录 |
| 分片上传记录堆积 | 检查 XXL-Job 中 `fileosMultipartCleanup` 任务是否正常运行 |
| 图片处理参数不生效 | 确认 OSS 服务商为 `ALI_OSS`，其他服务商暂不支持图片处理 |
| 预签名上传后文件状态为 UPLOADING | 前端上传完成后需调用 `presignComplete` 接口确认 |

---

**最后更新时间**: 2026-05-22
