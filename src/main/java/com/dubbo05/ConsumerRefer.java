package com.dubbo05;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dubbo04.HelloService;

public class ConsumerRefer {

	void readme() {
		//TODO 基于2.6.4
		// ReferenceBean
//		ReferenceConfig
		//DubboNamespaceHandler
		//NettyClient
		//invoke-->InvokerInvocationHandler
		//FailoverClusterInvoker
		//HeaderExchangeHandler 
	}
	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo05/consumer.xml" });
		context.start();
		HelloService demoService = context.getBean(HelloService.class); // ��ȡԶ�̷������
		String hello = demoService.sayHello("world"); // ִ��Զ�̷���
		System.out.println(hello + " " + demoService.getClass()); // ��ʾ���ý��

		context.close();
	}
}
