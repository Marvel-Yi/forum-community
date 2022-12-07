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
### 1.4 用户退出

## 2 数据库表
