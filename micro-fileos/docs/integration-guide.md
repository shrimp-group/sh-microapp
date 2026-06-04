# micro-fileos 接入文档

## 1. Maven 依赖引入

在主应用的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.wkclz.microapp</groupId>
    <artifactId>micro-fileos</artifactId>
</dependency>
```

模块引入后，`FileosAutoConfig` 会通过 Spring Boot 自动配置机制自动扫描组件和 Mapper，无需手动配置。

---

## 2. 后端 API 使用示例

### 2.1 FileosUploadApi — 文件上传

`FileosUploadApi` 提供服务端直传上传能力，适用于后端接收文件后直接存储到 OSS 的场景。

```java
@Autowired
private FileosUploadApi fileosUploadApi;

// 简单上传 — 仅传文件，使用默认 Bucket
MdmFileosRecordDto record = fileosUploadApi.upload(multipartFile);

// 简单上传 — 指定业务分类
MdmFileosRecordDto record = fileosUploadApi.upload(multipartFile, "avatar");

// 简单上传 — 指定分类和 Bucket
MdmFileosRecordDto record = fileosUploadApi.upload(multipartFile, "avatar", "my-bucket");

// 简单上传 — 指定分类、Bucket、是否公开读
MdmFileosRecordDto record = fileosUploadApi.upload(multipartFile, "avatar", "my-bucket", true);

// 简单上传 — 使用 FileosUploadRequest 精细控制
FileosUploadRequest request = new FileosUploadRequest();
request.setCategory("avatar");
request.setBucketName("my-bucket");
request.setIsPublic(true);
request.setImageProcess("{\"resize\":{\"width\":200,\"height\":200,\"mode\":\"lfit\"}}");
MdmFileosRecordDto record = fileosUploadApi.upload(multipartFile, request);

// 分片上传 — 初始化
MultipartUploadInitRequest initRequest = new MultipartUploadInitRequest();
initRequest.setFileName("large-video.mp4");
initRequest.setFileSize(1024L * 1024 * 500);
initRequest.setContentType("video/mp4");
initRequest.setCategory("video");
initRequest.setBucketName("media-bucket");
initRequest.setPartCount(10);
MultipartUploadInitResponse initResponse = fileosUploadApi.initMultipartUpload(initRequest);
// initResponse 包含 uploadId、fileId、各分片预签名 URL

// 分片上传 — 完成
MultipartCompleteRequest completeRequest = new MultipartCompleteRequest();
completeRequest.setUploadId(initResponse.getUploadId());
completeRequest.setFileId(initResponse.getFileId());
completeRequest.setBucketName("media-bucket");
completeRequest.setOssSp(initResponse.getOssSp());
completeRequest.setFileName("large-video.mp4");
completeRequest.setFileSize(1024L * 1024 * 500);
completeRequest.setCategory("video");
List<CompletedPartInfo> parts = new ArrayList<>();
parts.add(new CompletedPartInfo() {{ setPartNumber(1); setETag("etag-1"); }});
completeRequest.setParts(parts);
MdmFileosRecordDto record = fileosUploadApi.completeMultipartUpload(completeRequest);

// 分片上传 — 中止
fileosUploadApi.abortMultipartUpload(uploadId, fileId, bucketName, ossSp);
```

### 2.2 FileosSignApi — 签名 URL 生成

`FileosSignApi` 用于为私有文件生成临时访问签名 URL，支持单文件、批量、泛型签名。

```java
@Autowired
private FileosSignApi fileosSignApi;

// 单文件签名 — 默认过期时间
String url = fileosSignApi.sign("path/to/file.jpg");

// 单文件签名 — 自定义过期时间
String url = fileosSignApi.sign("path/to/file.jpg", 60, TimeUnit.MINUTES);

// 批量签名 — 默认过期时间
List<String> urls = fileosSignApi.sign(List.of("file1.jpg", "file2.png"));

// 批量签名 — 自定义过期时间
List<String> urls = fileosSignApi.sign(List.of("file1.jpg", "file2.png"), 30, TimeUnit.MINUTES);

// 内容签名（签名 URL 用于内容访问）
String contentUrl = fileosSignApi.signContent("path/to/content.html");

// 泛型签名 — 对实体中的 fileId 字段自动签名并回填
fileosSignApi.sign(userEntity, User::getAvatarFileId, User::setAvatarUrl);

// 泛型签名 — 批量实体
fileosSignApi.sign(userList, User::getAvatarFileId, User::setAvatarUrl);
```

### 2.3 FileosDownloadApi — 文件下载

`FileosDownloadApi` 提供服务端文件流下载，适用于后端需要读取文件内容的场景。

```java
@Autowired
private FileosDownloadApi fileosDownloadApi;

// 全量下载
InputStream inputStream = fileosDownloadApi.download("path/to/file.pdf");

// 范围下载（支持断点续传）
InputStream inputStream = fileosDownloadApi.download("path/to/file.pdf", 1024, 4096);
```

### 2.4 FileosDeleteApi — 文件删除

`FileosDeleteApi` 提供单文件和批量删除能力。

```java
@Autowired
private FileosDeleteApi fileosDeleteApi;

