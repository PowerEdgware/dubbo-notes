## dubbo 服务发布/引用/调用
> 主要是服务导出，也即服务端暴露端口对外提供服务。
>服务引用，客户端与服务端建立通信连接。
>服务调用，真正的客户端调用服务端。

###服务发布(导出)

>服务导出的过程就是把服务接口的实现暴露出去，并开启一个端口，把自己的信息注册到注册中心，供客户端连接、调用。

**导出起点 **:`ServiceBean.onApplicationEvent()`  
**服务导出调用链如下**：
核心链路：  `ServiceBean.onApplicationEvent()`->`ServiceBean.export()`->`ServiceConfig.doExport()`->
`ServiceConfig.doExportUrls()`->`ServiceConfig.super.loadRegistries()`->`ServiceConfig.doExportUrlsFor1Protocol()`

 ```
   @SuppressWarnings({"unchecked", "rawtypes"})
    private void doExportUrls() {
        List<URL> registryURLs = loadRegistries(true);
        for (ProtocolConfig protocolConfig : protocols) {
            String pathKey = URL.buildKey(getContextPath(protocolConfig).map(p -> p + "/" + path).orElse(path), group, version);
            ProviderModel providerModel = new ProviderModel(pathKey, ref, interfaceClass);
            ApplicationModel.initProviderModel(pathKey, providerModel);
            doExportUrlsFor1Protocol(protocolConfig, registryURLs);
        }
    }
```
继续：`ServiceConfig.doExportUrlsFor1Protocol()`
> 该方法前期流程，配置解析，包括：协议，方法和方法参数解析，组装解析后的配置参数为map，设置provider侧的接口方法等必要信息，获取到的host port，dubbo版本等等存入map，最后使用组装的map构建URL。
> dubbo服务导出流程贯穿对URL参数的使用，利用 URL 作为配置信息的统一格式，所有扩展点都通过传递 URL 携带配置信息。

组装完毕的服务提供者URL样例：  
`dubbo://192.168.88.118:20880/com.dubbo04.HelloService?anyhost=true&application=hello-world-app&bean.name=com.dubbo04.HelloService&bind.ip=192.168.88.118&bind.port=20880  &deprecated=false&dubbo=2.0.2&dynamic=true&generic=false &interface=com.dubbo04.HelloService&methods=sayHello&pid=15684 &register=true&release=2.7.2&server=netty &side=provider&timestamp=1575273041620`

注册中心URL样例：  
`[registry://127.0.0.1:2181/org.apache.dubbo.registry.RegistryService?application=hello-world-app&dubbo=2.0.2&pid=15684&registry=zookeeper&release=2.7.2&timestamp=1575272877531]`  

注册中心URL把服务导出的URL添加到自己的参数中：key=export,注册中心组装服务提供者后的完整URL:  
`registry://127.0.0.1:2181/org.apache.dubbo.registry.RegistryService?application=hello-world-app&dubbo=2.0.2 &export=dubbo%3A%2F%2F192.168.88.118%3A20880%2Fcom.dubbo04.HelloService%3Fanyhost%3Dtrue%26application%3Dhello-world-app%26bean.name%3Dcom.dubbo04.HelloService%26bind.ip%3D192.168.88.118%26bind.port%3D20880%26deprecated%3Dfalse%26dubbo%3D2.0.2%26dynamic%3Dtrue%26generic%3Dfalse%26interface%3Dcom.dubbo04.HelloService%26methods%3DsayHello%26pid%3D15684%26register%3Dtrue%26release%3D2.7.2%26server%3Dnetty%26side%3Dprovider%26timestamp%3D1575273041620 &pid=15684&registry=zookeeper&release=2.7.2&timestamp=1575272877531`

随后以该注册中心为协议的URL进行服务导出流程：  

**获取Invoker**  
`ServiceConfig.doExportUrlsFor1Protocol()`方法中获取`invoker`:

```
 Invoker<?> invoker = proxyFactory.getInvoker(ref, (Class) interfaceClass, registryURL.addParameterAndEncoded(EXPORT_KEY, url.toFullString()));
                        DelegateProviderMetaDataInvoker wrapperInvoker = new DelegateProviderMetaDataInvoker(invoker, this);
```
其中：`ref`是真正的实现类，`interfaceClass`是实现类的接口。 proxyFactory是扩展点，默认会调用到`JavassistProxyFactory#getInvoker` 最终返回的invoker是：`AbstractProxyInvoker`子类，属于 `JavassistProxyFactory`的内部类。  具体代码：  

```
   @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {
        // TODO Wrapper cannot handle this scenario correctly: the classname contains '$'
        final Wrapper wrapper = Wrapper.getWrapper(proxy.getClass().getName().indexOf('$') < 0 ? proxy.getClass() : type);
        return new AbstractProxyInvoker<T>(proxy, type, url) {
            @Override
            protected Object doInvoke(T proxy, String methodName,
                                      Class<?>[] parameterTypes,
                                      Object[] arguments) throws Throwable {
                return wrapper.invokeMethod(proxy, methodName, parameterTypes, arguments);
            }
        };
    }
```

**服务导出**  

```
  Exporter<?> exporter = protocol.export(wrapperInvoker);
                        exporters.add(exporter);
```

首先是利用SPI获取注册中心URL协议中的Protocol,本例中是：Registry，经过包装后的Protocol为：  
`ProtocolListenerWrapper->ProtocolFilterWrapper->QosProtocolWrapper->RegistryProtocol` 分别调用export方法。  最终调用`RegistryProtocol`,驱动URL为上述invoker保存的以`registry`为协议头的URL。  

`RegistryProtocol#export`  
a.获取注册中心URL，通过变化URL协议头的方式获取配置的注册中心URL，本例中为：`zookeeper`  
b.从URL参数`key=export`获取服务提供者配置信息，进行本地服务导出。(也就是启动服务暴露端口，此例中为netty服务)  
c.把服务提供者信息和相关订阅信息同步到注册中心。(本例中是`zookeeper`)  
d.创建并返回 DestroyableExporter  

---
*本地导出`RegistryProtocal#doLocalExport`*   

```
 @SuppressWarnings("unchecked")
    private <T> ExporterChangeableWrapper<T> doLocalExport(final Invoker<T> originInvoker, URL providerUrl) {
        String key = getCacheKey(originInvoker);

        return (ExporterChangeableWrapper<T>) bounds.computeIfAbsent(key, s -> {
            Invoker<?> invokerDelegate = new InvokerDelegate<>(originInvoker, providerUrl);
            return new ExporterChangeableWrapper<>((Exporter<T>) protocol.export(invokerDelegate), originInvoker);
        });
    }
```
其中 key就是以dubbo为协议头的服务提供者的URL.重点导出逻辑为：
`protocol.export(invokerDelegate)` 
`invokerDelegate=RegistryProtocol$InvokerDelegate->DelegateProviderMetaDataInvoker->JavassistProxyFactory$1`  

protocol为自适应扩展点，且生成的为包装后的提供者Protocol,本例中为：`DubboProtocol`  
包装后的Protocol链路：
`ProtocolListenerWrapper->ProtocolFilterWrapper->QosProtocolWrapper->DubboProtocol` 并依次`export`，其中ProtocolFilterWrapper中会构建调用链，规则是：  
获取激活的filter 从数组最后一个元素到首元素构建调用链，并返保存了首元素filter对应的Invoker并被`ProtocolFilterWrapper$CallbackRegistrationInvoker`包装。调用流程刚好是从首元素到最后一个元素。每个filter/invoker都保存了 `invokerDelegate`引用，但调用时恰好好最后一个保存了`invokerDelegate`的引用的元素会真正调用`invokerDelegate#invoke`方法，实现目标方法的链式调用。  

最终调用到 `DubboProtocol#export`导出代码， 具体如下：  

```
    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        URL url = invoker.getUrl();

        // export service.
        String key = serviceKey(url);
        DubboExporter<T> exporter = new DubboExporter<T>(invoker, key, exporterMap);
        exporterMap.put(key, exporter);
		...省略无关代码
        openServer(url);
        optimizeSerialization(url);
        return exporter;
    }
```
上述的key格式：`group/服务接口名:接口版本:服务端口`  
本例中为：com.dubbo04.HelloService:20880 因为没有指定分组和服务版本，所以省略了。  

