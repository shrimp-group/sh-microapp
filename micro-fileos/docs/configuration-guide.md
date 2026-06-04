# micro-fileos 配置文档

## 1. 完整配置项列表

所有配置项统一在 `sh.fileos` 命名空间下，在 `application.yml` 中配置。

### 1.1 全局配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `sh.fileos.max-size-mb` | Integer | `50` | 全局最大文件大小（MB），超过此大小的文件将被拒绝上传 |

### 1.2 图片配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `sh.fileos.image.max-size-mb` | Integer | `10` | 图片文件最大大小（MB） |
| `sh.fileos.image.extension-names` | String | `jpg,jpeg,png,gif,webp,svg,bmp` | 允许的图片扩展名，逗号分隔 |

### 1.3 视频配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `sh.fileos.video.max-size-mb` | Integer | `500` | 视频文件最大大小（MB） |
| `sh.fileos.video.extension-names` | String | `mp4,mpeg,avi,mov,wmv,rm,rmvb,mkv,flv` | 允许的视频扩展名，逗号分隔 |

### 1.4 预签名配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `sh.fileos.presign.expire-minutes` | Integer | `30` | 简单上传预签名 URL 过期时间（分钟） |
| `sh.fileos.presign.multipart.expire-minutes` | Integer | `60` | 分片上传预签名 URL 过期时间（分钟），因大文件上传耗时较长，默认值大于简单上传 |
| `sh.fileos.presign.multipart.default-part-size-mb` | Integer | `5` | 默认分片大小（MB），当请求未指定分片大小时使用此值 |

### 1.5 分片上传配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `sh.fileos.multipart.max-age-hours` | Integer | `24` | 分片上传记录过期时间（小时），超时未完成的分片上传将被定时任务清理 |

### 1.6 Hash 去重配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `sh.fileos.hash.enabled` | Boolean | `true` | 是否启用文件 Hash 去重，启用后相同内容的文件不会重复存储 |
| `sh.fileos.hash.algorithm` | String | `SHA-256` | Hash 算法，支持 `MD5`、`SHA-1`、`SHA-256`、`SHA-512` |

### 1.7 完整配置示例

```yaml
sh:
  fileos:
    max-size-mb: 50
    image:
      max-size-mb: 10
      extension-names: jpg,jpeg,png,gif,webp,svg,bmp
    video:
      max-size-mb: 500
      extension-names: mp4,mpeg,avi,mov,wmv,rm,rmvb,mkv,flv
    presign:
      expire-minutes: 30
      multipart:
        expire-minutes: 60
        default-part-size-mb: 5
    multipart:
      max-age-hours: 24
    hash:
      enabled: true
      algorithm: SHA-256
```

---

## 2. Bucket 配置说明

Bucket 配置存储在 `mdm_fileos_bucket` 数据库表中，通过 REST API 管理，支持多 Bucket、多 OSS 服务商。

### 2.1 表字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `bucket_name` | varchar(127) | 是 | Bucket 名称，需与 OSS 服务商侧的 Bucket 名称一致 |
| `oss_sp` | varchar(31) | 是 | OSS 服务商枚举值：`ALI_OSS`（阿里云）、`AWS_S3`（AWS）、`S3_COMPATIBLE`（S3 兼容存储） |
| `endpoint_inner` | varchar(255) | 否 | 内网 Endpoint，服务端访问 OSS 时优先使用，减少公网流量 |
| `endpoint_outer` | varchar(255) | 否 | 外网 Endpoint，生成预签名 URL 时使用，客户端直传场景必须配置 |
| `region` | varchar(63) | 否 | 区域标识，如 `cn-hangzhou`、`us-east-1` |
| `access_key` | varchar(255) | 是 | OSS 访问 Access Key |
| `secret_key` | varchar(255) | 是 | OSS 访问 Secret Key |
| `default_flag` | int | 否 | 默认标识，`1` 表示默认 Bucket，上传时未指定 Bucket 将使用默认 Bucket |
| `system` | varchar(63) | 否 | 系统标识，用于区分不同业务系统 |

### 2.2 OSS 服务商枚举

| 枚举值 | 底层实现 | 说明 |
|--------|----------|------|
| `ALI_OSS` | `AliOssServiceImpl` | 阿里云 OSS，使用 `aliyun-sdk-oss` SDK |
| `AWS_S3` | `S3ServiceImpl` | AWS S3，使用 AWS SDK `software.amazon.awssdk:s3` |
| `S3_COMPATIBLE` | `S3ServiceImpl` | S3 兼容存储（如 MinIO），同样使用 AWS S3 SDK |

