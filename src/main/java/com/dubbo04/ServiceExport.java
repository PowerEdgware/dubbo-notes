package com.dubbo04;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServiceExport {
	
	void nettyReview() {
		//AbstractChannel
		//ClientChannel --> AbstractNioByteChannel
		//NioEventLoop
	}

	void register() {
		// registry://224.5.6.7:1234/com.alibaba.dubbo.registry.RegistryService?application=hello-world-app&dubbo=2.0.2&export=dubbo%3A%2F%2F192.168.43.8%3A20880%2Fcom.dubbo03.HelloService%3Fanyhost%3Dtrue%26application%3Dhello-world-app%26bind.ip%3D192.168.43.8%26bind.port%3D20880%26dubbo%3D2.0.2%26generic%3Dfalse%26interface%3Dcom.dubbo03.HelloService%26methods%3DsayHello%26pid%3D3960%26side%3Dprovider%26timestamp%3D1564882106234&pid=3960&registry=multicast&timestamp=1564882106057
		// export=dubbo://192.168.43.8:20880/com.dubbo03.HelloService?anyhost=true&application=hello-world-app&bind.ip=192.168.43.8&bind.port=20880&dubbo=2.0.2&generic=false&interface=com.dubbo03.HelloService&methods=sayHello&pid=3960&side=provider&timestamp=1564882106234
		// protocol
		// QosProtocolWrapper(ProtocolListenerWrapper(ProtocolFilterWrapper(RegistryProtocol)))
		// start real export.assume use dubbo protocol
		// QosProtocolWrapper(ProtocolListenerWrapper(ProtocolFilterWrapper(DubboProtocol)))
		//Dispatcher
	}

	void readme() {
//		ServiceBean
		// ExtensionLoader
		// ServiceConfig
		// SimpleDataStore
		// NettyServer
		// DubboProtocol
		//DubboNamespaceHandler
		//DubboCodec
		
		//Server ChannelHandler wrapper
		//AllChannelHandler extends WrappedChannelHandler
		//DubboProtocol$1 内部类：requestHandler:DubboProtocol
		//MultiMessageHandler(HeartbeatHandler(AllChannelHandler(DecodeHandler((HeaderExchangeHandler(DubboProtocol$1()))))))
	}

	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("dubbo04/provider.xml");
		HelloService helloService = ctx.getBean(HelloService.class);
		System.out.println(helloService.getClass());

		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ctx.close();
	}
}