// 单文件删除
Integer count = fileosDeleteApi.delete("path/to/file.jpg");

// 批量删除
Integer count = fileosDeleteApi.delete(List.of("file1.jpg", "file2.png", "file3.doc"));
```

### 2.5 FileosPresignUploadApi — 预签名上传

`FileosPresignUploadApi` 提供客户端直传能力，后端仅生成预签名 URL，文件由前端/客户端直接上传到 OSS，减轻服务端带宽压力。

```java
@Autowired
private FileosPresignUploadApi fileosPresignUploadApi;

// 预签名简单上传
PresignUploadRequest request = new PresignUploadRequest();
request.setFileName("photo.jpg");
request.setFileSize(1024L * 100);
request.setContentType("image/jpeg");
request.setCategory("avatar");
request.setBucketName("my-bucket");
request.setIsPublic(false);
request.setExpireMinutes(30);
PresignUploadResponse response = fileosPresignUploadApi.presignUpload(request);
// response.getPresignUrl() 交给前端 PUT 上传
// response.getFileId() 用于后续确认

// 预签名批量简单上传
List<PresignUploadResponse> responses = fileosPresignUploadApi.presignUploadBatch(requestList);

// 预签名分片上传 — 初始化
MultipartUploadInitRequest initRequest = new MultipartUploadInitRequest();
initRequest.setFileName("big-file.zip");
initRequest.setFileSize(1024L * 1024 * 200);
initRequest.setContentType("application/zip");
initRequest.setCategory("archive");
initRequest.setPartCount(20);
initRequest.setExpireMinutes(60);
MultipartUploadInitResponse initResponse = fileosPresignUploadApi.initMultipartUpload(initRequest);
// initResponse.getParts() 包含每个分片的预签名 URL

// 预签名分片上传 — 完成
MultipartCompleteRequest completeRequest = new MultipartCompleteRequest();
completeRequest.setUploadId(initResponse.getUploadId());
completeRequest.setFileId(initResponse.getFileId());
completeRequest.setBucketName("my-bucket");
completeRequest.setOssSp(initResponse.getOssSp());
completeRequest.setFileName("big-file.zip");
completeRequest.setFileSize(1024L * 1024 * 200);
completeRequest.setParts(completedParts);
MdmFileosRecordDto record = fileosPresignUploadApi.completeMultipartUpload(completeRequest);

// 预签名分片上传 — 中止
fileosPresignUploadApi.abortMultipartUpload(uploadId, fileId, bucketName, ossSp);

// 预签名简单上传 — 完成确认（前端 PUT 完成后调用）
PresignCompleteRequest completeRequest = new PresignCompleteRequest();
completeRequest.setFileId(response.getFileId());
completeRequest.setOssSp(response.getOssSp());
completeRequest.setBucketName(response.getBucketName());
completeRequest.setFileName("photo.jpg");
completeRequest.setFileSize(1024L * 100);
completeRequest.setCategory("avatar");
completeRequest.setIsPublic(false);
MdmFileosRecordDto record = fileosPresignUploadApi.presignComplete(completeRequest);

