## dubbo支持的序列化协议以及扩展协议

---

## dubbo集群容错

** 集群出现的目**  

避免单点调用故障，服务提供者会出现多个，此时消费者会拿到多个服务者url，怎么选择调用，以及出现错误如何处理。集群解决这些问题。

** 集群工作过程 **  

1.服务消费者初始化引用阶段，集群`Cluster`实现者给客户端创建`Cluster invoker`实例，客户端代理类最终会包装该实例(可能是多层包装)，为后续服务调用做准备。  
2.服务消费者进行远程调用时，通过`Cluster invoker`从服务目录Directory中列举服务提供者列表(`List<Invoker>`)，其中还包括服务路由的路由规则过滤，返回符合条件的`Invoker`列表。  
获取服务列表后，根据负载均衡策略通过`LoadBalance`从列表中选择一个`Invoker`进行真正的远程调用。


** Dubbo多种集群容错的实现**    

> 集群涉及的组件： Cluster、Cluster Invoker、Directory、Router 和 LoadBalance 等。

 * `Failover Cluster` - 失败自动切换  
 * `Failfast Cluster` - 快速失败  
 * `Failsafe Cluster` - 失败安全  
 * `Failback Cluster` - 失败自动恢复  
 * `Forking Cluster` - 并行调用多个服务提供者  
 
** Cluster Invoker 分析** 

客户端引用完毕（dubbo05消费者引用），返回的代理对象中包含 `Cluster invoker`->`MockClusterInvoker->FailoverClusterInvoker->..`

上述invoker被`InvokerInvocationHandler(invoker)`包装，且包装于代理类`proxy0`中。

调用也是从`InvokerInvocationHandler#invoker`这里开始。

定位到`Cluster Invoker` 的父类 `AbstractClusterInvoker` 源码:  

```
    @Override
    public Result invoke(final Invocation invocation) throws RpcException {
        checkWhetherDestroyed();

        // binding attachments into invocation.
        Map<String, String> contextAttachments = RpcContext.getContext().getAttachments();
        if (contextAttachments != null && contextAttachments.size() != 0) {
            ((RpcInvocation) invocation).addAttachments(contextAttachments);
        }
        List<Invoker<T>> invokers = list(invocation);// 列举invokers
        LoadBalance loadbalance = initLoadBalance(invokers, invocation);//初始化LoadBalance
        RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);
        return doInvoke(invocation, invokers, loadbalance);//doInvoke调用
    }
```
*列举invokers*   
直接从(动态)服务目录获取invokers，获取过程经过服务路由的过滤后返回。  

*初始化LoadBalance*   
获取方法级别的loadBalance：key=methodName.`loadbalance`。获取不到，在获取key=`loadbalance`的loadBalcace在获取不到则返回默认的key:`random`
默认key对应的是`RandomLoadBalance`。

*doInvoke调用*  
子类`FailoverClusterInvoker#doInvoke`

FailoverClusterInvoker 在调用失败时，会自动切换 Invoker 进行重试，默认配置是这个Cluster invoker.  

调用流程大致如下：  
通过负载均衡选择 Invoker，把选择的invoker放入已调用的invoked列表中，随后调用目标invoker，调用失败会进行重试。  

选择invoker并调用：   
a.判断方法参数是否对invoker粘滞，key=sticky。如果是，且粘滞invoker不为空，从invoked中检查是否被调用过，没有且当前invoker可用则直接返回。
b.调用父类`AbstractClusterInvoker#doSelect`方法继续选择，接着调用`LoadBalance#select`做负载均衡选择，默认是random。  
如果负载均衡选择出来的invoker不可用，或者选择出来的invoker已经出现在invoked列表中，则会进行重新选择:`reselect` 重新选择是，从所有`invokers`列表中  
去除已经调用过的`invoked`列表组成未调用列表`reselectInvokers`，再次调用负载均衡算法从未调用列表中选择invoker。如果重新选择的invoker不为空，则直接返回。  
否则，从所有`invokers`列表中选择上次不可用或已经被调用过的invoker的下一个元素并返回。  
c.从`doSelect`返回以后，如果是粘滞invoker，则设置粘滞invoker为当前选择的invoker` stickyInvoker = invoker;`并从select处返回。  
d.从select方法返回后，调用目标 Invoker 的 invoke 方法。正常调用则返回调用者，否则进行重试，次数耗尽仍失败，则抛出`RpcException`给上层调用者。   

** 其他的负载均衡容错Invoker**   

A.`FailbackClusterInvoker`  



---
## dubbo负载均衡算法

---
## dubbo 服务调用

---
##dubbo 通过激活扩展点实现服务限流，降级