---

## 3. 阿里云 OSS 配置示例

### 3.1 通过 API 创建

```bash
curl -X POST http://localhost:8080/micro-fileos/bucket/create \
  -H 'Content-Type: application/json' \
  -d '{
    "bucketName": "my-ali-bucket",
    "ossSp": "ALI_OSS",
    "endpointInner": "https://oss-cn-hangzhou-internal.aliyuncs.com",
    "endpointOuter": "https://oss-cn-hangzhou.aliyuncs.com",
    "region": "cn-hangzhou",
    "accessKey": "LTAI5tXXXXXXXXXXXXXX",
    "secretKey": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
    "defaultFlag": 1,
    "system": "main"
  }'
```

### 3.2 各区域 Endpoint 参考

| 区域 | 内网 Endpoint | 外网 Endpoint |
|------|--------------|--------------|
| 华东1（杭州） | `https://oss-cn-hangzhou-internal.aliyuncs.com` | `https://oss-cn-hangzhou.aliyuncs.com` |
| 华东2（上海） | `https://oss-cn-shanghai-internal.aliyuncs.com` | `https://oss-cn-shanghai.aliyuncs.com` |
| 华北1（青岛） | `https://oss-cn-qingdao-internal.aliyuncs.com` | `https://oss-cn-qingdao.aliyuncs.com` |
| 华北2（北京） | `https://oss-cn-beijing-internal.aliyuncs.com` | `https://oss-cn-beijing.aliyuncs.com` |
| 华南1（深圳） | `https://oss-cn-shenzhen-internal.aliyuncs.com` | `https://oss-cn-shenzhen.aliyuncs.com` |

### 3.3 注意事项

- 阿里云 OSS 使用 V4 签名，需确保 SDK 版本兼容
- 内网 Endpoint 仅在阿里云 ECS 同区域时可用，否则请使用外网 Endpoint
- 预签名上传场景必须配置 `endpointOuter`，否则客户端无法访问

---

## 4. AWS S3 配置示例

### 4.1 通过 API 创建

```bash
curl -X POST http://localhost:8080/micro-fileos/bucket/create \
  -H 'Content-Type: application/json' \
  -d '{
    "bucketName": "my-aws-bucket",
    "ossSp": "AWS_S3",
    "endpointInner": null,
    "endpointOuter": "https://s3.us-east-1.amazonaws.com",
    "region": "us-east-1",
    "accessKey": "AKIAIOSFODNN7EXAMPLE",
    "secretKey": "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
    "defaultFlag": 1,
    "system": "main"
  }'
```

### 4.2 各区域 Endpoint 参考

| 区域 | Endpoint |
|------|----------|
| 美国东部（弗吉尼亚北部） | `https://s3.us-east-1.amazonaws.com` |
| 美国西部（俄勒冈） | `https://s3.us-west-2.amazonaws.com` |
| 欧洲（爱尔兰） | `https://s3.eu-west-1.amazonaws.com` |
| 亚太（东京） | `https://s3.ap-northeast-1.amazonaws.com` |
| 亚太（新加坡） | `https://s3.ap-southeast-1.amazonaws.com` |

### 4.3 注意事项

- AWS S3 无内网/外网区分，`endpointInner` 可不配置
- `region` 必须与 Bucket 所在区域一致
- IAM 策略需授予 `s3:PutObject`、`s3:GetObject`、`s3:DeleteObject` 等必要权限

---

## 5. S3 兼容存储（MinIO）配置示例

### 5.1 通过 API 创建

```bash
curl -X POST http://localhost:8080/micro-fileos/bucket/create \
  -H 'Content-Type: application/json' \
  -d '{
    "bucketName": "my-minio-bucket",
    "ossSp": "S3_COMPATIBLE",
    "endpointInner": "http://minio:9000",
    "endpointOuter": "http://192.168.1.100:9000",
    "region": "us-east-1",
    "accessKey": "minioadmin",
    "secretKey": "minioadmin",
    "defaultFlag": 1,
    "system": "dev"
  }'
```

### 5.2 Docker 部署 MinIO 参考

```yaml
# docker-compose.yml
version: '3.8'
services:
  minio:
    image: minio/minio:latest
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    command: server /data --console-address ":9001"
    volumes:
      - minio-data:/data

volumes:
  minio-data:
```