// 预签名简单上传 — 批量完成确认
List<MdmFileosRecordDto> records = fileosPresignUploadApi.presignCompleteBatch(completeRequests);
```

---

## 3. REST API 接口清单

所有接口前缀：`/micro-fileos`

### 3.1 Bucket 管理

| # | 方法 | 路径 | 说明 | 参数 | 返回值 |
|---|------|------|------|------|--------|
| 1 | GET | `/bucket/page` | Bucket 分页查询 | `MdmFileosBucket` 实体字段作为查询条件 | `R<PageData<MdmFileosBucket>>` |
| 2 | GET | `/bucket/info` | Bucket 详情 | `id` (Long) | `R<MdmFileosBucket>` |
| 3 | POST | `/bucket/create` | 创建 Bucket | `MdmFileosBucket` JSON Body | `R<?>` |
| 4 | POST | `/bucket/update` | 修改 Bucket | `MdmFileosBucket` JSON Body | `R<?>` |
| 5 | POST | `/bucket/remove` | 删除 Bucket | `MdmFileosBucket` JSON Body | `R<?>` |
| 6 | GET | `/bucket/options` | Bucket 选项列表 | `MdmFileosBucket` 实体字段作为查询条件 | `R<List<MdmFileosBucket>>` |

### 3.2 目录管理

| # | 方法 | 路径 | 说明 | 参数 | 返回值 |
|---|------|------|------|------|--------|
| 7 | GET | `/directory/list` | 目录列表 | `parentPath` (必填), `bucketName` (可选) | `R<List<MdmFileosDirectory>>` |
| 8 | GET | `/directory/tree` | 目录树 | `bucketName` (可选) | `R<List<MdmFileosDirectoryDto>>` |
| 9 | GET | `/directory/info` | 目录详情 | `dirPath` (必填), `bucketName` (可选) | `R<MdmFileosDirectory>` |

### 3.3 文件上传

| # | 方法 | 路径 | 说明 | 参数 | 返回值 |
|---|------|------|------|------|--------|
| 10 | POST | `/upload/simple` | 简单上传 | `file` (MultipartFile), `category` (可选), `bucketName` (可选) | `R<MdmFileosRecordDto>` |
| 11 | POST | `/upload/simple/public` | 公开上传 | `file` (MultipartFile), `category` (可选), `bucketName` (可选) | `R<MdmFileosRecordDto>` |
| 12 | POST | `/upload/multipart/init` | 分片上传初始化 | `MultipartUploadInitRequest` JSON Body | `R<MultipartUploadInitResponse>` |
| 13 | POST | `/upload/multipart/complete` | 分片上传完成 | `MultipartCompleteRequest` JSON Body | `R<MdmFileosRecordDto>` |
| 14 | POST | `/upload/multipart/abort` | 分片上传中止 | `uploadId`, `fileId`, `bucketName` (可选), `ossSp` (可选) | `R<?>` |

### 3.4 文件下载

| # | 方法 | 路径 | 说明 | 参数 | 返回值 |
|---|------|------|------|------|--------|
| 15 | GET | `/download/{fileId}` | 文件下载 | `fileId` (路径参数), 支持 `Range` 请求头断点续传 | 文件流 (application/octet-stream) |

### 3.5 预签名上传

| # | 方法 | 路径 | 说明 | 参数 | 返回值 |
|---|------|------|------|------|--------|
| 16 | POST | `/presign/upload` | 预签名简单上传 | `PresignUploadRequest` JSON Body | `R<PresignUploadResponse>` |
| 17 | POST | `/presign/upload/batch` | 预签名批量简单上传 | `List<PresignUploadRequest>` JSON Body | `R<List<PresignUploadResponse>>` |
| 18 | POST | `/presign/multipart/init` | 预签名分片上传初始化 | `MultipartUploadInitRequest` JSON Body | `R<MultipartUploadInitResponse>` |
| 19 | POST | `/presign/multipart/complete` | 预签名分片上传完成 | `MultipartCompleteRequest` JSON Body | `R<MdmFileosRecordDto>` |
| 20 | POST | `/presign/multipart/abort` | 预签名分片上传中止 | `uploadId`, `fileId`, `bucketName` (可选), `ossSp` (可选) | `R<?>` |
| 21 | POST | `/presign/complete` | 预签名简单上传完成确认 | `PresignCompleteRequest` JSON Body | `R<MdmFileosRecordDto>` |
| 22 | POST | `/presign/complete/batch` | 预签名批量完成确认 | `List<PresignCompleteRequest>` JSON Body | `R<List<MdmFileosRecordDto>>` |

### 3.6 签名 URL

| # | 方法 | 路径 | 说明 | 参数 | 返回值 |
|---|------|------|------|------|--------|
| 23 | GET | `/sign/url` | 单文件签名 | `fileId` (必填), `expireMinutes` (可选) | `R<String>` |
| 24 | POST | `/sign/urls` | 多文件签名 | `List<String>` JSON Body (fileId 列表) | `R<List<String>>` |

### 3.7 文件记录管理

| # | 方法 | 路径 | 说明 | 参数 | 返回值 |
|---|------|------|------|------|--------|
| 25 | GET | `/record/page` | 文件记录分页 | `MdmFileosRecord` 实体字段作为查询条件 | `R<PageData<MdmFileosRecord>>` |
| 26 | GET | `/record/info` | 文件记录详情 | `id` (Long) | `R<MdmFileosRecord>` |
| 27 | POST | `/record/remove` | 删除文件记录 | `MdmFileosRecord` JSON Body (含 id) | `R<?>` |

---

## 4. 前端接入指南

### 4.1 FileosUploader 组件设计

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `mode` | `'simple' \| 'presign' \| 'multipart'` | `'presign'` | 上传模式 |
| `accept` | `string` | — | 允许的文件类型 |
| `multiple` | `boolean` | `false` | 是否允许多文件 |
| `maxSize` | `number` | `50` (MB) | 单文件最大大小 |
| `category` | `string` | — | 业务分类 |
| `bucketName` | `string` | — | 指定 Bucket |
| `isPublic` | `boolean` | `false` | 是否公开读 |
| `autoUpload` | `boolean` | `true` | 选择后自动上传 |
| `chunkSize` | `number` | `5` (MB) | 分片大小 |
| `concurrency` | `number` | `3` | 并发上传数 |
| `imageProcess` | `object` | — | 图片处理参数 |

#### Events

| 事件 | 参数 | 说明 |
|------|------|------|
| `upload-start` | `{ file, fileId }` | 上传开始 |
| `upload-progress` | `{ fileId, loaded, total, percent }` | 上传进度 |
| `upload-success` | `{ fileId, record }` | 上传成功 |
| `upload-error` | `{ fileId, error }` | 上传失败 |
| `upload-complete` | `{ results }` | 全部上传完成 |

#### 返回数据结构

```typescript
interface FileosRecord {
  id: number
  fileId: string
  fileName: string
  fileType: string
  fileSize: number
  fileHash: string
  contentType: string
  category: string
  dirPath: string
  isPublic: number
  ossSp: string
  bucketName: string
  uploadType: string
  uploadId: string
  uploadStatus: string
  imageProcess: string
  previewUrl: string
}
```

#### 公共类型：ImageProcess

图片处理参数，用于上传组件的 `imageProcess` 属性，支持缩放、裁剪、水印等 OSS 图片处理能力。

```typescript
interface ImageProcess {
  resize?: {
    width?: number
    height?: number
    mode?: 'fit' | 'fill' | 'exact'  // 等比缩放/填充/精确
  }
  crop?: {
    x?: number
    y?: number
    width?: number
    height?: number
  }
  watermark?: {
    text?: string
    position?: 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right' | 'center'
    opacity?: number    // 0-100
    fontSize?: number   // px
  }
}
```

### 4.2 简单上传流程（预签名模式）

推荐使用预签名模式，文件由前端直传 OSS，不经过后端服务器。

```
┌────────┐     ①请求预签名URL     ┌────────┐
│  前端   │ ──────────────────→  │  后端   │
│        │ ←──────────────────  │        │
│        │     presignUrl+fileId │        │
│        │                       └────────┘
│        │
│        │     ② PUT文件到OSS     ┌────────┐
│        │ ──────────────────→  │  OSS    │
│        │ ←──────────────────  │        │
│        │     200 OK            └────────┘
│        │
│        │     ③确认上传完成      ┌────────┐
│        │ ──────────────────→  │  后端   │
│        │ ←──────────────────  │        │
│        │     record            └────────┘
└────────┘
```

```javascript
// ① 请求预签名 URL
const presignResp = await fetch('/micro-fileos/presign/upload', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    fileName: file.name,
    fileSize: file.size,
    contentType: file.type,
    category: 'avatar',
    bucketName: 'my-bucket',
    isPublic: false,
    expireMinutes: 30
  })
})
const { data } = await presignResp.json()
const { fileId, presignUrl, ossSp, bucketName } = data

