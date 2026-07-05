# Story: 公众号OAuth与用户

## 描述
作为公众号用户，通过OAuth2授权登录H5页面，在微信内使用业务功能。

## 参与者
| 角色 | 说明 |
|------|------|
| 公众号用户 | OAuth2授权登录 |
| 微信服务器 | 提供OAuth2授权 |
| 系统 | 后端服务，处理OAuth2和用户管理 |

## 流程图
```mermaid
sequenceDiagram
    participant 用户
    participant H5页面
    participant 后端
    participant 微信服务器

    用户->>H5页面: 在微信内打开H5
    H5页面->>后端: 请求OAuth2登录
    后端->>微信服务器: 跳转微信授权页
    微信服务器-->>用户: 显示授权页
    用户->>微信服务器: 同意授权
    微信服务器-->>后端: 回调携带code
    后端->>微信服务器: code换access_token+openid
    微信服务器-->>后端: 返回用户信息
    后端->>后端: 创建/更新WxmpUser
    后端-->>H5页面: 登录成功

    H5页面->>后端: 请求JSAPI签名
    后端-->>H5页面: 返回jsapi签名供前端调用微信JS-SDK

    Note over 后端: 用户取关公众号时UnsubscribeHandler标记用户为取关状态
```

## 验收标准
- [ ] OAuth2登录：Given 用户在微信内打开H5, When 请求OAuth2登录, Then 跳转微信授权页
- [ ] 回调处理：Given 用户同意授权, When 回调到达, Then 获取openid和用户信息并创建/更新WxmpUser
- [ ] JSAPI签名：Given H5页面需要JSAPI, When 请求签名, Then 返回jsapi签名供前端调用微信JS-SDK
- [ ] 取关处理：Given 用户取关公众号, When UnsubscribeHandler处理, Then 标记用户为取关状态

## 关联模块
- micro-wxmp

## 关联 API
- `/micro-wxmp/oauth2/authorize`
- `/micro-wxmp/oauth2/callback`
- `/micro-wxmp/jsapi/signature`

## 优先级
P1

## 状态
待开发
