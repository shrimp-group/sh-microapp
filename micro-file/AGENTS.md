# micro-file 模块开发指南

帮助开发者快速理解 `micro-file` 模块的架构设计、核心功能和开发规范。

## 📦 模块概述

基于 Spring Boot 的文件管理服务，支持阿里云 OSS、AWS S3、MinIO，通过策略模式实现无缝切换。

**核心特性**: 多存储提供商 · 多租户隔离 · 临时签名链接 · Magic Bytes 校验 · Redis Pub/Sub 缓存

---

## 🏗️ 架构设计

```
Controller      FileCommonRest / FileBucketRest / FileRecordRest
    ↓
API Interface   FileUploadApi / FileSignApi / FileDeleteApi
    ↓
API Impl        *ApiImpl (extends AbstractFileApi — 共享依赖)
    ↓
Service         FileService → AliOssServiceImpl / S3ServiceImpl
                MdmFileBucketService / MdmFileRecordService
    ↓
Mapper          MdmFileBucketMapper / MdmFileRecordMapper
    ↓
Cache           BucketCache (Redis Pub/Sub)
```

| 模式 | 应用 | 说明 |
|------|------|------|
| 策略模式 | `FileService` + 两种实现 | 根据 bucket.oss_sp 动态路由 |
| 模板方法 | `AbstractFileApi` | 封装 Bucket 查找、服务路由、URL 解析 |
| Cache | `BucketCache` | Redis Pub/Sub + 本地 Map，3 秒防抖 |

---

## 📁 目录结构

```
micro-file/src/main/java/com/wkclz/micro/file/
├── FileAutoConfig.java              # @ComponentScan + @MapperScan
├── api/
│   ├── FileApi.java                 # [过时] 兼容接口
│   ├── FileUploadApi.java / FileSignApi.java / FileDeleteApi.java
│   └── impl/
│       ├── AbstractFileApi.java     # 基类：Bucket 查找 / 服务路由 / URL 解析
│       └── *ApiImpl.java            # [FileApiImpl 过时]
├── bean/entity/                     # MdmFileBucket / MdmFileRecord
├── bean/enums/
│   ├── ContentTypeEnum.java         # 300+ 类型映射（HashMap O(1)）
│   └── OssSpEnum.java              # ALI_OSS("AliOssService") / AWS_S3("S3Service") / MINIO("S3Service")
├── config/
│   ├── FileConfig.java              # 文件大小配置
│   └── FsConfig.java               # FileTypeHelper 依赖的扩展名配置
├── helper/
│   ├── BucketCache.java             # Redis Pub/Sub 缓存监听
│   ├── ContentFileHelper.java       # 富文本 URL 提取/替换
│   └── FileTypeHelper.java          # Magic Bytes 文件验证
├── mapper/                          # MdmFileBucketMapper / MdmFileRecordMapper
├── rest/                            # FileBucketRest / FileCommonRest / FileRecordRest
├── service/                         # FileService 接口 + AliOssServiceImpl / S3ServiceImpl
└── utils/OssUtil.java               # 文件名安全处理（路径穿越防护）
```

---

## 🔑 核心组件

### REST API（前缀 `/micro-file`）

| 端点 | 方法 | 说明 |
|------|------|------|
| `/bucket/page` | GET | Bucket 分页 |
| `/bucket/info` | GET | Bucket 详情 |
| `/bucket/create` | POST | 创建 Bucket |
| `/bucket/update` | POST | 更新 Bucket |
| `/bucket/remove` | POST | 删除 Bucket |
| `/bucket/options` | GET | Bucket 选项列表 |
| `/record/page` | GET | 文件记录分页 |
| `/record/info` | GET | 文件记录详情 |
| `/record/remove` | POST | 删除文件记录 |
| `/common/upload` | POST | 文件上传 |
| `/common/upload/public` | POST | 公开上传（无需权限） |

### OssSpEnum → Bean 路由

```java
ALI_OSS("AliOssService") → AliOssServiceImpl   // com.aliyun.oss.OSS
AWS_S3("S3Service")      → S3ServiceImpl        // S3Client + S3Presigner
MINIO("S3Service")       → S3ServiceImpl        // 复用 S3 协议
```

Spring 通过 `@Autowired Map<String, FileService> fileServiceMap` 自动收集所有实现，key 为首字母小写的 Bean 名。`AbstractFileApi.getApi(ossSp)` 通过 `OssSpEnum.getServiceName()` 查找对应实现。

### BucketCache（Redis Pub/Sub）

