# micro-wxapp 模块开发指南

## 模块概述

micro-wxapp 是 sh-microapp 微应用集合中的微信小程序模块，基于 weixin-java-miniapp（WxJava）封装多租户小程序能力：jscode2session 登录（对接 iam-session 会话）、用户信息管理、手机号绑定、临时素材上传下载、服务器验签与客服消息路由。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-wxapp`
- **API 前缀**: `/micro-wxapp`
- **数据表**: `wxapp_config`（小程序配置）、`wxapp_user`（小程序用户）、`wxapp_login_log`（登录日志）

## 架构设计

```
小程序端            WxMaUserRest（登录/用户信息/手机号）  WxMaMediaRest（素材）
微信服务器回调       WxAppRest（/public/miniapp/portal 验签）
管理端              WxappConfigRest（配置 CRUD）+ 用户分页
    ↓
Configuration   WxMaConfiguration（多租户 WxMaService / WxMaMessageRouter 缓存）
Service         WxMiniappService（登录/用户核心）  WxappLoginService（SessionManager 建会话）
                WxappConfigService / WxappUserService / WxappLoginLogService
    ↓
外部            weixin-java-miniapp / micro-fileos（头像签名）/ iam-session（会话）
```

| 组件 | 职责 |
|------|------|
| `WxMaConfiguration` | 按 tenantCode 缓存 `WxMaService`、按 appid 反查租户；懒加载初始化（读 `wxapp_config`）；构建 `WxMaMessageRouter` |
| `WxMiniappService` | code2session、可选解密用户信息、新用户注册（并发 DuplicateKey 兜底）、信息更新、手机号绑定、脱敏响应 |
| `WxappLoginService` | 组装 `UserIdentity`，经 iam-session `SessionManager.createSession(identity, AuthType.WECHAT_MINI)` 返回 token |
| 消息路由（demo） | 默认 logHandler 回显消息；按内容匹配"模板"（已废弃，TODO 占位）、"文本"、"图片"（发送 tmp.png 示例）、"二维码"（createQrcode 回发）四个演示 handler |

## 目录结构

```
micro-wxapp/src/main/java/com/wkclz/micro/wxapp/
├── Route.java                         # 路由常量（@Router，模块根包）
├── MicroAppAutoConfig.java            # 自动配置（@ComponentScan + @MapperScan）
├── bean/
│   ├── entity/                        # WxappConfig / WxappUser / WxappLoginLog
│   ├── req/                           # 登录/手机号/用户信息 Req + 分页 Req
│   │   └── validation/                # WxMaLoginFields / WxMaUserInfoUpdate 自定义校验器
│   ├── resp/                          # WxMaUserInfoResp / WxappUserPageResp / WxappConfigResp 等
│   └── vo/
│       └── WxMaAppInfo.java           # 小程序配置 VO（appId/appSecret/token/aesKey）
├── config/
│   └── WxMaConfiguration.java         # 多租户服务与消息路由
├── helper/
│   └── Checker.java                   # 参数校验辅助
├── mapper/                            # WxappConfigMapper / WxappUserMapper / WxappLoginLogMapper
├── rest/
│   ├── WxAppRest.java                 # 服务器验签（豁免 Req/Resp，返回 echostr）
│   ├── WxMaUserRest.java              # 登录与用户 REST
│   ├── WxMaMediaRest.java             # 临时素材 REST
│   └── WxappConfigRest.java           # 配置管理 REST
└── service/
    ├── WxappConfigService.java        # getWxMaAppInfo（配置查询）
    ├── WxappUserService.java          # 用户分页
    ├── WxappLoginLogService.java      # 登录日志
    └── custom/
        ├── WxMiniappService.java      # 登录/用户核心逻辑
        └── WxappLoginService.java     # SessionManager 会话创建