// ② PUT 文件到 OSS
const uploadResp = await fetch(presignUrl, {
  method: 'PUT',
  headers: { 'Content-Type': file.type },
  body: file
})

// ③ 确认上传完成
const completeResp = await fetch('/micro-fileos/presign/complete', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    fileId,
    ossSp,
    bucketName,
    fileName: file.name,
    fileSize: file.size,
    category: 'avatar',
    isPublic: false
  })
})
const record = (await completeResp.json()).data
```

### 4.3 分片上传流程

适用于大文件（>50MB），将文件拆分为多个分片并行上传。

```
┌────────┐  ①初始化分片上传   ┌────────┐
│  前端   │ ──────────────→  │  后端   │
│        │ ←──────────────  │        │
│        │  uploadId+parts   └────────┘
│        │
│        │  ②逐片PUT到OSS    ┌────────┐
│        │ ──────────────→  │  OSS    │
│        │ ←──────────────  │        │
│        │  eTag             └────────┘
│        │  (重复②直到所有分片完成)
│        │
│        │  ③完成分片上传     ┌────────┐
│        │ ──────────────→  │  后端   │
│        │ ←──────────────  │        │
│        │  record           └────────┘
└────────┘
```

```javascript
const CHUNK_SIZE = 5 * 1024 * 1024 // 5MB

// ① 初始化分片上传
const file = document.getElementById('fileInput').files[0]
const partCount = Math.ceil(file.size / CHUNK_SIZE)

const initResp = await fetch('/micro-fileos/presign/multipart/init', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    fileName: file.name,
    fileSize: file.size,
    contentType: file.type,
    category: 'video',
    bucketName: 'media-bucket',
    partCount,
    expireMinutes: 60
  })
})
const { uploadId, fileId, ossSp, bucketName, parts } = (await initResp.json()).data

// ② 逐片上传
const completedParts = []
for (const part of parts) {
  const start = (part.partNumber - 1) * CHUNK_SIZE
  const end = Math.min(start + CHUNK_SIZE, file.size)
  const chunk = file.slice(start, end)

  const partResp = await fetch(part.presignUrl, {
    method: 'PUT',
    headers: { 'Content-Type': file.type },
    body: chunk
  })
  const eTag = partResp.headers.get('ETag')
  completedParts.push({ partNumber: part.partNumber, eTag })
}

// ③ 完成分片上传
const completeResp = await fetch('/micro-fileos/presign/multipart/complete', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    uploadId,
    fileId,
    bucketName,
    ossSp,
    fileName: file.name,
    fileSize: file.size,
    category: 'video',
    parts: completedParts
  })
})
const record = (await completeResp.json()).data
```

### 4.4 断点续传流程

分片上传天然支持断点续传，核心思路是记录已完成的分片信息，恢复时跳过已上传的分片。

```javascript
// 上传进度持久化（localStorage / IndexedDB）
function saveProgress(fileId, completedParts) {
  localStorage.setItem(`fileos_upload_${fileId}`, JSON.stringify({
    fileId,
    uploadId,
    completedParts,
    timestamp: Date.now()
  }))
}

function loadProgress(fileId) {
  const data = localStorage.getItem(`fileos_upload_${fileId}`)
  return data ? JSON.parse(data) : null
}

