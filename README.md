# 论坛社区项目后端
## 0 项目简介
## 1 功能描述
### 1.1 首页展示
GET: `/index?current&limit`

| 参数 | 含义          |
|---|-------------|
| current | 当前页码        |
| limit | 每页可展示的帖子文章数 |
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
- 使用拦截器Interceptor处理请求，验证浏览器携带的cookie中的登录凭证ticket，若ticket存在于数据库中并且状态为已登录并且未过期，则说明用户处于登录状态，于是根据LoginTicket查询用户信息，使用ThreadLocal，线程私有地持有此用户信息，以便后续使用，并在模版渲染之前取出ThreadLocal用户信息用于展示，完成后在afterCompletion方法内及时清理ThreadLocal对象
### 1.5 用户退出
GET: `/logout`

| 参数     | 含义                |
|--------|-------------------|
| ticket | 从cookie获取的登录凭证标识码 |
- 获取浏览器携带的cookie头部中的登录凭证ticket信息，将凭证中的登录状态设为失效
### 1.6 检查登录状态
- 自定义注解，标记在需要登录才可访问的方法上，每次调用前使用拦截器LoginRequiredInterceptor检查是否需要登录，以及用户是否已登录，验证失败则重定向至首页展示帖子
- 每次请求到达进入controller之前，使用拦截器检查浏览器发送的请求携带的cookie头部中的登录凭证ticket，具体来说，会按照配置类注册拦截器的顺序遍历拦截器，依次验证prehandler方法的boolean返回值
- 由于LoginTicketInterceptor在LoginRequiredInterceptor之前注册，因此拦截器得到请求后，LoginTicketInterceptor首先验证ticket登录的有效性，若已登陆则线程私有地持有用户信息，以便后续使用和渲染展示用户信息。随后LoginRequiredInterceptor使用反射机制查看请求方法上是否带有需要登录的标记注解，若需要登录，则继续检查是否已线程私有地持有了用户信息，若没有则表示未登录，请求失败，重定向到首页
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
### 1.9 发布帖子
POST: `/post/publish`

| 参数      | 含义   |
|---------|------|
| title   | 帖子标题 |
| content | 帖子内容 |
- 拦截器先根据浏览器发送的cookie头部验证请求所需要的登录状态，用户输入帖子，敏感词过滤，入库
### 1.10 查看帖子
GET: `/post/detail/{postId}/?current&limit`

| 参数      | 含义          |
|---------|-------------|
| postId  | 帖子id        |
 | current | 当前页码        | 
 | limit   | 每页可展示的回帖评论数 |
- 查看帖子详情
- 分页展示对此帖子的评论
- 每条评论下再展示对此评论的所有回复，这些回复有的是直接回复评论，有的是回复评论下其他用户的回复
### 1.11 评论
POST: `/comment/publish/{postId}`

| 参数      | 含义     |
|---------|--------|
| postId  | 帖子id   |
| comment | 实体对象评论 |
- 通过指定参数，选择评论的对象为帖子，或帖子下的评论，或评论下其他用户的回复，评论发布后重定向到对应帖子的详情页
- 通过@Transactional注解利用事务的ACID特性，评论发布后更新comment表，紧接着将comment表中对应帖子的评论数同步到post表comment_count字段中
- 需要通过cookie验证登录状态
### 1.12 私信列表
GET: `/message/list`

| 参数      | 含义                        |
|---------|---------------------------|
| current | 私信列表当前页码                  |
| limit   | 每页可展示的私信会话数（一个对话目标对应一个会话） |
- 分页展示私信会话列表，列表中每个会话只展示与对话目标最新的一条消息
- 展示总的未读消息数，展示每个会话的未读消息数和总消息数，展示每个对话目标的用户信息
- 使用group by按会话分组查询每组会话的最新消息，即最大id（id按时间升序），展示时按id降序，即按时间由新到旧
### 1.13 私信详情
GET: `/message/detail/{conversationId}`

| 参数             | 含义           |
|----------------|--------------|
| conversationId | 会话id         |
| current        | 会话内当前页码      | 
| limit          | 会话内每页可展示的消息数 |
- 分页展示每个会话内的所有消息，展示消息发送者，会话对象
- 展示时按id降序，即按时间由新到旧
- 获取到私信详情中的消息后，对于其中的未读消息，使用MyBatis的for each标签批量更新为已读状态
### 1.14 发送私信
POST: `/message/send`