```

## API 端点清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/micro-wxapp/public/miniapp/login` | 小程序登录（jscode2session + 可选加密用户信息，返回 iam-session token） |
| GET | `/micro-wxapp/miniapp/userinfo` | 当前用户信息（手机号脱敏、头像签名） |
| POST | `/micro-wxapp/miniapp/userinfo/update` | 更新昵称/头像 |
| POST | `/micro-wxapp/miniapp/mobile/bind` | 绑定手机号（getPhoneNumber + 格式校验） |
| GET | `/micro-wxapp/customer/wx_user/phone` | 按 sessionKey 解密获取手机号 |
| GET | `/micro-wxapp/user/page` | 小程序用户分页（管理端，强制当前租户） |
| GET | `/micro-wxapp/public/miniapp/portal` | 微信服务器验签（GET echostr，豁免 Req/Resp） |
| POST | `/micro-wxapp/customer/wx/media/upload` | 上传临时素材（image 类型，返回 mediaId 列表） |
| GET | `/micro-wxapp/customer/wx/media/download/{mediaId}` | 下载临时素材（返回 File） |
| GET | `/micro-wxapp/config/page` 等 5 个 | 小程序配置 CRUD（page/info/create/update/remove） |

> Route 中另有 `PERSONAL_WECHAT_BIND` / `PERSONAL_WECHAT_UNBIND` 两个常量，当前无对应 Rest 实现，属预留定义。

## 核心机制：登录流程

`WxMiniappService.miniappLogin`（`@Transactional`）：

1. `wxService.getUserService().getSessionInfo(code)` 换取 openid / unionid / sessionKey
2. 请求携带 `encryptedData + iv + rawData + signature` 时：`checkUserInfo` 校验后 `getUserInfo` 解密昵称/头像/性别
3. 按 openid 查 `wxapp_user`：新用户插入（userCode 取昵称，缺失时 `redisIdGenerator.generateIdWithPrefix("wxapp_")`；`DuplicateKeyException` 并发时重查兜底）；老用户仅在解密信息变化时增量更新
4. 写登录日志 `wxapp_login_log`（userCode / openId / origin IP）
5. `WxappLoginService.login` → `SessionManager.createSession`（`AuthType.WECHAT_MINI`）→ 返回 `LoginResp`（token 等）

## 数据库表

| 表 | 关键字段 | 说明 |
|----|----------|------|
| `wxapp_config` | tenantCode / appId / appSecret / certPem / keyPem / appToken / aesKey | 小程序配置，按租户隔离 |
| `wxapp_user` | userCode / nickname / appId / openId / unionId / mobile / email / gender / avatar / loginTimes | 小程序用户，openid 唯一 |
| `wxapp_login_log` | userCode / openId / loginIp | 登录日志 |

## 依赖

- `weixin-java-miniapp`（WxJava）：小程序 SDK，版本由 sh-bom 管理
- `micro-fileos`：`FileosSignApi` 对头像 URL 签名
- `iam-session`：`SessionManager` 创建会话、`LoginResp`
- `sh-redis`：RedisIdGenerator（userCode 生成）

## 注意事项

1. 多租户路由：`WxMaConfiguration.getMaService()` 按当前请求租户（`IdentityContext.getTenantCode()`）定位服务，`wxapp_config` 缺配置时抛"没有找到小程序配置"；`getMaService(appid)` 按 appid 反查
2. 服务实例首次访问时懒加载初始化（`synchronized`），进程内常驻不自动刷新——修改 `wxapp_config` 后需重启实例生效
3. `/public/miniapp/login` 与 `/public/miniapp/portal` 为免登录路径，需在网关/权限层放行
4. `WxMaConfiguration` 内的 log/text/pic/qrcode 消息 handler 为 WxJava 官方 demo 保留现状，仅用于联调验证消息通道；生产业务请自行扩展 Router 规则
5. 用户信息响应中手机号经正则 `(?<=^.{3}).{4}` 脱敏为 `****`，头像经 `FileosSignApi.sign` 签名
6. 小程序"获取手机号"有两个入口：登录态下的 `mobile/bind`（getPhoneNumber）与携带 sessionKey 的 `customer/wx_user/phone`（getPhoneNoInfo），按场景选用

**最后更新时间**: 2026-11-28
