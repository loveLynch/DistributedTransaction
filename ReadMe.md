# 1.分布式事务理论基础

> 参考：https://blog.csdn.net/hosaos/article/details/89136666
> **两阶段提交协议**(Two Phase Commitment Protocol)中，涉及到两种角色
>
> 一个事务协调者（coordinator）：负责协调多个参与者进行事务投票及提交(回滚)
> 多个事务参与者（participants）：即本地事务执行者
>
> 总共处理步骤有两个
> （1）投票阶段（voting phase）：协调者将通知事务参与者准备提交或取消事务，然后进入表决过程。参与者将告知协调者自己的决策：同意（事务参与者本地事务执行成功，但未提交）或取消（本地事务执行故障）；
> （2）提交阶段（commit phase）：收到参与者的通知后，协调者再向参与者发出通知，根据反馈情况决定各参与者是否要提交还是回滚；
>
> 如果所示 1-2为第一阶段，2-3为第二阶段
>
> 如果任一资源管理器在第一阶段返回准备失败，那么事务管理器会要求所有资源管理器在第二阶段执行回滚操作。通过事务管理器的两阶段协调，最终所有资源管理器要么全部提交，要么全部回滚，最终状态都是一致的。
>
> ![](https://document-store.oss-cn-shenzhen.aliyuncs.com/dt_20191020102003.png?Expires=1603074003&OSSAccessKeyId=LTAIl4SxXuRjoq2z&Signature=6J%2Bh20BGJzNIUNx8CnmLAtoM2lI%3D)

# 2.TCC

> **基本原理**
> TCC 将事务提交分为 Try - Confirm - Cancel 3个操作。其和两阶段提交有点类似，Try为第一阶段，Confirm - Cancel为第二阶段，是一种应用层面侵入业务的两阶段提交。
>
> | 操作方法 | 含义                                                         |
> | -------- | ------------------------------------------------------------ |
> | Try      | 预留业务资源/数据效验                                        |
> | Confirm  | 确认执行业务操作，实际提交数据，不做任何业务检查，try成功，confirm必定成功，需保证幂等 |
> | Cancel   | 取消执行业务操作，实际回滚数据，需保证幂等                   |
>
> 其核心在于将业务分为两个操作步骤完成。不依赖 RM 对分布式事务的支持，而是通过对业务逻辑的分解来实现分布式事务。
>
> **幂等控制**
>
> 使用TCC时要注意Try - Confirm - Cancel 3个操作的**幂等控制**，网络原因，或者重试操作都有可能导致这几个操作的重复执行
>
> ![](https://document-store.oss-cn-shenzhen.aliyuncs.com/md_20191020102548.png?Expires=1603074348&OSSAccessKeyId=LTAIl4SxXuRjoq2z&Signature=lEr7TtGZ%2F73DFjqcqPiWyF%2FIl1M%3D)
>
> **空回滚**
> 如下图所示，事务协调器在调用TCC服务的一阶段Try操作时，可能会出现因为丢包而导致的网络超时，此时事务协调器会触发二阶段回滚，调用TCC服务的Cancel操作；
>
> TCC服务在未收到Try请求的情况下收到Cancel请求，这种场景被称为空回滚；TCC服务在实现时应当允许空回滚的执行；
>
> ![](https://document-store.oss-cn-shenzhen.aliyuncs.com/callback_null_20191020102619.png?Expires=1603074379&OSSAccessKeyId=LTAIl4SxXuRjoq2z&Signature=v6%2FJ7B1ovXIgxWta%2BRM%2FswqlOw0%3D)
>
> 那么具体代码里怎么做呢？
> 分析下，如果try()方法没执行，那么订单一定没创建，所以cancle方法里可以加一个判断，如果上下文中订单编号orderNo不存在或者订单不存在，直接return
>
> ```java
> if(orderNo==null || order==null){
> 	return;
> }
> ```
>
> 核心思想就是 回滚请求处理时，如果对应的具体业务数据为空，则返回成功
>
> 当然这种问题也可以通过中间件层面来实现，如，在第一阶段try()执行完后，向一张事务表中插入一条数据(包含事务id，分支id)，cancle()执行时，判断如果没有事务记录则直接返回，但是现在还不支持
>
> **防悬挂**
> 如下图所示，事务协调器在调用TCC服务的一阶段Try操作时，可能会出现因网络拥堵而导致的超时，此时事务协调器会触发二阶段回滚，调用TCC服务的Cancel操作；在此之后，拥堵在网络上的一阶段Try数据包被TCC服务收到，出现了二阶段Cancel请求比一阶段Try请求先执行的情况；
>
> 用户在实现TCC服务时，应当允许空回滚，但是要拒绝执行空回滚之后到来的一阶段Try请求；
> 这里又怎么做呢？
>
> 可以在二阶段执行时插入一条事务控制记录，状态为已回滚，这样当一阶段执行时，先读取该记录，如果记录存在，就认为二阶段回滚操作已经执行，不再执行try方法；
>
> ![](https://document-store.oss-cn-shenzhen.aliyuncs.com/fxg_20191020102807.png?Expires=1603074487&OSSAccessKeyId=LTAIl4SxXuRjoq2z&Signature=IFdRV8l53TlIDp0EgKkyhfmJhrk%3D)
>
> **事务消息**
> 事务消息更倾向于达成分布式事务的最终一致性，适用于分布式事务的提交或回滚只取决于事务发起方的业务需求
> 
> 
>
> **优缺点比较**
>
> | **事务方案** | **优点**                             | **缺点**                                           |
> | ------------ | ------------------------------------ | -------------------------------------------------- |
> | 2PC          | 实现简单                             | 1、需要数据库(一般是XA支持) 2、锁粒度大，性能差    |
> | TCC          | 锁粒度小，性能好                     | 需要侵入业务，实现较为复杂，复杂业务实现幂等有难度 |
> | 消息事务     | 业务侵入小，无需编写业务回滚补偿逻辑 | 事务消息实现难度大，强依赖第三方中间件可靠性       |
>
> 

# 3.AT

> Seata AT模式是基于XA事务演进而来的一个分布式事务中间件，XA是一个基于数据库实现的分布式事务协议，本质上和两阶段提交一样，需要数据库支持，Mysql5.6以上版本支持XA协议，其他数据库如Oracle，DB2也实现了XA接口
>
> 角色如下
>
> ![](https://document-store.oss-cn-shenzhen.aliyuncs.com/at_tm_20191020105125.png?Expires=1603075885&OSSAccessKeyId=LTAIl4SxXuRjoq2z&Signature=c%2FvoQplJan7JX0pOseryBVztTWc%3D)
>
> Transaction Coordinator (TC)： 事务协调器，维护全局事务的运行状态，负责协调并驱动全局事务的提交或回滚
> Transaction Manager ™： 控制全局事务的边界，负责开启一个全局事务，并最终发起全局提交或全局回滚的决议
> Resource Manager (RM)： 控制分支事务，负责分支注册、状态汇报，并接收事务协调器的指令，驱动分支（本地）事务的提交和回滚
> 基本处理逻辑如下
>
> ![](https://document-store.oss-cn-shenzhen.aliyuncs.com/branch_20191020105501.png?Expires=1603076101&OSSAccessKeyId=LTAIl4SxXuRjoq2z&Signature=kBXzkwQ7Xqkq1TcxeKOd3KPMbvc%3D)
>
> Branch就是指的分布式事务中每个独立的本地局部事务
>
> **第一阶段**
> Seata 的 JDBC 数据源代理通过对业务 SQL 的解析，把业务数据在更新前后的数据镜像组织成回滚日志，利用 本地事务 的 ACID 特性，将业务数据的更新和回滚日志的写入在同一个 本地事务 中提交。
>
> 这样，可以保证：**任何提交的业务数据的更新一定有相应的回滚日志存在**
>
> ![](https://document-store.oss-cn-shenzhen.aliyuncs.com/one_20191020105354.png?Expires=1603076034&OSSAccessKeyId=LTAIl4SxXuRjoq2z&Signature=gdR7asmiA1ROWMQlmBPH1P9WIUc%3D)
>
> 基于这样的机制，分支的本地事务便可以在全局事务的第一阶段提交，并马上释放本地事务锁定的资源
>
> 这也是Seata和XA事务的不同之处，两阶段提交往往对资源的锁定需要持续到第二阶段实际的提交或者回滚操作，而有了回滚日志之后，可以在第一阶段释放对资源的锁定，降低了锁范围，提高效率，即使第二阶段发生异常需要回滚，只需找对undolog中对应数据并反解析成sql来达到回滚目的
>
> 同时Seata通过代理数据源将业务sql的执行解析成undolog来与业务数据的更新同时入库，达到了对业务无侵入的效果
>
> **第二阶段**
> 如果决议是全局提交，此时分支事务此时已经完成提交，不需要同步协调处理（只需要异步清理回滚日志），Phase2 可以非常快速地完成
> ![](https://document-store.oss-cn-shenzhen.aliyuncs.com/two1_20191020105605.png?Expires=1603076165&OSSAccessKeyId=LTAIl4SxXuRjoq2z&Signature=LLizQukPtjsgJCY9Y7CKhobTCwU%3D)
>
> 如果决议是全局回滚，RM 收到协调器发来的回滚请求，通过 XID 和 Branch ID 找到相应的回滚日志记录，**通过回滚记录生成反向的更新 SQL 并执行**，以完成分支的回滚
>
> ![](https://document-store.oss-cn-shenzhen.aliyuncs.com/two2_20191020105619.png?Expires=1603076179&OSSAccessKeyId=LTAIl4SxXuRjoq2z&Signature=hoUK3%2BXVlUirrxxU1pQwjRnp5e8%3D)