### 5.3 注意事项

- `ossSp` 必须设为 `S3_COMPATIBLE`，底层使用 AWS S3 SDK 的自定义 Endpoint 模式
- MinIO 默认 `region` 为 `us-east-1`，除非自定义配置
- 开发环境可使用 HTTP，生产环境建议配置 HTTPS
- `endpointInner` 适用于 Docker 内网或 K8s 集群内部访问

---

## 6. 多 Bucket 配置示例

系统支持配置多个 Bucket，分别对接不同的 OSS 服务商或用途。

### 6.1 场景：按业务分类使用不同 Bucket

```bash
# 默认 Bucket — 阿里云 OSS（通用文件）
curl -X POST http://localhost:8080/micro-fileos/bucket/create \
  -H 'Content-Type: application/json' \
  -d '{
    "bucketName": "app-general",
    "ossSp": "ALI_OSS",
    "endpointInner": "https://oss-cn-hangzhou-internal.aliyuncs.com",
    "endpointOuter": "https://oss-cn-hangzhou.aliyuncs.com",
    "region": "cn-hangzhou",
    "accessKey": "LTAI5tXXXXXXXXXXXXXX",
    "secretKey": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
    "defaultFlag": 1,
    "system": "main"
  }'

# 视频 Bucket — AWS S3（大文件存储）
curl -X POST http://localhost:8080/micro-fileos/bucket/create \
  -H 'Content-Type: application/json' \
  -d '{
    "bucketName": "app-video",
    "ossSp": "AWS_S3",
    "endpointOuter": "https://s3.us-east-1.amazonaws.com",
    "region": "us-east-1",
    "accessKey": "AKIAIOSFODNN7EXAMPLE",
    "secretKey": "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
    "defaultFlag": 0,
    "system": "main"
  }'

# 开发 Bucket — MinIO（本地开发测试）
curl -X POST http://localhost:8080/micro-fileos/bucket/create \
  -H 'Content-Type: application/json' \
  -d '{
    "bucketName": "app-dev",
    "ossSp": "S3_COMPATIBLE",
    "endpointInner": "http://minio:9000",
    "endpointOuter": "http://localhost:9000",
    "region": "us-east-1",
    "accessKey": "minioadmin",
    "secretKey": "minioadmin",
    "defaultFlag": 0,
    "system": "dev"
  }'
```

### 6.2 上传时指定 Bucket

```java
// 使用默认 Bucket（defaultFlag=1）
fileosUploadApi.upload(file, "avatar");

// 指定 Bucket
fileosUploadApi.upload(file, "avatar", "app-video");
```

---

## 7. 图片处理参数配置说明

图片处理参数通过 `imageProcess` 字段传递，值为 JSON 字符串，对应 `ImageProcessParam` 结构。

### 7.1 缩放参数（resize）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `width` | Integer | 否 | 目标宽度（像素） |
| `height` | Integer | 否 | 目标高度（像素） |
| `mode` | String | 否 | 缩放模式 |

**缩放模式说明：**

| 模式 | 值 | 说明 |
|------|----|------|
| 等比缩放 | `lfit` | 按长边缩放，保证图片完整显示，可能留白 |
| 裁剪缩放 | `mfit` | 按短边缩放后居中裁剪，保证图片填满目标尺寸 |
| 填充缩放 | `fill` | 按指定尺寸缩放，不足部分填充背景色 |
| 按长边缩放 | `pad` | 按长边等比缩放，短边不足部分填充 |

### 7.2 裁剪参数（crop）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `x` | Integer | 是 | 裁剪起始 X 坐标（像素） |
| `y` | Integer | 是 | 裁剪起始 Y 坐标（像素） |
| `width` | Integer | 是 | 裁剪宽度（像素） |
| `height` | Integer | 是 | 裁剪高度（像素） |

### 7.3 水印参数（watermark）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `text` | String | 是 | 水印文字内容 |
| `position` | String | 否 | 水印位置，默认 `br`（右下角） |
| `opacity` | Integer | 否 | 透明度（0-100），默认 `50` |
| `fontSize` | Integer | 否 | 字体大小（像素），默认 `20` |

**水印位置说明：**

| 位置 | 值 | 说明 |
|------|----|------|
| 左上 | `tl` | Top-Left |
| 右上 | `tr` | Top-Right |
| 左下 | `bl` | Bottom-Left |
| 右下 | `br` | Bottom-Right |
| 居中 | `center` | Center |

