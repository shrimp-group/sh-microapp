# micro-wxmp 模块开发指南

## 模块概述

micro-wxmp 是 sh-microapp 微应用集合中的微信公众号模块，基于 weixin-java-mp（WxJava）封装多租户（按 appid）公众号能力：消息回调与事件路由、OAuth2 网页授权登录（对接 iam-session 会话）、JSAPI 签名、素材列表与菜单管理、客服消息记录与用户管理。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-wxmp`
- **API 前缀**: `/micro-wxmp`
- **数据表**: `wxmp_config`（公众号配置）、`wxmp_user`（微信用户）、`wxmp_kf_msg`（客服消息）、`wxmp_login_log`（登录日志）

## 架构设计

```
微信服务器回调       WxPortalRest（GET 验签 / POST 消息路由，明文与 AES 加密均支持）
H5 端              WxmpLoginRest（OAuth2 登录）  WxUserRest（我的信息）  WxSignRest（JSAPI 签名）
管理端              WxmpConfigRest（配置 CRUD）  WxmpKfMsgRest（客服消息分页/详情）
                   WxMaterialRest（素材列表 / 菜单设置与删除）
    ↓
Configuration   WxMpConfiguration（按 appid 缓存 WxMpService + WxMpMessageRouter 规则链）
Handler         Log/Msg/Menu/Subscribe/Unsubscribe/Location/Scan/KfSession/StoreCheckNotify/Null
                （各留 LogSpi/MsgSpi/MenuSpi/SubscribeSpi 可选扩展点）
Service         WxmpLoginService（OAuth2 + SessionManager）  WxMaterialService  WxmpUserService 等
    ↓
外部            weixin-java-mp / micro-fileos（头像签名）/ iam-session（会话）
```

| 组件 | 职责 |
|------|------|
| `WxMpConfiguration` | 按 appid 缓存 `WxMpService`（读 `wxmp_config`，懒加载 + synchronized）；`messageRouter` 构建事件规则链 |
| `WxmpLoginService` | `getOAuth2Service().getAccessToken(code)` + `getUserInfo` → `WxmpUserService.initUser` 初始化/更新用户 → `SessionManager.createSession(identity, AuthType.WECHAT_MP)` → 写 `wxmp_login_log` |
| `WxMaterialService` | 永久素材列表（image 类型 `materialFileBatchGet`）；菜单设置取 `wxmp_config.mp_menu_json` 预设 JSON 调 `menuCreate`（未配置抛异常）；`menuDelete` 删除菜单 |
| Builder | `AbstractBuilder` + `TextBuilder` / `ImageBuilder`，组装被动回复 XML 消息 |

## 目录结构

```
micro-wxmp/src/main/java/com/wkclz/micro/wxmp/
├── Route.java                         # 路由常量（@Router）
├── WxmpAutoConfig.java                # 自动配置（@ComponentScan + @MapperScan）
├── bean/
│   ├── dto/
│   │   └── WxMpAppInfo.java           # 公众号配置 VO
│   ├── entity/                        # WxmpConfig / WxmpUser / WxmpKfMsg / WxmpLoginLog
│   ├── req/                           # 配置/消息/素材/签名 Req
│   └── resp/                          # 配置/消息/登录/用户 Resp
├── builder/                           # AbstractBuilder / TextBuilder / ImageBuilder
├── config/
│   └── WxMpConfiguration.java         # 多租户服务与消息路由
├── handler/
│   ├── AbstractHandler.java           # WxMpMessageHandler 基类（统一 Logger）
│   ├── LogHandler / MsgHandler / MenuHandler / SubscribeHandler / UnsubscribeHandler
│   ├── LocationHandler / ScanHandler / KfSessionHandler / StoreCheckNotifyHandler / NullHandler
│   └── spi/                           # LogSpi / MsgSpi / MenuSpi / SubscribeSpi 扩展点
├── mapper/                            # WxmpConfigMapper / WxmpUserMapper / WxmpKfMsgMapper / WxmpLoginLogMapper
├── rest/
│   ├── manager/                       # 管理端：WxmpConfigRest / WxmpKfMsgRest
│   ├── mp/                            # 回调与平台：WxPortalRest / WxSignRest / WxMaterialRest
│   └── cust/                          # H5 客户端：WxmpLoginRest / WxUserRest
└── service/                           # WxmpConfig / WxmpUser / WxmpKfMsg / WxmpLoginLog / WxMaterial / WxmpLogin
```

## API 端点清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/micro-wxmp/public/portal/{appid}` | 服务器验签（返回 echostr，豁免 Req/Resp） |
| POST | `/micro-wxmp/public/portal/{appid}` | 消息回调：验签后解析 XML（明文/AES），经 `WxMpMessageRouter` 分发，返回被动回复 |
| GET | `/micro-wxmp/public/login/{appid}` | OAuth2 网页授权登录（code 换 token + 用户信息，返回 iam-session token） |
| GET | `/micro-wxmp/h5/mine/userinfo` | 当前登录用户基本信息（头像经 `FileosSignApi.sign` 签名） |
| GET | `/micro-wxmp/h5/wx/sign` | JSAPI 签名（`createJsapiSignature(url)`） |
| GET | `/micro-wxmp/material/batchget_material/{appid}` | 永久素材列表（image 类型，offset/size 分页） |
| POST | `/micro-wxmp/menu/update/{appid}` | 按 `wxmp_config.mp_menu_json` 设置菜单 |
| POST | `/micro-wxmp/menu/delete/{appid}` | 删除菜单 |
| GET | `/micro-wxmp/config/page` 等 5 个 | 公众号配置 CRUD（page/info/create/update/remove） |
| GET | `/micro-wxmp/kf/msg/page`、`/kf/msg/info` | 客服消息分页 / 详情 |

