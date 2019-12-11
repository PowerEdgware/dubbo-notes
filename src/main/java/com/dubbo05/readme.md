### 基于注册中心的服务引用流程



#### dubbo服务引用

**起始类： ReferenceBean**  

> 两种方式：1，afterPropertiesSet(饿汉式，可通过 <dubbo:reference> 的 init属性开启) 2，getObject(懒汉式，当该服务被其他类注入时调用，FactoryBean#getObject方法) 默认使用  
>引用的三种方式：1，引用本地(JVM)服务；2，直连方式引用远程服务；3，通过注册中心引用远程服务。生成`Invoker `实例，该实例具备本地或远程服务调用能力，最终框架使用`ProxyFactory`为服务接口生成代理类
>封装`Invoker` 并让代理类去调用 Invoker 逻辑。

---
*从ReferenceBean#getObject开始*  

```
 @Override
    public Object getObject() {
        return get(); //ReferenceConfig.get()->init
    }
```

1.解析客户端配置信息：  

`{side=consumer, register.ip=192.168.88.118, release=2.7.2, methods=sayHello, lazy=false, qos.port=9908, dubbo=2.0.2, pid=11276, interface=com.dubbo04.HelloService, qos.enable=true, timeout=30000, application=consumer-of-helloworld-app, sticky=false, client=netty4, qos.accept.foreign.ip=false, timestamp=1575872698076}`

2.组装生成的注册中心URl List:  

`[registry://127.0.0.1:2181/org.apache.dubbo.registry.RegistryService?application=consumer-of-helloworld-app&dubbo=2.0.2&pid=11276&qos.accept.foreign.ip=false&qos.enable=true&qos.port=9908&refer=application%3Dconsumer-of-helloworld-app%26client%3Dnetty4%26dubbo%3D2.0.2%26interface%3Dcom.dubbo04.HelloService%26lazy%3Dfalse%26methods%3DsayHello%26pid%3D11276%26qos.accept.foreign.ip%3Dfalse%26qos.enable%3Dtrue%26qos.port%3D9908%26register.ip%3D192.168.88.118%26release%3D2.7.2%26side%3Dconsumer%26sticky%3Dfalse%26timeout%3D30000%26timestamp%3D1575872698076&registry=zookeeper&release=2.7.2&timestamp=1575873181822]`

上述URL 列表中可能有多个注册中心，`registry=zookeeper` 代表使用的注册中心类型，这里是zk.`refer`key代表客户端引用url信息.上述url以`registry`为协议头，在创建客户端 `invoker`和代理对象时会传入该URL参数。   

**创建代理前构建`invoker`**  

`ReferenceConfig#createProxy->REF_PROTOCOL.refer(interfaceClass, urls.get(0))`

```
 private T createProxy(Map<String, String> map){
 	...//省略
 	 if (urls.size() == 1) {
                invoker = REF_PROTOCOL.refer(interfaceClass, urls.get(0));
            } else {
                List<Invoker<?>> invokers = new ArrayList<Invoker<?>>();
                URL registryURL = null;
                for (URL url : urls) {
                    invokers.add(REF_PROTOCOL.refer(interfaceClass, url));
                    if (REGISTRY_PROTOCOL.equals(url.getProtocol())) {
                        registryURL = url; // use last registry url
                    }
                }
            }
       ... //
       
        // create service proxy
        return (T) PROXY_FACTORY.getProxy(invoker);
 }
```

上述代码`urls`即时组装生成的 注册中心URL，其中包含客户端需要服务引用的key：`refer`。  

`REF_PROTOCOL`是自适应扩展点:`org.apache.dubbo.rpc.Protocol$Adaptive@2bffa76d`，用来服务引用。    
`PROXY_FACTORY`也是自适应扩展点`org.apache.dubbo.rpc.ProxyFactory$Adaptive@3b718392`用来创建客户端代理。  

**服务引用**  
`invoker = REF_PROTOCOL.refer(interfaceClass, urls.get(0))`  

和服务导出类似，从自适应扩展点中获取url指定协议的:`Protocol` 也即：`RegistryProtocol`，经过包装所生成的`Protocol`为：  
`ProtocolFilterWrapper->ProtocolListenerWrapper->QosProtocolWrapper->RegistryProtocol`

分别调用`Protocol#public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException` 方法，并传入 type 接口参数，和上述获取的注册中心url。这里type=`interface com.dubbo04.HelloService`。

一直调用到`RegistryProtocol#refer` 

```
 @SuppressWarnings("unchecked")
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        url = URLBuilder.from(url)
                .setProtocol(url.getParameter(REGISTRY_KEY, DEFAULT_REGISTRY))
                .removeParameter(REGISTRY_KEY)
                .build();
        Registry registry = registryFactory.getRegistry(url);
        if (RegistryService.class.equals(type)) {
            return proxyFactory.getInvoker((T) registry, type, url);
        }

        // group="a,b" or group="*"
        Map<String, String> qs = StringUtils.parseQueryString(url.getParameterAndDecoded(REFER_KEY));
        String group = qs.get(GROUP_KEY);
        if (group != null && group.length() > 0) {
            if ((COMMA_SPLIT_PATTERN.split(group)).length > 1 || "*".equals(group)) {
                return doRefer(getMergeableCluster(), registry, type, url);
            }
        }
        return doRefer(cluster, registry, type, url);
    }

```

转换协议头`registry`为从参数中获取key:`registry=zookeeper`的参数值并将它设置新的url协议头，其他参数不变。
转换后的url协议：  
`zookeeper://127.0.0.1:2181/org.apache.dubbo.registry.RegistryService?application=consumer-of-helloworld-app&dubbo=2.0.2&pid=11276&qos.accept.foreign.ip=false&qos.enable=true&qos.port=9908&refer=application%3Dconsumer-of-helloworld-app%26client%3Dnetty4%26dubbo%3D2.0.2%26interface%3Dcom.dubbo04.HelloService%26lazy%3Dfalse%26methods%3DsayHello%26pid%3D11276%26qos.accept.foreign.ip%3Dfalse%26qos.enable%3Dtrue%26qos.port%3D9908%26register.ip%3D192.168.88.118%26release%3D2.7.2%26side%3Dconsumer%26sticky%3Dfalse%26timeout%3D30000%26timestamp%3D1575872698076&release=2.7.2&timestamp=1575873181822`  

获取注册中心实例，进行服务引用：`doRefer`:  

```
    private <T> Invoker<T> doRefer(Cluster cluster, Registry registry, Class<T> type, URL url) {
        RegistryDirectory<T> directory = new RegistryDirectory<T>(type, url);
        directory.setRegistry(registry);
        directory.setProtocol(protocol);
        // all attributes of REFER_KEY
        Map<String, String> parameters = new HashMap<String, String>(directory.getUrl().getParameters());
        URL subscribeUrl = new URL(CONSUMER_PROTOCOL, parameters.remove(REGISTER_IP_KEY), 0, type.getName(), parameters);
        if (!ANY_VALUE.equals(url.getServiceInterface()) && url.getParameter(REGISTER_KEY, true)) {
            directory.setRegisteredConsumerUrl(getRegisteredConsumerUrl(subscribeUrl, url));
            registry.register(directory.getRegisteredConsumerUrl());
        }
        directory.buildRouterChain(subscribeUrl);
        directory.subscribe(subscribeUrl.addParameter(CATEGORY_KEY,
                PROVIDERS_CATEGORY + "," + CONFIGURATORS_CATEGORY + "," + ROUTERS_CATEGORY));

        Invoker invoker = cluster.join(directory);
        ProviderConsumerRegTable.registerConsumer(invoker, url, subscribeUrl, directory);
        return invoker;
    }
```
创建 `RegistryDirectory`内部保存了很多URL，包括以注册中心为协议的url,以refer为key的值的查询参数等。  
构建 consumerUrl并注册该信息到注册中心。consumerUrl同时保存了客户端配置的相关参数，用于后续注册操作:  
`consumer://192.168.88.118/com.dubbo04.HelloService?application=consumer-of-helloworld-app&category=consumers&check=false&client=netty4&dubbo=2.0.2&interface=com.dubbo04.HelloService&lazy=false&methods=sayHello&pid=2172&qos.accept.foreign.ip=false&qos.enable=true&qos.port=9908&release=2.7.2&side=consumer&sticky=false&timeout=30000&timestamp=1575967156071`  

把consumerUrl信息通过注册中心`ZookeeperRegistry#doRegister`注册到  的 `/dubbo/com.dubbo04.HelloService/consumers`节点上，让consumerUrl成为他的子节点。  

关键的一步：调用`RegistryDirectory#subscribe`方法去订阅查询`category=providers,configurators,routers`等节点信息，目的是获取服务提供者，监听上述节点信息变更的事件，即使刷新服务提供者的信息，实现consumer动态链接服务提供者，方便后续调用。  

继续跟踪：`RegistryDirectory#subscribe`  

```
  public void subscribe(URL url) {
        setConsumerUrl(url);
        CONSUMER_CONFIGURATION_LISTENER.addNotifyListener(this);
        serviceConfigurationListener = new ReferenceConfigurationListener(this, url);
        registry.subscribe(url, this);
    }
```
内部调用 注册中心：`ZookeeperRegistry#subscribe`实现`category`中的节点订阅。并把订阅结果以回调方式处理。
其中：` CONSUMER_CONFIGURATION_LISTENER.addNotifyListener(this);`就是添加的回调处理类。  该回调处理类会传递到`ZookeeperRegistry`中，方便订阅信息发送变化后，进行回调处理。  

最终走到：`ZookeeperRegistry#doSubscribe`此方法会从zk上获取`category=providers,configurators,routers`所有父目录下的子节点，包括`providers`下面的服务提供者信息。  

核心代码：  

```
 @Override
    public void doSubscribe(final URL url, final NotifyListener listener){
    	...
    	   zkClient.create(path, false);
    	   //获取 path目录下的子节点，此处 path为：providers,configurators,routers的一个或多个
                    List<String> children = zkClient.addChildListener(path, zkListener);
                    if (children != null) {
                        urls.addAll(toUrlsWithEmpty(url, path, children));
                    }
                }
                notify(url, listener, urls);//获取到服务提供者节点信息后的核心通知方法
    }
```

重点的：`FailRegistry#notify`方法:  

```
   @Override
    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
      	...
        try {
            doNotify(url, listener, urls);//当前类类 的doNotify方法，会调用父类AbstractRegistry#notify
        } catch (Exception t) {
            // Record a failed registration request to a failed list, retry regularly
            addFailedNotified(url, listener, urls);
            logger.error("Failed to notify for subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
        }
    }
```
传递的参数：url-以consumer为协议头的url；listener-RegistryDirectory；urls-从`providers,configurators,routers`等多个节点获取的子节点集合。  

追踪到：`AbstractRegistry#notify`，经过层层处理，调用`listener#notify`方法，并单独传递，`providers,configurators,routers`下的子节点，其中：`listener=RegistryDirectory`

最后调用到：`RegistryDirectory#refreshInvoker` 并把providers URL信息列表传入入参。

providers下子节点信息列表：  	
`[dubbo://192.168.88.118:20881/com.dubbo04.HelloService?anyhost=true&application=hello-world-app&bean.name=com.dubbo04.HelloService&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&interface=com.dubbo04.HelloService&methods=sayHello&pid=17136&register=true&release=2.7.2&server=netty&side=provider&timestamp=1575959534175]`

调用到：RegistryDirectory的私有方法`toInvokers`，这里会真正执行`DubboProtocol#refer`：  

```
  if (enabled) {
                        invoker = new InvokerDelegate<>(protocol.refer(serviceType, url), url, providerUrl);
                    }
```
最终把生成的invoker 放入map返回，其中` key = url.toFullString();`，这里的url是providerUrl合并后的url，和providerUrl大同小异。

分析：` invoker = new InvokerDelegate<>(protocol.refer(serviceType, url), url, providerUrl);`
protocol为自适应扩展点类，获取到的被包装的扩展点：  
`ProtocolFilterWrapper->ProtocolListenerWrapper->QosProtocolWrapper->DubboProtocol`  

调用到`DubboProtocol#`:返回DubboInvoker

```
 @Override
    public <T> Invoker<T> protocolBindingRefer(Class<T> serviceType, URL url) throws RpcException {
        optimizeSerialization(url);

        // create rpc invoker.
        DubboInvoker<T> invoker = new DubboInvoker<T>(serviceType, url, getClients(url), invokers);
        invokers.add(invoker);

        return invoker;
    }
```
其中`getClients`就是建立与服务端之间的连接。
返回被包装的invoker:`InvokerDelegate->ProtocolFilterWrapper$CallbackRegistrationInvoker->ListenerInvokerWrapper->AsyncToSyncInvoker->DubboInvoker`

其中`ProtocolFilterWrapper$CallbackRegistrationInvoker`包装了很多激活的consumer`Filter`，每个filter被包装成invoker，按照`List<Filter>`逆序包装，最后返回的是首元素`Invoker->Filter`，所以调用方式就是调用invoker，在invoker内部调用filter，进行链式调用，调用的顺序就是按照List元素从`0--size-1`。首元素`invoker->filter`被`CallbackRegistrationInvoker`包装并返回。

至此invoker构建成功，并被放入`RegistryDirectory`属性列表`invokers`中。返回到`RegistryProtocol#doRefer`这里：  

```
    private <T> Invoker<T> doRefer(Cluster cluster, Registry registry, Class<T> type, URL url) {
        RegistryDirectory<T> directory = new RegistryDirectory<T>(type, url);
       	...省略
        directory.subscribe(subscribeUrl.addParameter(CATEGORY_KEY,
                PROVIDERS_CATEGORY + "," + CONFIGURATORS_CATEGORY + "," + ROUTERS_CATEGORY));

        Invoker invoker = cluster.join(directory);
        ProviderConsumerRegTable.registerConsumer(invoker, url, subscribeUrl, directory);
        return invoker;
    }

```
此时已经构建完毕invoker并保存在`RegistryDirectory`属性列表`invokers`中，该属性值会随着注册中心配置变化从而得到通知刷新`invokers`列表。

`Invoker invoker = cluster.join(directory);`是与集群容错相关，一个消费者可能调用多个服务提供者，消费者需要决定调用哪个提供者以及在调用失败时的处理措施，比如重试或是抛出异常等。集群接口 Cluster 以及 Cluster Invoker就是为了处理这些问题，集群 Cluster会把多个服务提供者合并成一个Cluster Invoker，并把这个合并的Invoker暴露给消费者，`Invoker invoker = cluster.join(directory);`这段代码就是做了合并的工作。，其中`cluster`也是自适应扩展点，默认会使用`FailoverCluster`并且被`MockClusterWrapper`包装。具体代码如下：  

```
 @Override
    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return new MockClusterInvoker<T>(directory,
                this.cluster.join(directory));
    }
    
      @Override
    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return new FailoverClusterInvoker<T>(directory);
    }
```
经过包装调用，返回的`invoker=MockClusterInvoker->FailoverClusterInvoker` 并且他们内部都保存了`RegistryDirectory`对象，调用时，会根据负载均衡策略选择服务提供者进行调研。  

最终回到 `ReferenceConfig#createProxy`，此时返回的invoker为`MockClusterInvoker->FailoverClusterInvoker`，为下一步创建代理做准备。

**根据构建完毕的invoker创建代理** 
> 代理对象，用来进行远程调用的，其内部保存了上述生成的invoker对象。

```
private T createProxy(Map<String, String> map) { 
...//省略信息
 invoker = REF_PROTOCOL.refer(interfaceClass, urls.get(0));
 ...//省略
 return (T) PROXY_FACTORY.getProxy(invoker);
}
```

此处的`PROXY_FACTORY`为自适应扩展点的代理工厂，用来创建代理对象，默认会获取`JavassistProxyFactory`同时被`StubProxyFactoryWrapper`包装。`JavassistProxyFactory#getProxy`:		

```
public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
        return (T) Proxy.getProxy(interfaces).newInstance(new InvokerInvocationHandler(invoker));
    }
```
最终就是利用javassist生成 Proxy的子类，并调用子类的`abstract public Object newInstance(InvocationHandler handler);`方法生成，由javassist生成的实现了用户自定义接口的代理类，该代理类保存了`InvocationHandler`的引用。代理类大致如下：  

```
package org.apache.dubbo.common.bytecode;

public class proxy0 implements org.apache.dubbo.demo.DemoService {
    public static java.lang.reflect.Method[] methods;
    private java.lang.reflect.InvocationHandler handler;
    public proxy0() {
    }
    public proxy0(java.lang.reflect.InvocationHandler arg0) {
        handler = $1;
    }
    public java.lang.String sayHello(java.lang.String arg0) {
        Object[] args = new Object[1];
        args[0] = ($w) $1;
        Object ret = handler.invoke(this, methods[0], args);
        return (java.lang.String) ret;
    }
}
```

---

*消费端构建到服务端的链接*  

在`DubboProtocol#protocolBindingRefer`中，会发起客户端连接服务提供者的动作，具体如下：	

```
  private ExchangeClient[] getClients(URL url) {
        // whether to share connection
        boolean useShareConnect = false;
        int connections = url.getParameter(CONNECTIONS_KEY, 0);
        List<ReferenceCountExchangeClient> shareClients = null;

        ExchangeClient[] clients = new ExchangeClient[connections];
        for (int i = 0; i < clients.length; i++) {
            if (useShareConnect) {
                clients[i] = shareClients.get(i);
            } else {
                clients[i] = initClient(url);
            }
        }
        return clients;
    }
```
重点看`initClients`:  

```
private ExchangeClient initClient(URL url){
		...//简化
		 ExchangeClient client=Exchangers.connect(url, requestHandler);
         return client;
}	
```

和服务端类似，跟踪：`Exchangers.connect->HeaderExchanger.connect->Transporters.connect->NettyTransporter.connect`最终在`NettyClient`实现服务的远程连接。  

生成的消息处理链ChannelHandler： `MultiMessageHandler->HeartbeatHandler->AllChannelHandler->DecodeHandler->HeaderExchangeHandler->DubboProtocol$requestHandler`

此handler最终被保存到 `NettyClient`父类`AbstractPeer`属性`handler`中。用来处理netty消息。
netty Channel上处理消息编解码的为：`NettyClientHandler`，所有consumer都会注册到同一个eventlop中，来管理多个连接。消费者业务消息处理线程会根据不同端口生成不同的Executor，具体参考：		

`WrappedChannelHandler的构造函数`，dubbo协议的序列化与反序列化可参考`DubboCountCodec`

结束。  

后面还有集群容错，负载均衡，服务调用等。