// 恢复上传
async function resumeUpload(file) {
  const progress = loadProgress(file.id) // 用文件 hash 或 fileId 恢复
  if (!progress) {
    return startNewUpload(file) // 无进度记录，重新上传
  }

  const { uploadId, fileId, completedParts } = progress

  // 重新获取分片预签名 URL（原 URL 可能已过期）
  const initResp = await fetch('/micro-fileos/presign/multipart/init', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fileName: file.name,
      fileSize: file.size,
      contentType: file.type,
      partCount: Math.ceil(file.size / CHUNK_SIZE),
      expireMinutes: 60
    })
  })
  const { parts } = (await initResp.json()).data

  // 过滤已完成的分片
  const completedNumbers = new Set(completedParts.map(p => p.partNumber))
  const pendingParts = parts.filter(p => !completedNumbers.has(p.partNumber))

  // 仅上传未完成的分片
  for (const part of pendingParts) {
    const start = (part.partNumber - 1) * CHUNK_SIZE
    const end = Math.min(start + CHUNK_SIZE, file.size)
    const chunk = file.slice(start, end)

    const partResp = await fetch(part.presignUrl, {
      method: 'PUT',
      body: chunk
    })
    const eTag = partResp.headers.get('ETag')
    completedParts.push({ partNumber: part.partNumber, eTag })
    saveProgress(fileId, completedParts) // 每片完成后保存进度
  }

  // 完成上传
  await completeMultipartUpload(uploadId, fileId, completedParts)
}
```

### 4.5 FileosDirectoryBrowser — 目录浏览组件

用于浏览和管理文件目录结构，支持文件选择、预览和删除操作。

#### Props

```typescript
interface FileosDirectoryBrowserProps {
  action: string
  bucket?: string
  selectable?: boolean         // 是否可选择文件，默认 false
  multiple?: boolean           // 是否多选，默认 false
  accept?: string
  showPreview?: boolean        // 是否显示预览，默认 true
  onDelete?: boolean           // 是否允许删除，默认 false
}
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `action` | `string` | — | API 基础路径，如 `/micro-fileos` |
| `bucket` | `string` | — | 指定 Bucket |
| `selectable` | `boolean` | `false` | 是否可选择文件 |
| `multiple` | `boolean` | `false` | 是否多选（`selectable` 为 `true` 时生效） |
| `accept` | `string` | — | 可选择的文件类型过滤 |
| `showPreview` | `boolean` | `true` | 是否显示文件预览 |
| `onDelete` | `boolean` | `false` | 是否允许删除文件 |

#### 使用示例

```javascript
// 加载目录树
const treeResp = await fetch('/micro-fileos/directory/tree?bucketName=my-bucket')
const tree = (await treeResp.json()).data

// 加载子目录列表
const listResp = await fetch(`/micro-fileos/directory/list?parentPath=/images&bucketName=my-bucket`)
const directories = (await listResp.json()).data

// 获取目录详情
const infoResp = await fetch(`/micro-fileos/directory/info?dirPath=/images/avatar&bucketName=my-bucket`)
const directory = (await infoResp.json()).data
```

### 4.6 FileosImageUploader — 图片上传组件

专门用于图片上传，带预览、裁剪、压缩功能。

#### Props