启动服务开启本地端口 :`openServer(url);`  

```
    private void openServer(URL url) {
        // find server.
        String key = url.getAddress();
        //client can export a service which's only for server to invoke
        boolean isServer = url.getParameter(IS_SERVER_KEY, true);
        if (isServer) {
            ExchangeServer server = serverMap.get(key);
            if (server == null) {
                synchronized (this) {
                    server = serverMap.get(key);
                    if (server == null) {
                        serverMap.put(key, createServer(url));
                    }
                }
            } else {
                // server supports reset, use together with override
                server.reset(url);
            }
        }
    }
```
根据`IP:PORT`的规则从缓存查找server.再看`createServer`  

```
 private ExchangeServer createServer(URL url) {
       	...
        ExchangeServer server;
        try {
            server = Exchangers.bind(url, requestHandler);
        } catch (RemotingException e) {
            throw new RpcException("Fail to start server(url: " + url + ") " + e.getMessage(), e);
        }
     	...
        return server;
    }

```
>创建server的过程就是启动服务监听端口的过程，根据URL驱动参数server类型确定最终需要启动的服务，本例中是netty.  

创建server并绑定端口：  
a.添加参数构建新的URL。添加编解码codec=dubbo，添加心跳时间：60s。  
b.绑定服务`public static ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException`。  
`Exchanges.bind()` 参数handler为：DubboProtocol内部类：`private ExchangeHandler requestHandler = new ExchangeHandlerAdapter() {}`  
获取到`HeaderExchanger#bind`进行绑定。
部分代码：   

```
  @Override
    public ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
        return new HeaderExchangeServer(Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler))));
    }
```
`HeaderExchanger`中包装 `ExchangeHandler`为`ChannelHandler` 并调用`Transporters#bind`进行绑定。  
此时 ChannelHandler为：`DecodeHandler->HeaderExchangeHandler->requestHandler(DubboProtocol$1)`  

```
Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler)))
```
Transporters.bind内部调用 `NettyTransporter.bind`并绑定netty服务端口返回`NettyServer`  

```
 @Override
    public Server bind(URL url, ChannelHandler listener) throws RemotingException {
        return new NettyServer(url, listener);
    }
```
此时的ChannelHandler是上述链路一路传递进来的 `DecodeHandler`  

NettyServer会调用Netty服务端API创建服务，绑定端口。
在NettyServer构造器中继续包装`DecodeHandler`:

```
public NettyServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, ChannelHandlers.wrap(handler, ExecutorUtil.setThreadName(url, SERVER_THREAD_POOL_NAME)));
    }
```
最终包装的结果还是ChannelHandler并传递到父类引用中保存。
`MultiMessageHandler->HeartbeatHandler->Dispatcher扩展点返回的ChannelHandler->DecodeHandler->HeaderExchangeHandler->requestHandler(DubboProtocol$1)`  

NettyServer继承关系：`NettyServer->AbstractServer->AbstractEndpoint->AbstractPeer` `AbstractPeer`其自身也是一个`ChannelHandler`  
并保存了`MultiMessageHandler`的引用。另外保存了服务提供者的URL。  

关于netty绑定服务监听，其中添加到netty `ChannelPipeline`的编解码器/消息处理器包括`NettyServerHandler extends ChannelDuplexHandler`  
他是真正的netty 中`ChannelHandler`负责netty层消息在Pipeline中传播的处理。其中`NettyServerHandler`保存了服务提供者的URL和`NettyServer`  
实例的引用，负责最后消息的链式分发处理。 关于dubbo中消息的编解码参考`NettyServer#doOpen`，部分代码：  

```
NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec(), getUrl(), NettyServer.this);
                        ch.pipeline()//.addLast("logging",new LoggingHandler(LogLevel.INFO))//for debug
                                .addLast("decoder", adapter.getDecoder())//head
                                .addLast("encoder", adapter.getEncoder())
                                .addLast("server-idle-handler", new IdleStateHandler(0, 0, idleTimeout, MILLISECONDS))
                                .addLast("handler", nettyServerHandler);//tail
```
**codec默认就是dubbo协议**中以`dubbo`为key的`Codec2`扩展点，其默认实现是：  
`DubboCountCodec` 内部维护了`DubboCodec`作为真正的编解码。    

