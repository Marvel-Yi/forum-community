# 论坛社区项目后端
## 0 项目简介
## 1 功能描述
### 1.1 首页展示
GET: `/index?current&limit`

| 参数 | 含义        |
|---|-----------|
| current | 当前页码      |
| limit | 每页可展示的帖子数 |
- 首页分页展示文章帖子
### 1.2 用户注册
POST: `/register`

| 参数   | 含义        |
|------|-----------|
| user | 用户实体对象      |
- 用户提交信息，服务端验证后落库并发送激活邮件
### 1.3 用户激活
GET: `/activation/{userId}/{activationCode}`

| 参数     | 含义        |
|--------|-----------|
| userId | 用户id      |
| activationCode  | 用户激活码 |
- 验证用户激活码，通过后更新数据库用户状态完成激活
### 1.4 用户登录
POST: `/login`

| 参数     | 含义                              |
|--------|---------------------------------|
| userName | 用户名                             |
| password  | 密码                              |
 |rememberMe | 是否记住当前账号，分别对应不同的登录凭证ticket的过期时间 |
- 验证用户名、激活状态、密码
- 生成登录凭证ticket，以cookie形式存储于浏览器，并设置过期时间
- 使用拦截器Interceptor处理请求，验证浏览器携带的cookie中的登录凭证ticket，若ticket存在于数据库中并且状态为已登录并且未过期，则说明用户处于登录状态，于是根据LoginTicket查询用户信息，使用ThreadLocal，线程私有地持有此用户信息，以便后续使用，并在模版渲染之前取出ThreadLocal用户信息用于展示，完成后及时清理ThreadLocal对象
### 1.5 用户退出
GET: `/logout`

| 参数     | 含义                |
|--------|-------------------|
| ticket | 从cookie获取的登录凭证标识码 |
- 获取浏览器携带的cookie头部中的登录凭证ticket信息，将凭证中的登录状态设为失效
### 1.6 检查登录状态
- 自定义注解标记在需要登录才可访问的方法，每次调用前使用拦截器LoginRequiredInterceptor检查是否需要登录，以及用户是否已登录，验证失败则重定向至首页展示帖子
- 每次请求到达进入controller之前，使用拦截器检查浏览器发送请求携带的cookie头部中的登录凭证ticket，具体来说，会按照配置类注册拦截器的顺序便利店拦截器，依次验证prehandler方法的boolean返回值
- 由于LoginTicketInterceptor在LoginRequiredInterceptor之前注册，因此拦截器得到请求后，LoginTicketInterceptor首先验证ticket登录的有效性，若已登陆则线程私有地持有用户信息，以便后续使用和渲染展示用户信息。随后LoginRequiredInterceptor使用反射机制查看请求方法上是否带有需要登录的标记注解，若需要登录则继续检查是否已线程私有地持有了用户信息，若没有则表示未登录，请求失败，重定向到首页
### 1.7 账户设置
#### 修改密码
POST: `/user/modify/password`

| 参数     | 含义      |
|--------|---------|
| originPassword | 原密码     |
| newPassword  | 新密码     |
|repeatPassword | 二次确认新密码 |
- 验证旧密码，二次确认新密码后完成修改
- 拦截器通过浏览器携带的cookie查验登录凭证，确认登录后，线程私有地持有用户信息，从而进行旧密码的验证
### 1.8 过滤敏感词
- 使用单词查找树Trie完成过滤，且不受敏感词中间穿插的特殊符号的干扰
## 2 实体
### 2.1 User 用户
```java
public class User {
 private int id;
 private String userName;
 private String password;
 private String email;
 private int userType; // 0 ordinary user，1 owner of section, 2 administrator
 private int status; // 0 not activated, 1 activated
 private String activationCode;
 private Date createTime;
}
```
### 2.2 LoginTicket 登录凭证
```java
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status; // 0 means login, 1 means logout
    private Date expired;
}
```
### 2.3 Post 帖子文章
```java
public class Post {
    private int id;
    private int userId;
    private String title;
    private String content;
    private int postType; // 0 ordinary post, 1 top post
    private int postStatus; // 0 normal, 1 essential, 2 off the shelf
    private Date createTime;
    private int commentCount;
    private double score; // degree of popularity
}
```
## 3 数据库
### 3.1 user
```
`id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `email` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `user_type` int NOT NULL COMMENT '用户类型，0普通用户，1版主，2管理员',
  `status` tinyint NOT NULL COMMENT '状态，0未激活，1激活',
  `activation_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```
### 3.2 login_ticket
```
CREATE TABLE `login_ticket` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `ticket` varchar(45) NOT NULL DEFAULT '',
  `status` int NOT NULL COMMENT '登录状态，0有效，1无效',
  `expired` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```
### 3.3 post
```
`user_id` int NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `content` text,
  `post_type` int NOT NULL COMMENT '帖子类型，0普通，1置顶',
  `post_status` int NOT NULL COMMENT '帖子状态，0正常，1精华，2下架',
  `create_time` datetime NOT NULL,
  `comment_count` int DEFAULT NULL COMMENT '评论数，冗余字段，防止频繁关联查询',
  `score` double DEFAULT NULL COMMENT '帖子热度，用于排名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

## application.properties 配置项