```typescript
interface FileosImageUploaderProps {
  action: string
  bucket?: string
  category?: string
  accept?: string              // 默认 'image/*'
  maxSize?: number             // 默认 10 (MB)
  maxCount?: number            // 默认 9
  multiple?: boolean           // 默认 true
  compress?: boolean           // 是否压缩，默认 true
  compressQuality?: number     // 压缩质量 0-1，默认 0.8
  crop?: boolean               // 是否裁剪，默认 false
  cropRatio?: number           // 裁剪比例，如 1/1, 4/3, 16/9
  imageProcess?: ImageProcess  // 图片处理参数（缩放、水印）
  listType?: 'text' | 'picture' | 'picture-card'  // 默认 'picture-card'
  disabled?: boolean
}
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `action` | `string` | — | API 基础路径，如 `/micro-fileos` |
| `bucket` | `string` | — | 指定 Bucket |
| `category` | `string` | — | 业务分类 |
| `accept` | `string` | `'image/*'` | 允许的文件类型 |
| `maxSize` | `number` | `10` (MB) | 单文件最大大小 |
| `maxCount` | `number` | `9` | 最大上传数量 |
| `multiple` | `boolean` | `true` | 是否允许多文件 |
| `compress` | `boolean` | `true` | 是否启用压缩 |
| `compressQuality` | `number` | `0.8` | 压缩质量（0-1） |
| `crop` | `boolean` | `false` | 是否启用裁剪 |
| `cropRatio` | `number` | — | 裁剪比例，如 `1/1`、`4/3`、`16/9` |
| `imageProcess` | `ImageProcess` | — | 图片处理参数（缩放、水印），参见 [公共类型：ImageProcess](#公共类型imageprocess) |
| `listType` | `'text' \| 'picture' \| 'picture-card'` | `'picture-card'` | 文件列表展示样式 |
| `disabled` | `boolean` | `false` | 是否禁用 |

### 4.7 FileosDragUploader — 拖拽上传组件

支持拖拽文件到区域上传，适用于批量文件上传场景。

#### Props

```typescript
interface FileosDragUploaderProps {
  action: string
  bucket?: string
  category?: string
  accept?: string
  maxSize?: number             // 默认 50 (MB)
  maxCount?: number            // 默认 5
  multiple?: boolean           // 默认 true
  isPublic?: boolean
  imageProcess?: ImageProcess
  disabled?: boolean
  dragAreaText?: string        // 拖拽区域提示文字，默认 '将文件拖到此处，或点击上传'
  dragAreaIcon?: string        // 拖拽区域图标
}
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `action` | `string` | — | API 基础路径，如 `/micro-fileos` |
| `bucket` | `string` | — | 指定 Bucket |
| `category` | `string` | — | 业务分类 |
| `accept` | `string` | — | 允许的文件类型 |
| `maxSize` | `number` | `50` (MB) | 单文件最大大小 |
| `maxCount` | `number` | `5` | 最大上传数量 |
| `multiple` | `boolean` | `true` | 是否允许多文件 |
| `isPublic` | `boolean` | `false` | 是否公开读 |
| `imageProcess` | `ImageProcess` | — | 图片处理参数（缩放、水印），参见 [公共类型：ImageProcess](#公共类型imageprocess) |
| `disabled` | `boolean` | `false` | 是否禁用 |
| `dragAreaText` | `string` | `'将文件拖到此处，或点击上传'` | 拖拽区域提示文字 |
| `dragAreaIcon` | `string` | — | 拖拽区域图标 |

### 4.8 FileosChunkUploader — 大文件分片上传组件

专门用于大文件上传，支持分片、断点续传。

#### Props

```typescript
interface FileosChunkUploaderProps {
  action: string
  bucket?: string
  category?: string
  accept?: string
  maxSize?: number             // 默认 500 (MB)
  chunkSize?: number           // 分片大小 (MB)，默认 5
  chunkThreshold?: number      // 触发分片上传的阈值 (MB)，默认 10
  concurrent?: number          // 并发上传数，默认 3
  retryCount?: number          // 失败重试次数，默认 3
  isPublic?: boolean
  disabled?: boolean
  showProgress?: boolean       // 是否显示进度，默认 true
  showSpeed?: boolean          // 是否显示速度，默认 true
}
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `action` | `string` | — | API 基础路径，如 `/micro-fileos` |
| `bucket` | `string` | — | 指定 Bucket |
| `category` | `string` | — | 业务分类 |
| `accept` | `string` | — | 允许的文件类型 |
| `maxSize` | `number` | `500` (MB) | 单文件最大大小 |
| `chunkSize` | `number` | `5` (MB) | 分片大小 |
| `chunkThreshold` | `number` | `10` (MB) | 触发分片上传的阈值，低于此值使用简单上传 |
| `concurrent` | `number` | `3` | 并发上传数 |
| `retryCount` | `number` | `3` | 失败重试次数 |
| `isPublic` | `boolean` | `false` | 是否公开读 |
| `disabled` | `boolean` | `false` | 是否禁用 |
| `showProgress` | `boolean` | `true` | 是否显示上传进度 |
| `showSpeed` | `boolean` | `true` | 是否显示上传速度 |

---

## 5. 数据库表结构

### 5.1 mdm_fileos_bucket — Bucket 配置表

```sql
CREATE TABLE `mdm_fileos_bucket` (
  `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_code`     varchar(63)  DEFAULT NULL COMMENT '租户编码',
  `bucket_name`     varchar(127) NOT NULL COMMENT 'Bucket名称',
  `oss_sp`          varchar(31)  NOT NULL COMMENT 'OSS服务商(ALI_OSS/AWS_S3/S3_COMPATIBLE)',
  `endpoint_inner`  varchar(255) DEFAULT NULL COMMENT '内网Endpoint',
  `endpoint_outer`  varchar(255) DEFAULT NULL COMMENT '外网Endpoint',
  `region`          varchar(63)  DEFAULT NULL COMMENT '区域',
  `access_key`      varchar(255) NOT NULL COMMENT 'Access Key',
  `secret_key`      varchar(255) NOT NULL COMMENT 'Secret Key',
  `default_flag`    int          DEFAULT 0  COMMENT '默认标识(1=默认Bucket)',
  `system`          varchar(63)  DEFAULT NULL COMMENT '系统标识',
  `sort`            int          DEFAULT 0  COMMENT '排序',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by`       varchar(31)  DEFAULT NULL COMMENT '创建人',
  `update_time`     datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_by`       varchar(31)  DEFAULT NULL COMMENT '修改人',
  `remark`          varchar(255) DEFAULT NULL COMMENT '备注',
  `version`         int          DEFAULT 0  COMMENT '乐观锁',
  `deleted`         varchar(24)  DEFAULT '0' COMMENT '逻辑删除(0=未删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bucket_name` (`bucket_name`, `tenant_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储Bucket配置';
```

### 5.2 mdm_fileos_record — 文件记录表

```sql
CREATE TABLE `mdm_fileos_record` (
  `id`             bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_code`    varchar(63)   DEFAULT NULL COMMENT '租户编码',
  `file_id`        varchar(511)  NOT NULL COMMENT '文件存储路径',
  `file_name`      varchar(255)  DEFAULT NULL COMMENT '原始文件名',
  `file_type`      varchar(31)   DEFAULT NULL COMMENT '文件扩展名',
  `file_size`      bigint        DEFAULT NULL COMMENT '文件大小(字节)',
  `file_hash`      varchar(127)  DEFAULT NULL COMMENT '文件Hash',
  `content_type`   varchar(127)  DEFAULT NULL COMMENT 'MIME类型',
  `category`       varchar(63)   DEFAULT NULL COMMENT '业务分类',
  `dir_path`       varchar(511)  DEFAULT NULL COMMENT '所属目录路径',
  `is_public`      int           DEFAULT 0  COMMENT '是否公共读(0=私有,1=公开)',
  `oss_sp`         varchar(31)   DEFAULT NULL COMMENT 'OSS服务商',
  `bucket_name`    varchar(127)  DEFAULT NULL COMMENT '所属Bucket',
  `upload_type`    varchar(31)   DEFAULT NULL COMMENT '上传方式(SIMPLE/MULTIPART/PRESIGN)',
  `upload_id`      varchar(127)  DEFAULT NULL COMMENT '分片上传ID',
  `upload_status`  varchar(31)   DEFAULT NULL COMMENT '上传状态(UPLOADING/COMPLETED/ABORTED)',
  `image_process`  varchar(1023) DEFAULT NULL COMMENT '图片处理参数(JSON)',
  `sort`           int           DEFAULT 0  COMMENT '排序',
  `create_time`    datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by`      varchar(31)   DEFAULT NULL COMMENT '创建人',
  `update_time`    datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_by`      varchar(31)   DEFAULT NULL COMMENT '修改人',
  `remark`         varchar(255)  DEFAULT NULL COMMENT '备注',
  `version`        int           DEFAULT 0  COMMENT '乐观锁',
  `deleted`        varchar(24)   DEFAULT '0' COMMENT '逻辑删除(0=未删除)',
  PRIMARY KEY (`id`),
  KEY `idx_file_id` (`file_id`(255)),
  KEY `idx_category` (`category`),
  KEY `idx_file_hash` (`file_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储记录';
```

### 5.3 mdm_fileos_directory — 目录表

```sql
CREATE TABLE `mdm_fileos_directory` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_code`  varchar(63)  DEFAULT NULL COMMENT '租户编码',
  `bucket_name`  varchar(127) DEFAULT NULL COMMENT '所属Bucket',
  `dir_path`     varchar(511) NOT NULL COMMENT '目录完整路径',
  `dir_name`     varchar(127) DEFAULT NULL COMMENT '目录名',
  `parent_path`  varchar(511) DEFAULT NULL COMMENT '父目录路径',
  `dir_level`    int          DEFAULT NULL COMMENT '目录层级',
  `file_count`   bigint       DEFAULT 0  COMMENT '文件数量',
  `total_size`   bigint       DEFAULT 0  COMMENT '文件总大小(字节)',
  `sort`         int          DEFAULT 0  COMMENT '排序',
  `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by`    varchar(31)  DEFAULT NULL COMMENT '创建人',
  `update_time`  datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_by`    varchar(31)  DEFAULT NULL COMMENT '修改人',
  `remark`       varchar(255) DEFAULT NULL COMMENT '备注',
  `version`      int          DEFAULT 0  COMMENT '乐观锁',
  `deleted`      varchar(24)  DEFAULT '0' COMMENT '逻辑删除(0=未删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_bucket_dirpath` (`tenant_code`, `bucket_name`, `dir_path`(255)),
  KEY `idx_parent_path` (`parent_path`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件目录';
```

### 5.4 mdm_fileos_multipart — 分片上传记录表

```sql
CREATE TABLE `mdm_fileos_multipart` (
  `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_code`     varchar(63)  DEFAULT NULL COMMENT '租户编码',
  `upload_id`       varchar(127) NOT NULL COMMENT '分片上传ID',
  `file_id`         varchar(511) NOT NULL COMMENT '文件存储路径',
  `file_name`       varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_size`       bigint       DEFAULT NULL COMMENT '文件大小(字节)',
  `content_type`    varchar(127) DEFAULT NULL COMMENT 'MIME类型',
  `category`        varchar(63)  DEFAULT NULL COMMENT '业务分类',
  `is_public`       int          DEFAULT 0  COMMENT '是否公共读',
  `oss_sp`          varchar(31)  DEFAULT NULL COMMENT 'OSS服务商',
  `bucket_name`     varchar(127) DEFAULT NULL COMMENT '所属Bucket',
  `part_count`      int          DEFAULT NULL COMMENT '分片总数',
  `completed_parts` text         DEFAULT NULL COMMENT '已完成分片信息(JSON)',
  `status`          varchar(31)  DEFAULT 'UPLOADING' COMMENT '状态(UPLOADING/COMPLETED/ABORTED)',
  `expire_time`     datetime     DEFAULT NULL COMMENT '过期时间',
  `sort`            int          DEFAULT 0  COMMENT '排序',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by`       varchar(31)  DEFAULT NULL COMMENT '创建人',
  `update_time`     datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_by`       varchar(31)  DEFAULT NULL COMMENT '修改人',
  `remark`          varchar(255) DEFAULT NULL COMMENT '备注',
  `version`         int          DEFAULT 0  COMMENT '乐观锁',
  `deleted`         varchar(24)  DEFAULT '0' COMMENT '逻辑删除(0=未删除)',
  PRIMARY KEY (`id`),
  KEY `idx_upload_id` (`upload_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传记录';
```

---

## 6. 初始化配置

### 6.1 Bucket 配置

通过 REST API 或直接插入数据库配置 Bucket：

```bash
# 通过 API 创建 Bucket
curl -X POST http://localhost:8080/micro-fileos/bucket/create \
  -H 'Content-Type: application/json' \
  -d '{
    "bucketName": "my-bucket",
    "ossSp": "ALI_OSS",
    "endpointInner": "https://oss-cn-hangzhou-internal.aliyuncs.com",
    "endpointOuter": "https://oss-cn-hangzhou.aliyuncs.com",
    "region": "cn-hangzhou",
    "accessKey": "your-access-key",
    "secretKey": "your-secret-key",
    "defaultFlag": 1,
    "system": "main"
  }'
```

### 6.2 application.yml 配置

```yaml
sh:
  fileos:
    max-size-mb: 50                    # 全局最大文件大小(MB)
    image:
      max-size-mb: 10                  # 图片最大大小(MB)
      extension-names: jpg,jpeg,png,gif,webp,svg,bmp  # 图片扩展名
    video:
      max-size-mb: 500                 # 视频最大大小(MB)
      extension-names: mp4,mpeg,avi,mov,wmv,rm,rmvb,mkv,flv  # 视频扩展名
    presign:
      expire-minutes: 30               # 预签名URL过期时间(分钟)
      multipart:
        expire-minutes: 60             # 分片预签名URL过期时间(分钟)
        default-part-size-mb: 5        # 默认分片大小(MB)
    multipart:
      max-age-hours: 24                # 分片上传记录过期时间(小时)
    hash:
      enabled: true                    # 是否启用Hash去重
      algorithm: SHA-256               # Hash算法
```

---

## 7. DTO 数据结构参考

### PresignUploadResponse

| 字段 | 类型 | 说明 |
|------|------|------|
| `fileId` | String | 文件存储路径（唯一标识） |
| `presignUrl` | String | 预签名上传 URL |
| `ossSp` | String | OSS 服务商 |
| `bucketName` | String | 所属 Bucket |
| `contentType` | String | MIME 类型 |
| `expireMinutes` | Integer | URL 过期时间（分钟） |

### MultipartUploadInitResponse

| 字段 | 类型 | 说明 |
|------|------|------|
| `uploadId` | String | 分片上传 ID |
| `fileId` | String | 文件存储路径 |
| `ossSp` | String | OSS 服务商 |
| `bucketName` | String | 所属 Bucket |
| `contentType` | String | MIME 类型 |
| `expireMinutes` | Integer | URL 过期时间（分钟） |
| `parts` | List\<PresignedPartInfo\> | 各分片预签名信息 |

### PresignedPartInfo

| 字段 | 类型 | 说明 |
|------|------|------|
| `partNumber` | Integer | 分片序号（从 1 开始） |
| `presignUrl` | String | 分片预签名上传 URL |

### CompletedPartInfo

| 字段 | 类型 | 说明 |
|------|------|------|
| `partNumber` | Integer | 分片序号 |
| `eTag` | String | 分片 ETag（OSS 返回） |

### ImageProcessParam

| 字段 | 类型 | 说明 |
|------|------|------|
| `resize` | ResizeParam | 缩放参数 |
| `crop` | CropParam | 裁剪参数 |
| `watermark` | WatermarkParam | 水印参数 |

### ResizeParam

| 字段 | 类型 | 说明 |
|------|------|------|
| `width` | Integer | 目标宽度 |
| `height` | Integer | 目标高度 |
| `mode` | String | 缩放模式（lfit=等比缩放, mfit=裁剪缩放, fill=填充缩放, pad=按长边缩放） |

### CropParam

| 字段 | 类型 | 说明 |
|------|------|------|
| `x` | Integer | 裁剪起始 X 坐标 |
| `y` | Integer | 裁剪起始 Y 坐标 |
| `width` | Integer | 裁剪宽度 |
| `height` | Integer | 裁剪高度 |

### WatermarkParam

| 字段 | 类型 | 说明 |
|------|------|------|
| `text` | String | 水印文字 |
| `position` | String | 水印位置（tl=左上, tr=右上, bl=左下, br=右下, center=居中） |
| `opacity` | Integer | 透明度（0-100） |
| `fontSize` | Integer | 字体大小 |
