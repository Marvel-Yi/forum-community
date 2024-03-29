# 论坛社区项目后端
## 0 项目简介
## 1 功能描述
### 1.1 首页展示
GET: `/index?current&limit&sortingMode`

| 参数          | 含义          |
|-------------|-------------|
| current     | 当前页码        |
| limit       | 每页可展示的帖子文章数 |
| sortingMode | 帖子排序模式      |
- 首页分页展示文章帖子
- sortingMode默认为0，按时间排序，即新帖在前，sortingMode为1时，按热度排序，即热帖在前
### 1.2 用户注册
POST: `/register`

| 参数   | 含义        |
|------|-----------|
| user | 用户实体对象      |
- 用户提交用户名密码邮箱用于注册，服务端验证后落库并发送激活邮件
### 1.3 用户激活
GET: `/activation/{userId}/{activationCode}`

| 参数     | 含义        |
|--------|-----------|
| userId | 用户id      |
| activationCode  | 用户激活码 |
- 验证用户激活码，通过后在数据库更新用户状态以完成激活
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
#### 优化
- 使用redis缓存登陆凭证和用户信息，优化查询性能
- 由于存在大量请求需要验证cookie中的登陆凭证，并通过凭证查询用户信息，分别对应两次数据库查询（ticket表和user表），性能较低，故考虑使用redis作为缓存
- 分别将登陆凭证ticket对象和用户信息user对象序列化并使用redis string存储，每次请求通过查询redis缓存验证cookie中的登陆凭证，同样地，通过凭证中的id查询redis缓存获取用户信息
- 对用户信息user对象采用**旁路缓存**的读写策略（Cache Aside Pattern），读取时首先尝试从缓存中获取，获取成功直接返回，获取不到则查询数据库，并缓存到redis中再返回。更新时，先更新数据库，然后删除缓存上的旧数据，以保证缓存和数据库的数据一致性
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
- 展示帖子、评论、回复的点赞信息
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
### 1.16 点赞
POST: `/like`

| 参数              | 含义            |
|-----------------|---------------|
| subjectType     | 点赞目标的类型（帖子、评论） |
| subjectId       | 点赞目标id        |
| subjectAuthorId | 点赞目标的作者id     |
| postId          | 点赞目标所在的帖子id   |
- 基于redis实现对帖子、评论、回复的点赞功能，并显示点赞数和是否已点赞，第二次点赞将会取消之前的点赞
- 使用set存储帖子和评论的点赞数，点赞成功后通过add把点赞用户的id存入set中，取消点赞则通过remove把id从set中移除，通过size统计点赞数，通过isMember查询是否已点赞
- 使用string存储用户收到的点赞数，点赞成功和取消点赞分别通过increment和decrement来更新点赞数
- 上述两步使用redis事务来执行，重写SessionCallback的execute方法，使用multi方法开启事务，exec提交事务。redis事务不具备原子性，只是将命令一次性提交到队列中再按序逐个执行。
### 1.17 关注
POST: `/follow`

POST: `/unfollow`

| 参数              | 含义             |
|-----------------|----------------|
| subjectType     | 关注目标的类型（帖子、用户） |
| subjectId       | 关注目标id         |
- 关注和取消关注不同类型的对象（帖子、用户）
- 使用两个zset分别维护关注列表与粉丝列表，存储关注对象的id，并将其加入有序集合的时间作为score，实现按时间先后排序
- 每个用户的关注列表根据关注对象的类型使用不同的zset，键名中使用subjectType来区分，每个对象的粉丝列表的键名中也记录自身的类型，粉丝列表存储的是用户id
- 每次关注和取消关注都要更新关注列表和粉丝列表，通过execute、multi、exec方法使用redis事务来处理
### 1.18 个人主页
GET: `/user/profile/{userId}`

| 参数             | 含义         |
|----------------|------------|
| userId | 用户id |
- 根据用户id查询个人主页，展示个人信息、获赞数、关注数、粉丝数，以及当前登陆用户是否已关注正在访问的用户
### 1.19 关注列表与粉丝列表
GET: `/follow/list/{userId}`

GET: `/fans/list/{userId}`

