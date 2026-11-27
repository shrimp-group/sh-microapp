# micro-seq 模块开发指南

## 模块概述

micro-seq 是 sh-microapp 微应用集合中的序列号生成模块，按前缀（prefix）维护单调递增的业务序列号，用于生成编码类主键/单号（如订单号、物料编码）。对外暴露 `SeqApi`，管理端仅提供序列记录的查询与修正。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-seq`
- **API 前缀**: `/micro-seq`
- **数据表**: `mdm_sequence`（序列生成记录）

## 架构设计

```
宿主应用
    ↓ seqApi.genSequence / genSequences
API             SeqApi（拼接前缀，透传 Service）
    ↓
Service         MdmSequenceService.genSequences
                （@Transactional(SERIALIZABLE) + synchronized + 乐观锁更新）
    ↓
Mapper          MdmSequenceMapper（getSequenceInfo / insertSequenceInfo / updateSequenceInfo）
REST            SequenceRest（序列记录分页 / 详情 / 修正）
```

| 保障层 | 机制 | 说明 |
|--------|------|------|
| JVM | `synchronized` | 单实例内串行 |
| 事务 | `Isolation.SERIALIZABLE` | 跨实例串行化事务 |
| 数据 | 乐观锁 | `updateSequenceInfo` 带 `AND version = #{version}`，更新 0 行即竞争失败 |

设计取向（源码注释）：序列唯一性非常重要、生成数量极少，牺牲性能保功能。

## 目录结构

```
micro-seq/src/main/java/com/wkclz/micro/seq/
├── SeqAutoConfig.java                 # 自动配置（@ComponentScan + @MapperScan）
├── api/
│   └── SeqApi.java                    # 对外生成 API（@Component）
├── bean/
│   ├── dto/
│   │   └── MdmSequenceDto.java        # 序列 DTO
│   ├── entity/
│   │   └── MdmSequence.java           # 实体（mdm_sequence，含手写 copy/copyIfNotNull）
│   ├── req/                           # SequencePageReq / SequenceInfoReq / SequenceUpdateReq
│   └── resp/                          # SequencePageResp / SequenceResp
├── mapper/
│   └── MdmSequenceMapper.java         # getSequenceList / getSequenceInfo / insertSequenceInfo / updateSequenceInfo
├── rest/
│   ├── Route.java                     # 路由常量（@Router）
│   └── SequenceRest.java              # 管理端 REST
└── service/
    └── MdmSequenceService.java        # 序列生成核心 + 单表服务
```

resources：`META-INF/spring/...AutoConfiguration.imports`、`mapper/MdmSequenceMapper.xml`、`mapper/base/MdmSequenceMapper.xml`

## API 端点清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/micro-seq/sequence/page` | 序列记录分页（seqName/prefix 模糊查询） |
| GET | `/micro-seq/sequence/info` | 序列记录详情 |
| POST | `/micro-seq/sequence/update` | 修正序列记录（如回拨当前值） |

> 无 create 端点：序列记录由 `genSequences` 按 prefix 惰性自动创建。

## 核心机制：序列生成

`MdmSequenceService.genSequences(prefix, size, length, seqName)`（返回值不含前缀）：

1. `prefix` 去空白后按查询 `mdm_sequence`（`getSequenceInfo`）
2. 不存在则初始化：`sequence = 0`，`codeLength` 默认 4（传入小于 4 亦按 4），`seqName` 默认取 prefix，插入后重查
3. 循环 `size` 次 `sequence + 1`，`fitLength` 左补 0 至 `codeLength` 位（超长原样返回）
4. `updateSequenceInfo` 乐观锁更新当前值（`version = version + 1`），更新行数 < 1 时抛"编码生成竞争失败，为防止重复，已放弃生成，请重新提交！"，事务回滚

### 宿主应用使用方式

```java
@Autowired SeqApi seqApi;

// 默认长度 4：返回 "ORD0001"
String seq = seqApi.genSequence("ORD");

// 指定序列名（便于管理端区分用途）
String seq2 = seqApi.genSequence("ORD", "订单序列");

// 指定长度：返回 "ORD20260001"
String seq3 = seqApi.genSequence("ORD", 8, null);

// 批量生成（一次事务推进 N 个，返回含前缀）
List<String> seqs = seqApi.genSequences("ORD", 10, "订单序列");
```

## 数据库表

### mdm_sequence

| 字段 | Java 字段 | 说明 |
|------|----------|------|
| `seq_name` | `seqName` | 序列名称 |
| `prefix` | `prefix` | 序列前缀（按此定位记录；`SeqApi` 生成结果 = prefix + 补零序列） |
| `sequence` | `sequence` | 当前序列值 |
| `code_length` | `codeLength` | 序列长度（不计前缀） |

## 依赖

- 无第三方依赖：仅依赖父 POM 默认引入的 sh-framework 基础模块（sh-core / sh-mybatis / sh-tool 等）

## 注意事项

1. 实体 `MdmSequence` 的手写 `copy` / `copyIfNotNull` 静态方法为项目统一拷贝约定，保留勿删
2. 序列记录唯一性按 `prefix` 判定（`duplicateCheck`），同一 prefix 只允许一条记录
3. 竞争失败会向上抛 `ValidationException`，调用方（生成场景极少并发）按提示重试即可
4. 修正序列值（`/sequence/update`）需谨慎：回拨会与已发放编号冲突，仅用于故障恢复
5. `genSequences` 为整批一个事务，批量越大持锁越久；大批量场景建议分批调用

**最后更新时间**: 2026-11-28
