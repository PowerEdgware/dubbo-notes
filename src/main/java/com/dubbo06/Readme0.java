package com.dubbo06;

import java.lang.reflect.Method;

public class Readme0 {

	void readme() {
		//默认的协议：dubbo,其他对比：WebServiceProtocol
		//1.dubbo的序列化协议 --基于服务端的导出服务
		//DubboProtocol export导出服务，监听本地端口-->createServer添加dubbo为默认的codec
		//NettyServer doOpen
		//channelInit内部;
		//
		//Codec2--基于他的扩展点实现 ，默认：
		//dubbo=org.apache.dubbo.rpc.protocol.dubbo.DubboCountCodec
		//DubboCountCodec  内部维护：DubboCodec
		//DubboCountCodec传入 NettyCodecAdapter
		//NettyCodecAdapter内部维护着与Netty挂钩的 :io.netty.channel.ChannelHandler
		//作为与dubbocodec的纽带
		//最终根据请求头计算出序列化类型
		//调用 CodecSupport 进行序列化和反序列化
		//CodecSupport维护了基于 Serialization 扩展点的所有序列化类型
		//2 基于客户端的服务引用
		//DubboProtocol.protocolBindingRefer  最终还是走 DubboCountCodec
		//NettyClient
		//发送数据使用的序列化类型：
		//从客户端调用发送数据开始
		//invoke-->InvokerInvocationHandler
		//客户端发送数据使用的序列化方式是从url
		//参数key=serialization。可在配置文件标签：<dubbo:protocol> 中配置 . 获取默认序列化是：hessian2
		//CodecSupport.getSerialization
		
		
		//TODO 集群容错
		//FailoverClusterInvoker
		
		//TODO 负载均衡
		
		//AbstractLoadBalance 
		
		//ClassLoader
		//Class.forName
		//ConsistentHashLoadBalance
		
	}
	
	public static void main(String[] args) throws Exception {
		
		boolean equals=ClassLoader.getSystemClassLoader()==Thread.currentThread().getContextClassLoader();
		System.out.println(equals);
		ClassLoader appClsLoader=ClassLoader.getSystemClassLoader();
		String clsName="com.dubbo06.Loader";
		try {
			Class<?> cls=Class.forName(clsName, false, appClsLoader);
			System.out.println(cls.getName());
			
			//CLassLoader.loaderClass
			Class<?> cls2=appClsLoader.loadClass(clsName);
			System.out.println(cls2==cls);
			
			Method mSay=cls2.getDeclaredMethod("sayLoader", null);
			System.out.println(mSay);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}

class Loader{
	static {
		System.out.println("LoaderXX");
	}
	
	 void sayLoader() {
		System.out.println("sayLoader");
	}
	
}
