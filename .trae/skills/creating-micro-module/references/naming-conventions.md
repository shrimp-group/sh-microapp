# 命名规范速查表

新建 micro-* 微模块时的命名约定速查。每条规则附真实示例，避免不一致。

## 1. 模块级命名

| 维度 | 规则 | 示例 |
|------|------|------|
| 模块目录名 | `micro-xxx`（小写，连字符） | micro-seq、micro-dict |
| Maven artifactId | 同模块目录名 | micro-seq |
| GroupId | 固定 `com.wkclz.microapp` | — |
| 包名根 | `com.wkclz.micro.xxx`（小写） | com.wkclz.micro.seq |
| API 前缀 | `/micro-xxx` | /micro-seq |
| Route module 属性 | `micro-xxx` | micro-seq |

## 2. 类命名

| 类型 | 规则 | 示例 |
|------|------|------|
| AutoConfig | `XxxModAutoConfig`（**不带** Micro 前缀） | SeqAutoConfig |
| Route | `Route`（固定名，位于 rest 包） | rest/Route.java |
| Entity | `MdmXxxEnt`（**Mdm** 前缀） | MdmSequence |
| Mapper 接口 | `MdmXxxEntMapper` | MdmSequenceMapper |
| Service | `MdmXxxEntService` | MdmSequenceService |
| Rest 控制器 | `XxxEntRest`（**不带** Mdm 前缀） | SequenceRest |
| 对外 API 接口 | `XxxModApi`（可选，位于 api 包） | SeqApi、PdfApi |

## 3. DTO 命名

| 类型 | 规则 | 示例 |
|------|------|------|
| 分页查询入参 | `XxxEntPageReq` | SequencePageReq |
| 详情入参 | `XxxEntInfoReq`（含 id） | SequenceInfoReq |
| 创建入参 | `XxxEntCreateReq`（含 @Valid） | SequenceCreateReq |
| 修改入参 | `XxxEntUpdateReq`（含 id + version） | SequenceUpdateReq |
| 详情返回 | `XxxEntResp` | SequenceResp |
| 分页返回 | `XxxEntPageResp` | SequencePageResp |

## 4. 数据库命名

| 维度 | 规则 | 示例 |
|------|------|------|
| 表名 | `mdm_xxx`（蛇形，mdm 前缀） | mdm_sequence、mdm_dict_item |
| 字段名 | 蛇形，Java 自动转驼峰 | seq_name → seqName |
| 主键 | `id`（bigint AUTO_INCREMENT） | — |
| 业务编码 | `xxx_code`（唯一索引） | dict_code、prefix |
| 逻辑删除 | `deleted`（varchar(24)，0=未删除） | — |
| 乐观锁 | `version`（int） | — |

## 5. 文件命名与位置

| 文件 | 位置 | 命名 |
|------|------|------|
| AutoConfig | `src/main/java/com/wkclz/micro/xxx/` | `XxxModAutoConfig.java` |
| imports | `src/main/resources/META-INF/spring/` | `org.springframework.boot.autoconfigure.AutoConfiguration.imports` |
| Route | `src/main/java/com/wkclz/micro/xxx/rest/` | `Route.java` |
| Entity | `src/main/java/com/wkclz/micro/xxx/bean/entity/` | `MdmXxxEnt.java` |
| Mapper 接口 | `src/main/java/com/wkclz/micro/xxx/mapper/` | `MdmXxxEntMapper.java` |
| Mapper.xml | `src/main/resources/mapper/` | `MdmXxxEntMapper.xml` |
| Service | `src/main/java/com/wkclz/micro/xxx/service/` | `MdmXxxEntService.java` |
| Rest | `src/main/java/com/wkclz/micro/xxx/rest/` | `XxxEntRest.java` |
| Req/Resp | `src/main/java/com/wkclz/micro/xxx/bean/req/` 或 `resp/` | `XxxEntXxxReq.java` |

## 6. 基础字段（继承 BaseEntity 自动获得）

| 数据库字段 | Java 字段 | 类型 | 说明 |
|-----------|----------|------|------|
| id | id | bigint | 主键 |
| sort | sort | int | 排序 |
| create_time | createTime | datetime | 创建时间 |
| create_by | createBy | varchar(31) | 创建人 |
| update_time | updateTime | datetime | 修改时间 |
| update_by | updateBy | varchar(31) | 修改人 |
| remark | remark | varchar(255) | 备注 |
| version | version | int | 乐观锁 |
| deleted | — | varchar(24) | 逻辑删除（BaseMapper 自动过滤，0=未删除） |

## 7. 易错点

1. **AutoConfig 不带 Micro 前缀**——`SeqAutoConfig` 而非 `MicroSeqAutoConfig`
2. **Entity 带 Mdm 前缀，Rest 不带**——`MdmSequence` vs `SequenceRest`
3. **@MapperScan 路径精确到 mapper 包**——`com.wkclz.micro.xxx.mapper`，不是 `com.wkclz.micro.xxx`
4. **@ComponentScan 路径到模块根包**——`com.wkclz.micro.xxx`
5. **imports 文件路径必须精确**——`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，无扩展名
6. **子 pom 不指定依赖版本号**——由 sh-bom 统一管理
7. **Mapper.xml 的 namespace 必须与 Mapper 接口全限定名一致**——否则 MyBatis 绑定失败
8. **所有查询必须过滤 deleted = 0**——逻辑删除由 BaseMapper 自动处理，自定义查询需手动加
