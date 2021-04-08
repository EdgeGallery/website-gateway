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
