# 论坛社区项目后端
## 1 功能描述
### 1.1 首页展示
GET: `/index?current&limit`： 
- 首页分页展示文章帖子
- current，当前页码
- limit，每页可展示的帖子数
### 1.2 用户注册
POST: `/register`
- 用户提交信息，服务端验证后入库并发送激活邮件

GET: `/activation/{userId}/{activationCode}`
- 用户点击激活邮件，服务端激活用户状态
- userId，用户id
- activationCode，用户激活码
### 1.3 用户登陆
POST: `/login`
- 验证用户名、激活状态、密码
- 生成登陆凭证ticket，以cookie形式存储于浏览器，并设置过期时间
- 使用拦截器Interceptor处理请求，验证浏览器携带的cookie中的登陆凭证ticket，若ticket存在于数据库中并且状态为已登陆并且为过期，则说明用户处于登陆状态，于是根据LoginTicket查询用户信息，使用ThreadLocal线程私有地持有该用户信息，后续在模版渲染之前取出ThreadLocal用户信息用于展示，完成后及时清理
### 1.4 用户退出
GET: `/logout`
- 获取浏览器携带cookie头部中的登陆凭证ticket信息，将凭证中的登陆状态设为失效
## 2 数据库表