| 参数      | 含义               |
|---------|------------------|
| userId  | 用户id             |
| current | 用户关注或粉丝列表的当前页码   |
| limit   | 关注或粉丝列表每页可展示的用户数 |
- 分页展示指定用户的关注列表或粉丝列表，展示被关注者或粉丝的信息，关注或被关注的时间，以及当前登陆用户是否已关注列表中的用户
### 1.20 系统通知列表
GET: `/notification/list`
- 分评论、点赞、关注三个对话框显示系统通知，每个对话框显示该对话内最新的一条消息，同时显示对话的消息总数，对话的未读消息数，以及用户三类系统消息的未读消息数
- 另外，使用拦截器，对每次请求都计算出用户的未读私信数和未读系统通知数，通过modelAndView记录未读总数用于展示
- 使用Kafka存储消息，当用户做出点赞、评论、关注动作后，生产者将事件发布到对应topic下，消费者订阅topic消费消息，将点赞、评论、关注的信息作为系统私信通知到目标用户，并存储到message表中
- 当上述动作发生后，创建Event对象，设置主题，动作触发者id，目标类型和目标id，待通知用户id，生产者将封装好的事件发送到消息队列，消费者监听读取事件消息，并将消息转化为Message私信对象落库
- 根据事件Event信息设置好Message私信的发送方（即系统账号），接收方（被评论点赞关注的用户），conversation_id（事件主题），私信内容（触发者，目标类型，目标id）
### 1.21 系统通知详情
GET: `/notification/detail/{topic}`

| 参数      | 含义           |
|---------|--------------|
| topic   | 系统通知类别       |
| current | 会话内当前页码      | 
| limit   | 会话内每页可展示的通知数 |
- 分页展示给定类别的系统通知的所有消息，对于其中的未读消息，在查询后批量更新为已读
### 1.22 统计独立访客
POST: `/stat/uv`

| 参数    | 含义   |
|-------|------|
| begin | 开始日期 |
| end   | 结束日期 |
- 指定起止日期，统计独立访客数量，一个ip地址视为一个独立访客（unique visitor）
- 使用Redis HyperLogLog数据结构来统计，相比于set集合，HyperLogLog空间开销极小，但统计不精确，会有0.81%的误差
- 以日期为键，用一个HyperLogLog来统计单独一天的独立访客数，将多个HyperLogLog合并即可得到多天的独立访客数
- HyperLogLog结合伯努利实验和极大似然估算法得到关系`n=2^k_max`，再使用调和平均（减轻极大异常值的影响）、偏差修正等手段进行优化后，得到更精确的n的估算公式，在本场景下，n就是独立访客的数量。HyperLogLog会对传入的元素哈希出一个long值，本场景下传入的元素是用户ip，哈希结果的一部分位数表示轮次用于调和平均，剩下的位数则表示伯努利实验结果，第一个为1的位视为k，将每一轮的k_max代入估算公式中得到n
- 使用拦截器实现HandlerInterceptor接口的preHandle方法，每次请求前，获取用户的ip来统计uv
### 1.23 统计活跃用户
POST: `/stat/dau`

| 参数    | 含义   |
|-------|------|
| begin | 开始日期 |
| end   | 结束日期 |
- 指定起止日期，统计活跃用户，一个用户id视为一个活跃用户（active user）
- 使用Redis BitMap数据结构来统计，相比于boolean数组，位图使用一个位上的1或0来表示true或false，空间开销更小
- 以日期为键，用一个BitMap来统计单独一天的活跃用户，用userId作为BitMap的offset，每一位的1或0表示该userId代表的用户是否活跃，将多个BitMap进行OR位运算即可得到多天的活跃用户情况，通过bitCount统计1的位数，则可得到活跃用户的数量
- 位运算bitOp和bitCount需要使用RedisTemplate的execute方法，传入RedisCallback接口实现类，重写接口方法doInRedis通过RedisConnection来完成
- 使用拦截器实现HandlerInterceptor接口的preHandle方法，每次请求前，获取用户的id来统计dau
### 1.24 管理员权限校验
- 使用Interceptor拦截器进行权限校验，对于加精删帖等管理员操作，重写拦截器preHandle方法，在请求进入Controller之前判断用户类型是否为管理员，不是则拒绝请求
### 1.25 管理员设置精华帖
POST: `/post/essence`

| 参数     | 含义   |
|--------|------|
| postId | 帖子id |
- 管理员权限，将帖子的类型设置为精华帖
### 1.26 管理员删除帖子
POST: `/post/delete`