**Dubbo Server端业务线程池**相关设置，该参数设置依赖URL驱动，默认的线程池类型：fixed,可以通过关键字：`threadpool`进行配置。  
默认的线程池名字：`DubboServerHandler`  
`FixedThreadPool` 相关参数的URL获取：    
1.`threadname` 线程池名字；默认是：`DubboServerHandler`  
2.`threads` 核心线程和组最大线程数量，二者相等；默认：`200`    
3.`queues`线程池中队列大小；默认：`0` 使用的队列：`SynchronousQueue`，其他queues的值，使用：`LinkedBlockingQueue`队列。  

**Dubbo Server IO线程相关设置**：  
1.默认Netty 的Boss线程池只有一个线程，线程池名字：`NettyServerBoss`;    
worker线程池名字：`NettyServerWorker`
Worker线程池大小可以通关URL key:`iothreads`显示设置，默认大小：`Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);`  
2.其他参数，比如服务端连接数限制，对应的URL key:`accepts`默认是0 代表不限制。  
消息读取超时时间：`timeout` 默认 1s，连接超时时间：`connect.timeout` 默认3s，  
闲置超时时间：`idle.timeout` 默认600s，心跳超时时间：`heartbeat`默认60s等。  

---
*服务注册*  

回到` RegistryProtocol#export` 方法：  

```
 @Override
    public <T> Exporter<T> export(final Invoker<T> originInvoker) throws RpcException {
        URL registryUrl = getRegistryUrl(originInvoker);
        // url to export locally
        URL providerUrl = getProviderUrl(originInvoker);
		... 省略相关
        //export invoker
        final ExporterChangeableWrapper<T> exporter = doLocalExport(originInvoker, providerUrl);//TODO 在服务本地导出已经分析过

        // url to registry
        final Registry registry = getRegistry(originInvoker);
        final URL registeredProviderUrl = getRegisteredProviderUrl(providerUrl, registryUrl);
        ProviderInvokerWrapper<T> providerInvokerWrapper = ProviderConsumerRegTable.registerProvider(originInvoker,
                registryUrl, registeredProviderUrl);
        //to judge if we need to delay publish
        boolean register = registeredProviderUrl.getParameter("register", true);
        if (register) {
            register(registryUrl, registeredProviderUrl);
            providerInvokerWrapper.setReg(true);
        }
		...省略相关
		
        exporter.setRegisterUrl(registeredProviderUrl);
        exporter.setSubscribeUrl(overrideSubscribeUrl);
        //Ensure that a new exporter instance is returned every time export
        return new DestroyableExporter<>(exporter);
    }
```

此处的originInvoker为上文分析出的`JavassistProxyFactory.getInvoker`的返回值：  
`originInvoker=DelegateProviderMetaDataInvoker->JavassistProxyFactory$1`


获取注册中心并注册：	

```
	//获取注册中心
  private Registry getRegistry(final Invoker<?> originInvoker) {
        URL registryUrl = getRegistryUrl(originInvoker);
        return registryFactory.getRegistry(registryUrl);
    }
    //向注册中心注册provider信息
     public void register(URL registryUrl, URL registeredProviderUrl) {
        Registry registry = registryFactory.getRegistry(registryUrl);
        registry.register(registeredProviderUrl);
    }
```

以zookeeper为例获取注册中心： `ZookeeperRegistryFactory`
`ZookeeperRegistryFactory extends AbstractRegistryFactory`

获取的 ZookeeperRegistry 进行注册。

最终会调用：`ZookeeperRegistry#doRegister` 利用curatorClient创建服务节点

```
  @Override
    public void doRegister(URL url) {
        try {
            zkClient.create(toUrlPath(url), url.getParameter(DYNAMIC_KEY, true));
        } catch (Throwable e) {
            throw new RpcException("Failed to register " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }
```



 

