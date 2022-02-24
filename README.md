[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Website-gateway主要能力

Website-gateway用来封装EdgeGallery各业务平台的前台服务，主要提供如下几方面能力：
- 1 实现后台接口的隔离，客户端的访问请求通过website-gateway的zuul网关转发到后台服务
- 2 实现单点登录的客户端服务
- 3 支持北向接口请求转发
- 4 支持会话失效向客户端推送通知

## How to start

- 1 编译业务前台代码，将dist目录中的内容复制到Website-gateway的/src/main/resource/static中

- 2 本地启动时需配置如下环境变量：

    - **SC_ADDRESS**：连接SC的地址。本地运行的SC默认为：http://127.0.0.1:30100
    - **CLIENT_ID**: 待启动前台服务的ID。需要与User Management服务中配置的oauth2.clients.clientId保持一致
    - **CLIENT_SECRET**: 待启动前台服务的密钥。需要与User Management服务中配置的oauth2.clients.clientSecret保持一致
    - **AUTH_SERVER_ADDRESS**: User Management服务的URL，如http://x.x.x.x:8067
    - **AUTH_SERVER_ADDRESS_CLIENTACCESS**: 该配置是为代理访问模式定义的变量。正常访问模式下，与AUTH_SERVER_ADDRESS保持一致即可
    - **COOKIE_NAME**：为对应业务前台定义的SESSION ID，例如AppStore的SESSIONID名称为APPSTORESESSIONID。也可以不配置，使用默认的JSESSIONID

- 3 本地启动：

    运行/src/main/java/org/edgegallery/website/GatewayApplication.java文件中的main函数就能启动对应的业务前台。

## Use RateLimit-zuul to limit API

```yaml
zuul:
  routes:
    user-mgmt-be: /mec-usermgmt/**
    mec-developer: /mec-developer/**
    mec-appstore: /mec-appstore/**
    mec-atp: /mec-atp/**
    mec-lab: /mec-lab/**
    mecm-inventory: /mecm-inventory/**
    mecm-appo: /mecm-appo/**
    mecm-apm: /mecm-apm/**
    mecm-north: /mecm-north/**
    mec-thirdsystem: /mec-thirdsystem/**
  sensitive-headers:
  ratelimit:
    enabled: true
    behind-proxy: false
    repository: JPA
    default-policy:
      limit: 10  #optional - request number limit per refresh interval window
      quota: 1000  #optional - request time limit per refresh interval window (in seconds)
      refresh-interval: 30 #default value is 60 (in seconds)
      type:
        - USER
        - URL
        - ORIGIN
```
Response when has error:
```json
{
    "timestamp": "2021-08-04T01:40:54.123+0000",
    "status": 429,
    "error": "Too Many Requests",
    "message": ""
}
```
规则说明：
  - 30秒内某个链接超过10次被访问，下次请求被拦截，并返回429错误
  - 链接唯一性通过：username/访问端IP/访问API确定，例如：rate-limit-application:mec-developer:test123:/mec/developer/v1/projects/:127.0.0.1
    > 1. rate-limit-application: 固定前缀
    > 2. mec-developer: serviceId，可以是zuul.routes中定义其他服务
    > 3. test123: 访问链接的用户名
    > 4. /mec/developer/v1/projects/：被访问的链接地址
    > 5. 127.0.0.1：访问来源IP，从request heaser的"X-Forwarded-For"中获取
  - 使用内存数据库保存中间数据
  - 更详细配置请参考：[spring-cloud-zuul-ratelimit:2.1.0.REALSE](https://github.com/marcosbarbero/spring-cloud-zuul-ratelimit/tree/v2.1.0.RELEASE)
  