| 参数     | 含义   |
|--------|------|
| postId | 帖子id |
- 管理员权限，删除帖子，将帖子状态设为已删除
### 1.27 定期计算帖子热度
- 每2小时自动执行计算帖子热度的任务，用于首页可按热度从高到低展示帖子
- 热度与发布时间、评论数、点赞数、是否被管理员设为精华帖有关，因此，只有发新帖、发评论、点赞、加精等操作后，帖子的热度才会发生变化，把这部分帖子的id存入redis set中去重，以便定期取出计算热度，如果某个时间段比如凌晨没有需要更新热度的帖子，则记录日志提前结束任务
- 任务开始时记录日志，把redis set里需要计算热度的帖子取出，发布时间新，评论数多，点赞数多，被设为精华，都有利于热度的增加，根据公式计算热度后，把热度score落库post表，任务结束前记录日志
- 使用spring quartz框架基于线程池实现定时任务的调度，首先实现Job接口重写execute方法，方法里的逻辑是定时任务的内容，然后配置QuartzConfig配置类，通过@Bean注解和FactoryBean装配好JobDetail和SimpleJobTrigger实例，前者配置任务细节如Job类、任务名任务组、持久化等，后者配置触发器信息如JobDetail任务细节类、触发器定期触发的时间间隔RepeatInterval、触发器名触发器组等
### 1.28 多级缓存
- 使用Caffeine本地缓存+Redis分布式缓存构建多级缓存，提升首页按热度查询帖子列表的性能，有利于构建scalable（本地缓存性能高）和reliable（多级缓存多级保障）的系统
- 由于首页热帖会被频繁访问，且热帖更新的频率取决于线程池任务调度的间隔，直至下一次热帖计算任务开始前，热帖列表的排行都不会变化，即读多写少，因此很适合使用缓存加速查询，此外帖子的总数用于查询总页数，同样访问频繁，故一并放入多级缓存
- 查询时，请求首先访问一级本地缓存，若命中则直接返回，否则查询二级缓存redis，若命中则返回，并把结果写入一级缓存，否则将进一步查询数据库，并依次把结果写入二级缓存和一级缓存后返回
- 对于热帖的redis缓存，以offset和limit作为key，以便redis未命中作为参数传递到数据库查询，value存储offset和limit对应的那一页的热帖列表，由于首页访问是整存整取，故将热帖列表List整个序列化存入redis string
- 当线程池定时任务重新计算了热帖分数，热帖列表将会发生变化，当用户发布新帖，帖子总页数也会发生变化，需要保证数据库和缓存的一致性。redis缓存采用cache-aside旁路缓存策略保证一致性，即先更新数据库后删除redis缓存。一级本地缓存由于设置了较短的过期时间，首页热帖列表的需求可以容忍牺牲一定的实时性，当一级缓存过期，将会把新数据写入从而保证一致性
- 热帖计算定期执行后，根据cache-aside需要删除之前缓存到redis中的热帖，若之前用户访问了多页热帖，则redis就有多个键值对，这些key都有共同的业务前缀但有不同的offset和limit，value则是offset和limit对应的那一页的热帖集合。可以简单的使用keys命令把含有共同前缀pattern的所有热帖键找出来，但keys命令会阻塞redis主服务，当符合pattern的键数量很多时，将阻塞系统正常运行。因此采用不会阻塞的scan命令，用游标的方式分批次扫描符合pattern的key，具体通过调用redisTemplate的execute，重写接口方法使用connection和ScanOptions，设置match的pattern和每批次返回的键数目，将结果分批次扫描到Cursor，迭代Cursor结果里的键，逐个删除
- Caffeine工作原理：
  - 一方面，LRU不适合处理大量稀疏流量，比如大量只访问一次的巡检项目，将会排挤淘汰掉其他真正有用的数据项。另一方面，LFU不仅需要开销来维护和存储缓存项的访问次数，且对于曾经访问次数很高但热度已过的缓存项迟迟无法淘汰
  - Caffeine采用Window-TinyLFU算法，结合了LRU和LFU的优势，改进了LRU和LFU的缺陷
  - 首先使用Count-Min Sketch算法维护缓存项的访问次数，这个次数并非精确的，而是一个估计值，有效改进了LFU维护访问次数开销大的问题。具体来说，它会使用4个hash函数将缓存项的key映射到4个计数器，每次访问key，都将这4个计数器加一，而获取key的访问次数时，则取4个计数器中的最小值，计数器大小为4位，最大计数值为15，超过后不再增加空间占用小，因此得名TinyLFU。此外，当所有计数器的计数总和达到一定阈值后，将所有计数器的值减半，这是一种衰减机制，所以当数据热度过去后，缓存项将被衰减淘汰，这也是传统LFU无法解决的
  - 接着整个算法划分为3个区域，Window Cache、Probation Cache、Protected Cache，三者都基于LRU。当新的缓存项写入时，会进入window cache，如果window cache已满，window cache的淘汰项将会进入probation cache，如果probation cache也满了，则window cache淘汰项将会和probation cache的淘汰项一起进行淘汰机制的比较，胜出者才能留在probation cache。此外，probation cache缓存项的访问次数达到一定阈值后，会直接升级到protected cache，如果此时protected cache满了，则protected cache的淘汰项将会降级到probation cache，如果probation cache又满，则protected cache降级项和probation cache淘汰项进行淘汰机制的比较，同样，胜出者留在probation cache
  - W-TinyLFU淘汰机制为：从window cache和protected cache移出的缓存项称为candidate，而probation cache的淘汰项则称为victim，如果candidate的访问次数大于victim，则直接丢弃victim。而如果在candidate访问次数小于victim时，且candidate访问次数又小于5，则丢弃candidate，如果大于5，则在candidate和victim中随机丢弃一个
  - 综上，W-TinyLFU综合了LRU和LFU的优点，将不同特性的数据写入不同的区域，高频访问的数据写入protected受到保护，新数据写入window也受到一定的保护，那么不太新和不高频的数据则进入probation接受观察。同时使用Count-Min Sketch估算访问次数节省存储资源，通过引入衰减机制避免了过时热点数据难以淘汰的问题