## 核心机制：消息路由

`WxPortalRest` POST 回调 → `WxMpConfiguration.messageRouter(appid)` 规则链（按注册顺序）：

| 规则 | Handler | 说明 |
|------|---------|------|
| 全部事件（异步） | `LogHandler` | 记录日志；可注入 `LogSpi` 扩展 |
| 客服会话事件（进入/转接/关闭） | `KfSessionHandler` | 客服会话管理 |
| 门店审核事件 | `StoreCheckNotifyHandler` | POI 审核通知 |
| 菜单 CLICK | `MenuHandler` | 可注入 `MenuSpi` 扩展 |
| 菜单 VIEW | `NullHandler` | 跳转链接，空处理 |
| 关注 / 取消关注 | `SubscribeHandler` / `UnsubscribeHandler` | `WxmpUserService.initUser` 初始化/更新 `subscribeStatus` |
| 地理位置事件与消息 | `LocationHandler` | 上报地理位置 |
| 扫码事件 | `ScanHandler` | SCAN 事件 |
| 默认 | `MsgHandler` | 非 EVENT 消息落库 `wxmp_kf_msg`；无 `MsgSpi` 时以 JSON 文本回复，有则执行 `autoReply` |

SPI 扩展：`LogSpi` / `MsgSpi` / `MenuSpi` / `SubscribeSpi` 经 `@Autowired(required = false)` 注入，宿主应用实现 Bean 即接管对应逻辑；未提供时走内置默认行为，SPI 抛异常仅记 error 不中断。

## 数据库表

| 表 | 关键字段 | 说明 |
|----|----------|------|
| `wxmp_config` | tenantCode / appId / appSecret / certPem / keyPem / mpToken / aesKey / mpMenuJson / mpTalkReplyMap / welcomeMsg | 公众号配置（token/aesKey 用于验签解密，mpMenuJson 用于菜单设置） |
| `wxmp_user` | userCode / appId / openId / unionId / subscribeStatus / nickname / gender / avatar / country / province / city / privilegeList / mobile / email / loginTimes | 公众号用户 |
| `wxmp_kf_msg` | tenantCode / appId / msgType / fromUser / toUser / content / msgId / msgTime | 客服消息记录 |
| `wxmp_login_log` | userCode / openId / loginIp | 登录日志 |

## 依赖

- `weixin-java-mp`（WxJava）：公众号 SDK，版本由 sh-bom 管理
- `micro-fileos`：`FileosSignApi` 对头像 URL 签名
- `iam-session`：`SessionManager` 创建会话（`AuthType.WECHAT_MP`）、`LoginResp`
- `sh-redis`：基础缓存能力

## 注意事项

1. 回调与登录为免登录路径（`/public/**`），需在网关/权限层放行；验签/回调端点按微信协议返回明文或 XML，豁免 Req/Resp 封装
2. `WxMpService` 按 appid 懒加载缓存、进程内常驻不自动刷新——修改 `wxmp_config`（token/aesKey/appSecret）后需重启实例生效
3. 菜单内容来自配置字段 `mp_menu_json`，为空时"菜单-设置"抛"未配置预设菜单!"
4. 自定义业务扩展优先实现 `handler/spi` 四个 SPI 接口，勿直接改动内置 handler
5. `MsgHandler` 对客服消息落库失败仅记录 error 日志，不阻断消息回复

**最后更新时间**: 2026-11-28