### 7.4 图片处理参数使用示例

#### 后端上传时指定

```java
FileosUploadRequest request = new FileosUploadRequest();
request.setCategory("avatar");
request.setBucketName("my-bucket");
request.setImageProcess("{\"resize\":{\"width\":200,\"height\":200,\"mode\":\"mfit\"}}");
fileosUploadApi.upload(file, request);
```

#### 预签名上传时指定

```java
PresignUploadRequest request = new PresignUploadRequest();
request.setFileName("photo.jpg");
request.setFileSize(file.length());
request.setContentType("image/jpeg");
request.setImageProcess("{\"resize\":{\"width\":800,\"mode\":\"lfit\"},\"watermark\":{\"text\":\"©MyApp\",\"position\":\"br\",\"opacity\":30}}");
fileosPresignUploadApi.presignUpload(request);
```

#### 组合使用

```json
{
  "resize": { "width": 1024, "height": 768, "mode": "lfit" },
  "crop": { "x": 100, "y": 50, "width": 800, "height": 600 },
  "watermark": { "text": "Confidential", "position": "center", "opacity": 20, "fontSize": 36 }
}
```

> **注意**：图片处理参数的实际效果取决于 OSS 服务商的支持情况。阿里云 OSS 原生支持图片处理，S3 兼容存储可能需要应用层自行处理。

---

## 8. Hash 去重配置说明

### 8.1 工作原理

当 `sh.fileos.hash.enabled=true` 时，上传文件前会计算文件内容的 Hash 值，并在 `mdm_fileos_record` 表中查找是否已存在相同 Hash 的记录：

- **存在相同 Hash**：直接复用已有文件记录，不再重复上传到 OSS，节省存储空间
- **不存在相同 Hash**：正常上传并记录 Hash 值

### 8.2 配置项

```yaml
sh:
  fileos:
    hash:
      enabled: true           # 是否启用去重
      algorithm: SHA-256      # Hash 算法
```

### 8.3 支持的 Hash 算法

| 算法 | 值 | 速度 | 碰撞概率 | 推荐场景 |
|------|----|------|----------|----------|
| MD5 | `MD5` | 快 | 较高 | 对去重精度要求不高的场景 |
| SHA-1 | `SHA-1` | 较快 | 较低 | 一般场景 |
| SHA-256 | `SHA-256` | 中等 | 极低 | **推荐**，默认值 |
| SHA-512 | `SHA-512` | 较慢 | 极低 | 对安全性要求极高的场景 |

### 8.4 注意事项

- Hash 计算在大文件场景下会消耗 CPU 和内存，超大文件建议关闭去重
- 删除文件记录时不会删除 OSS 上的实际文件（因为可能被其他记录引用）
- Hash 去重仅在服务端上传（`FileosUploadApi`）时生效，预签名上传由客户端直传，无法在服务端计算 Hash

---

## 9. 分片上传配置说明

### 9.1 分片上传流程

```
初始化 → 逐片上传 → 完成
              ↑
         可中断/恢复
```

### 9.2 配置项

```yaml
sh:
  fileos:
    presign:
      expire-minutes: 30                          # 简单预签名过期时间
      multipart:
        expire-minutes: 60                        # 分片预签名过期时间
        default-part-size-mb: 5                   # 默认分片大小
    multipart:
      max-age-hours: 24                           # 过期清理时间
```

### 9.3 分片大小选择建议

| 文件大小 | 推荐分片大小 | 分片数量 | 说明 |
|----------|-------------|----------|------|
| 50MB - 200MB | 5MB | 10 - 40 | 默认配置即可 |
| 200MB - 1GB | 10MB | 20 - 100 | 适当增大分片 |
| 1GB - 5GB | 20MB | 50 - 250 | 增大分片，减少请求次数 |
| > 5GB | 50MB | 100+ | 大分片，注意预签名 URL 过期时间 |

### 9.4 分片上传记录

分片上传的中间状态记录在 `mdm_fileos_multipart` 表中：

| 状态 | 说明 |
|------|------|
| `UPLOADING` | 上传中 |
| `COMPLETED` | 已完成 |
| `ABORTED` | 已中止 |

### 9.5 注意事项