- 使用JMeter进行本地压力测试，线程数设置为200，持续时间1分钟，数据库帖子总数为486836，对路径`/index?sortingMode=1`发送http get请求进行压测

| 缓存策略       | 总请求数  | 平均响应时间/ms | p50   | p90   | p95   | p99   | 最小响应时间/ms | 最大响应时间/ms | 异常率 | 吞吐量qps  | 数据接收量kb/s | 数据发送量kb/s |
|------------|-------|-----------|-------|-------|-------|-------|-----------|-----------|-----|---------|-----------|-------|
| 无缓存        | 1300  | 9465      | 10164 | 11070 | 11150 | 11326 | 784       | 18641     | 0%  | **18**   | 51        | 2     |
| 仅redis     | 21977 | 36        | 3     | 11    | 160   | 633   | 1         | 4766      | 0%  | **366** | 1035      | 48    |
| redis+本地缓存 | 23537 | 4         | 2     | 5     | 8     | 71    | 1         | 242       | 0%  | **392** | 1108      | 51    |
- 相比于无缓存，多级缓存在响应时间上的表现有了翻天覆地的提升，吞吐量更是翻了20多倍。而与仅使用redis缓存相比，响应时间的各级指标也均有提升，尤其平均响应时间和p99均缩短了接近90%，吞吐量提升7%
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
    private int status; // 0 unread, 1 read, 2 deleted
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
## 4 Redis
### 4.1
## 5 application.properties 配置项
```properties
# Data Source Properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/community?characterEncoding=utf-8&useSSL=false&serverTimezone=Hongkong
spring.datasource.username
spring.datasource.password
spring.datasource.type=com.zaxxer.hikari.HikariDataSource
spring.datasource.hikari.maximum-pool-size=15
spring.datasource.hikari.minimum-idle=3
spring.datasource.hikari.idle-timeout=3000

# Mybatis Properties
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=com.marvel.communityforum.entity
mybatis.configuration.use-generated-keys=true
mybatis.configuration.map-underscore-to-camel-case=true

# Logger
logging.level.com.marvel.communityforum=info

# Mail Properties
spring.mail.host=smtp.163.com
spring.mail.port=465
spring.mail.username
spring.mail.password
spring.mail.protocol=smtps
spring.mail.properties.mail.smtp.ssl.enable=true

# Domain Properties
community.domain=http://127.0.0.1:8080

# Redis Properties
spring.redis.database=6
spring.redis.host=localhost
spring.redis.port=6379

# Kafka Properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=test-consumer-group
spring.kafka.consumer.enable-auto-commit=true
spring.kafka.consumer.auto-commit-interval=3000

# Caffeine Properties
caffeine.hotpost.max-size=10
caffeine.hotpost.expire-seconds=300
```
