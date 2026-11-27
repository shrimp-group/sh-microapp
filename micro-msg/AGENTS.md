# micro-msg 模块开发指南

## 模块概述

micro-msg 是 sh-microapp 微应用集合中的消息通知模块，提供站内消息模板管理、通知发布（按用户生成未读记录）、个人消息列表/已读/阅读计数、用户消息偏好设置能力，并通过 `MsgApi` 供宿主业务系统发送站内消息。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-msg`
- **API 前缀**: `/micro-msg`
- **模块依赖**: `sh-redis`（RedisIdGenerator 生成通知单号）、`spring-boot-starter-validation`

## 架构设计

```
REST（manager 管理端 / personal 个人端）
    ↓
Service   MsgNotificationService（通知发布）/ MsgTemplateService（模板）
          MsgUserRecordService（用户消息）/ MsgUserSettingsService（用户设置）
    ↓
api       MsgApi（sentMsg 4 种重载：title/content/[extUrl]/单或多人）
    ↓
Mapper    4 张表 Mapper + XML（通知-记录联查）
    ↓
RedisIdGenerator（noticeNo = msg_ 前缀）/ IdentityContext（当前用户）
```

发布机制：`createNotification` 生成 `msg_` 前缀通知单号落库 `msg_notification`，为目标用户去重后批量写入 `msg_user_record`（readStatus=0、showTimes=0）。

## 目录结构

```
micro-msg/src/main/java/com/wkclz/micro/msg/
├── MsgAutoConfig.java             # 自动配置（@ComponentScan + @MapperScan）
├── api/
│   └── MsgApi.java                # 对外发送消息入口（4 个 sentMsg 重载）
├── bean/
│   ├── entity/
│   │   ├── MsgTemplate.java       # 消息模板（表 msg_template）
│   │   ├── MsgNotification.java   # 消息通知（表 msg_notification）
│   │   ├── MsgUserRecord.java     # 用户消息记录（表 msg_user_record）
│   │   └── MsgUserSettings.java   # 用户消息设置（表 msg_user_settings）
│   ├── dto/                       # 4 个实体对应 Dto（含 sentToUsers 发送目标）
│   ├── req/                       # 15 个 Req（Page/Info/Create/Update/Readed/Save 等）
│   └── resp/                      # 8 个 Resp
├── mapper/                        # 4 个 Mapper 接口
├── rest/
│   ├── Route.java                 # 路由常量（@Router，manager/personal 分组）
│   ├── ManagerMsgTemplateRest.java    # 模板 CRUD
│   ├── ManagerNotificationRest.java   # 通知分页/发布/详情/阅读记录
│   ├── PersonalUserRecordRest.java    # 个人消息列表/分页/详情/批量已读
│   └── PersonalUserSettingsRest.java  # 个人消息设置读取/保存
└── service/                       # 4 个 Service（extends BaseService）
```

## API 端点清单

前缀 `/micro-msg`，共 15 个端点（manager 9 + personal 6）：

| 分组 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 模板 | GET | `/manager/template/page` | 模板分页 |
| 模板 | GET | `/manager/template/info` | 模板详情 |
| 模板 | POST | `/manager/template/create` | 创建模板 |
| 模板 | POST | `/manager/template/update` | 修改模板 |
| 模板 | POST | `/manager/template/remove` | 删除模板 |
| 通知 | GET | `/manager/notification/page` | 通知分页 |
| 通知 | POST | `/manager/notification/sent` | 发布通知（须指定 sentToUser 或 sentToUsers） |
| 通知 | GET | `/manager/notification/info` | 通知详情 |
| 通知 | GET | `/manager/notification/record/page` | 通知的阅读记录分页 |
| 个人消息 | GET | `/personal/list` | 消息列表（默认 size=100，前端展示 99+） |
| 个人消息 | GET | `/personal/page` | 消息分页 |
| 个人消息 | GET | `/personal/info` | 消息详情（自动 showTimes+1） |
| 个人消息 | POST | `/personal/readed` | 批量标记已读（传 id 或 ids） |
| 个人设置 | GET | `/personal/settings` | 获取当前用户消息配置 |
| 个人设置 | POST | `/personal/settings/save` | 保存当前用户消息配置 |

## 核心机制

### 数据模型

| 表 | 关键字段 | 说明 |
|----|----------|------|
| `msg_template` | template_code、template_name、title、content | 消息模板（标题/正文复用） |
| `msg_notification` | notice_no（msg_ 前缀）、user_code（发送人）、title、content、ext_url（跳转链接） | 通知主表，一通知多记录 |
| `msg_user_record` | user_code、notice_no、read_status（0 未读）、read_time、show_times（展示/阅读计数） | 每用户每通知一条，联查通知取标题正文 |
| `msg_user_settings` | user_code、notify_event（事件消息配置 JSON）、notify_system（系统消息配置 JSON） | 用户维度消息偏好 |

均继承 BaseEntity 基础字段（id/sort/create_*/update_*/remark/version/deleted）。

### 关键行为

- **发布**：REST `/manager/notification/sent` 与 API `MsgApi.sentMsg` 均收敛到 `MsgNotificationService.createNotification`；目标用户取 `sentToUser` + `sentToUsers` 去重合并
- **阅读计数**：`/personal/info` 查询详情时 `updateShowTimes` 自增（阅读 +1），已读状态由 `/personal/readed` 批量更新（写 read_time）
- **用户隔离**：个人端查询/已读均以 `IdentityContext.getUserCode()` 过滤，不可操作他人消息
- **用户设置**：`getUserSettings` / `setUserSettings` 按当前用户读写，配置项为 JSON 字符串

## 依赖关系

- **sh-redis**：RedisIdGenerator（通知单号生成）
- **sh-core / sh-mybatis / sh-tool / sh-web**（传递）：IdentityContext、BaseService/PageQuery、BeanUtil、R
- **被依赖**：工程内暂无其他 micro-* 模块依赖本模块；宿主业务系统注入 `MsgApi` 发送站内消息（单用户/多用户/带跳转链接）

## 开发注意事项

1. 模板当前仅做数据维护，`createNotification` 不做模板渲染（title/content 由调用方传入）
2. 发布通知必须至少指定一个目标用户，否则 REST 返回错误
3. 用户记录为发布时快照式批量插入（`insertBatch`），删除通知不会级联删除用户记录
4. `showTimes` 在详情查询时自增，列表/分页查询不计数
5. 个人消息列表默认 size=100，配合前端"99+"展示
6. 异常处理统一使用 `ValidationException.of(...)`，REST 返回 `R<T>`

---

**最后更新时间**: 2026-11-28