| 参数      | 含义      |
|---------|---------|
| toName  | 私信目标用户名 |
| content | 私信消息内容  | 
- 发送私信到目标用户，私信入库前使用前缀树做敏感词过滤
### 1.15 AOP
- 使用AOP统一记录service层的业务日志
#### 为什么要使用AOP：
***日志记录***、***事务管理***、***权限校验***等功能是项目中多个业务组件共同使用的系统需求，如果没有aop，而是把这些功能封装到一个系统组件中，那么多个业务组件都会调用该系统组件，这样做的缺点一个是会产生许多冗余重复的方法调用，另一个是造成业务代码和系统代码耦合，使得业务逻辑与系统逻辑无法完全分离。这样的耦合形成后，将来若是想修改系统逻辑会非常麻烦（比如将调用方法后打印日志改为抛出异常后打印日志）。AOP，面向切面编程，是OOP思想的一种补充，利用声明的方式，避免冗余代码并解耦，从而解决上述问题。
#### 如何使用AOP：
- 使用@Component、@Aspect注解定义切面组件，将系统功能封装至其中。
- 使用@Pointcut注解修饰方法来声明切点，即声明系统代码要织入到项目的哪些连接点joinpoint中（常用的连接点就是指业务组件中的方法）
- 定义通知advice，并声明作用在什么切点pointcut上（带@Pointcut注解的方法名）。常用的通知注解有@Before（joinpoint执行前生效），@After（joinpoint执行后生效），@Around（joinpoint执行前后生效），@AfterReturning（joinpoint返回后生效），@AfterThrowing（joinpoint抛出异常后生效）
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
### 2.4 Comment 评论
```java
public class Comment {
    private int id;
    private int userId;
    private int subjectType; // 0 means comment on post, 1 means reply to comment
    private int subjectId; // post id or comment id
    private int targetId; // id of user who is replied while commenting on the comment of the post
    private String content;
    private int status; // 0 normal, 1 deleted
    private Date createTime;
}
```
### 2.5 Message 私信
```java
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status; // 0 read, 1 unread, 2 deleted
    private Date createTime;
}
```
## 3 数据库
### 3.1 user
```mysql
CREATE TABLE `user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `email` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `user_type` int NOT NULL COMMENT '用户类型，0普通用户，1版主，2管理员',
  `status` tinyint NOT NULL COMMENT '状态，0未激活，1激活',
  `activation_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```
### 3.2 login_ticket
```mysql
CREATE TABLE `login_ticket` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `ticket` varchar(45) NOT NULL DEFAULT '',
  `status` int NOT NULL COMMENT '登陆状态，0有效，1无效',
  `expired` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```
### 3.3 post
```mysql
CREATE TABLE `post` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `content` text,
  `post_type` int NOT NULL COMMENT '帖子类型，0普通，1置顶',
  `post_status` int NOT NULL COMMENT '帖子状态，0正常，1精华，2下架',
  `create_time` datetime NOT NULL,
  `comment_count` int DEFAULT NULL COMMENT '评论数，冗余字段，防止频繁关联查询',
  `score` double DEFAULT NULL COMMENT '帖子热度，用于排名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```
### 3.4 comment
```mysql
CREATE TABLE `comment` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `subject_type` int NOT NULL COMMENT '评论的对象，1帖子（回复帖子），2评论（回复评论）',
  `subject_id` int NOT NULL COMMENT '被评论对象的id',
  `target_id` int NOT NULL COMMENT 'subject_type为2时生效，即在某个subject_type为1的评论下，要回复的用户id',
  `content` text NOT NULL,
  `status` tinyint unsigned NOT NULL COMMENT '评论状态，0正常，1被删除',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```
### 3.5 message
```mysql
CREATE TABLE `message` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `from_id` int NOT NULL,
  `to_id` int NOT NULL,
  `conversation_id` varchar(20) NOT NULL DEFAULT '' COMMENT '冗余字段，方便查询会话，由from_id和to_id升序拼接',
  `content` text NOT NULL,
  `status` int NOT NULL COMMENT '0未读，1已读，2已删除',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

## application.properties 配置项
