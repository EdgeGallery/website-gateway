[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Website-gateway

Website-gateway用来封装EdgeGallery其他模块的前台服务，主要目的有两个：
- 1 实现后台接口的隔离，客户端的访问请求通过website-gateway的zuul网关转发到后台服务
- 2 实现单点登录的客户端服务

## How to start

- 1 编译前台代码，将dist目录中的内容复制到/resource/static中

- 2 配置单点登录：
```yaml
security:
  oauth2:
    sso:
      login-path: /login
    client:
      client-id: ${CLIENT_ID:}
      client-secret: ${CLIENT_SECRET:}
      user-authorization-uri: ${AUTH_SERVER_ADDRESS:}/oauth/authorize
      access-token-uri: ${AUTH_SERVER_ADDRESS:}/oauth/token
    resource:
      jwt:
        key-uri: ${AUTH_SERVER_ADDRESS:}/oauth/token_key
```
    - CLIENT_ID: 待启动前台服务的ID
    - CLIENT_SECRET: 待启动前台服务的密钥，这两个参数是用来注册到user-mgmt服务，用于单点登录的服务注册
    - AUTH_SERVER_ADDRESS: user-mgmt服务的URL  
CLIENT_ID 和 CLIENT_SECRET 需要和user-mgmt服务中配置的oauth2.clients.clientId 和 oauth2.clients.clientSecret一样，不然启动会报错

## Use RateLimit-zuul to limit API

```yaml
zuul:
  routes:
    mec-developer: /mec-developer/**
    mec-appstore: /mec-appstore/**
    mec-atp: /mec-atp/**
    mec-lab: /mec-lab/**
    mecm-inventory: /mecm-inventory/**
    mecm-appo: /mecm-appo/**
    mecm-apm: /mecm-apm/**
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
    > 5. 127.0.0.1：访问来源IP
  - 使用内存数据库保存中间数据
  - 更详细配置请参考：[spring-cloud-zuul-ratelimit:2.1.0.REALSE](https://github.com/marcosbarbero/spring-cloud-zuul-ratelimit/tree/v2.1.0.RELEASE)
  