- 监听频道 `shrimp:micro:bucket:cache:refresh`
- 两级缓存：`CACHE_BUCKET_DEFAULT` + `CACHE_BUCKET`(Map<bucketName, Bucket>)
- `clearCache()` → PUBLISH → 所有实例 `onMessage()` → `loadCache()`
- `synchronized` + 3 秒防抖

### 安全特性

| 机制 | 实现 |
|------|------|
| Magic Bytes 校验 | JPG:`FFD8FF` / PNG:`89504E47` / GIF:`47494638` / PDF:`25504446` / ZIP:`504B0304` |
| 路径穿越防护 | 过滤 `/` `\` `..` |
| 大小限制 | 图片≤2MB / 视频≤100MB / 其他≤50MB（可配置） |
| 扩展名白名单 | 图片/视频分别维护 |
| 租户隔离 | Bucket 和文件记录含 `tenant_code`，操作校验归属 |

---

## 🛠️ 开发指南

### 使用 API

```java
// 上传
@Autowired FileUploadApi uploadApi;
uploadApi.upload(file);                                    // 默认 bucket
uploadApi.upload(file, "avatar");                          // 指定业务类型
uploadApi.upload(file, "avatar", "my-bucket");             // 指定 bucket

// 签名（默认 10 分钟过期）
@Autowired FileSignApi signApi;
signApi.sign(fileId);                                      // 单个签名
signApi.sign(fileId, 30, TimeUnit.MINUTES);                // 自定义过期
signApi.sign(fileIds);                                     // 批量签名
signApi.sign(user, User::getAvatarUrl, User::setAvatarUrl);// 泛型签名

// 删除（同时删 OSS 文件 + DB 记录）
@Autowired FileDeleteApi deleteApi;
deleteApi.delete(fileId);                                  // 单个删除
deleteApi.delete(fileIds);                                 // 批量删除
```

### 添加新 OSS 提供商

1. 创建 `@Service("newOssService") implements FileService`
2. `OssSpEnum` 添加 `NEW_OSS("NewOssService", "...")`
3. `mdm_file_bucket` 表插入 `oss_sp = "NEW_OSS"` 记录

---

## 📊 数据库表

### mdm_file_bucket
id / bucket / oss_sp(ALI_OSS\|AWS_S3\|MINIO) / access_key / secret_key / end_point / default_flag / tenant_code

### mdm_file_record
id / file_id / file_name / file_type / file_size / business_type / bucket / oss_sp / tenant_code

---

## 🚀 性能优化

- **客户端缓存**: `ConcurrentHashMap<String, Client>` 按 Bucket 复用
- **S3Presigner 复用**: 循环外创建，避免重复实例化
- **ContentTypeEnum O(1)**: 静态 HashMap 替代遍历 300+ 枚举值
- **BucketCache 防抖**: 3 秒内不重复加载

---

## 🔧 配置项

```yaml
sh:
  file:
    max-size-mb: 50                           # 通用文件（默认 50MB）
    image:
      max-size-mb: 2                          # 图片（默认 2MB）
      extension-names: jpg,jpeg,png,gif,webp
    video:
      max-size-mb: 100                        # 视频（默认 100MB）
      extension-names: mp4,mpeg,avi,mov,wmv,rm,rmvb
```

---

## ⚠️ 注意事项

1. `FileApi` / `FileApiImpl` 已过时，新代码使用 `FileUploadApi` / `FileSignApi` / `FileDeleteApi`
2. `BucketCache` 依赖 `RedisMessageListenerContainer` Bean，确保全局已配置
3. 统一使用 `ValidationException.of("消息")` 抛出业务异常
4. 不指定 bucket 参数时使用 `default_flag=1` 的默认 Bucket

---

## 📚 依赖

- `sh-core`: BaseEntity、ValidationException、R 返回对象
- `sh-mybatis`: BaseService、BaseMapper、PageQuery
- `sh-redis`: StringRedisTemplate、RedisMessageListenerContainer
- `sh-iam`: SessionHelper（用户/租户信息）
- `aliyun-sdk-oss` / `aws-java-sdk-s3`

---

## 🆘 常见问题

| 问题 | 原因/解决 |
|------|----------|
| 上传失败 "bucket 未配置" | 检查 `mdm_file_bucket` 表，或设置 `default_flag=1` |
| 签名链接无法访问 | 确认 Bucket 为私有读写，检查 OSS/S3 权限 |
| 多实例缓存不一致 | 确认 Redis 频道 `shrimp:micro:bucket:cache:refresh` 正常通信 |
| 文件类型校验失败 | Magic Bytes 验证实际内容，确认文件未损坏或扩展名匹配 |

---

**最后更新时间**: 2026-04-26