- 分片上传的预签名 URL 过期时间应大于预估上传时间，建议设为 `60` 分钟以上
- 每个分片上传完成后需记录 OSS 返回的 `ETag`，完成上传时需提交所有分片的 `ETag`
- 分片序号（partNumber）从 `1` 开始，连续递增
- 中止分片上传会清理 OSS 上的已上传分片，释放存储空间

---

## 10. 定时任务配置说明

### 10.1 分片上传清理任务

模块内置 `MultipartCleanupJob`，用于清理过期未完成的分片上传记录。

**任务 Handler**：`fileosMultipartCleanup`

**工作流程**：

1. 查询 `mdm_fileos_multipart` 表中状态为 `UPLOADING` 且创建时间超过 `sh.fileos.multipart.max-age-hours` 的记录
2. 按 Bucket 分组，调用 OSS API 中止分片上传（清理 OSS 上的临时分片）
3. 更新分片记录状态为 `ABORTED`
4. 同步更新关联的 `mdm_fileos_record` 记录状态为 `ABORTED`

### 10.2 XXL-Job 配置

在 XXL-Job 调度中心配置定时任务：

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| 执行器 | `spring.application.name` | 自动注册 |
| JobHandler | `fileosMultipartCleanup` | 固定值 |
| 调度类型 | CRON | — |
| CRON 表达式 | `0 0 */6 * * ?` | 每 6 小时执行一次 |
| 阻塞处理策略 | 丢弃后续调度 | 避免重复执行 |
| 路由策略 | 第一个 | — |

### 10.3 过期时间配置

```yaml
sh:
  fileos:
    multipart:
      max-age-hours: 24    # 超过 24 小时未完成的分片上传将被清理
```

- 默认值 `24` 小时，适用于大多数场景
- 网络环境较差时可适当增大，如 `48` 或 `72`
- 不建议设得过小，可能导致正在上传的大文件被误清理

### 10.4 日志输出示例

```
2026-05-22 06:00:00 INFO  - 无过期分片上传记录需要清理
2026-05-22 12:00:00 INFO  - 开始清理过期分片上传记录, 共 3 条, 过期时间阈值=...
2026-05-22 12:00:01 INFO  - 分片上传清理完成: 成功 3 条, 失败 0 条
2026-05-22 12:00:01 ERROR - 清理分片上传记录失败: uploadId=xxx, fileId=xxx, bucketName=xxx
```

---

## 11. 环境差异化配置

### 11.1 开发环境（MinIO）

```yaml
# application-dev.yml
sh:
  fileos:
    max-size-mb: 100
    presign:
      expire-minutes: 60
    multipart:
      max-age-hours: 48
    hash:
      enabled: false    # 开发环境关闭去重，方便调试
```

Bucket 配置指向本地 MinIO：

```json
{
  "bucketName": "dev-bucket",
  "ossSp": "S3_COMPATIBLE",
  "endpointInner": "http://minio:9000",
  "endpointOuter": "http://localhost:9000",
  "region": "us-east-1",
  "accessKey": "minioadmin",
  "secretKey": "minioadmin",
  "defaultFlag": 1
}
```

### 11.2 测试环境

```yaml
# application-test.yml
sh:
  fileos:
    max-size-mb: 50
    presign:
      expire-minutes: 30
    multipart:
      max-age-hours: 24
    hash:
      enabled: true
```

### 11.3 生产环境（阿里云 OSS）

```yaml
# application-prod.yml
sh:
  fileos:
    max-size-mb: 50
    image:
      max-size-mb: 10
    video:
      max-size-mb: 500
    presign:
      expire-minutes: 15        # 生产环境缩短过期时间，提高安全性
      multipart:
        expire-minutes: 60
        default-part-size-mb: 5
    multipart:
      max-age-hours: 24
    hash:
      enabled: true
      algorithm: SHA-256
```

Bucket 配置使用阿里云内网 Endpoint：

```json
{
  "bucketName": "prod-bucket",
  "ossSp": "ALI_OSS",
  "endpointInner": "https://oss-cn-hangzhou-internal.aliyuncs.com",
  "endpointOuter": "https://oss-cn-hangzhou.aliyuncs.com",
  "region": "cn-hangzhou",
  "accessKey": "${ALI_OSS_AK}",
  "secretKey": "${ALI_OSS_SK}",
  "defaultFlag": 1
}
```

> **安全提示**：生产环境的 `accessKey` 和 `secretKey` 不应明文写在配置文件中，建议使用环境变量注入或配置中心加密管理。
