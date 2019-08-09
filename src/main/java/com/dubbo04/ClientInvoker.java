package com.dubbo04;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientInvoker {

	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo04/consumer.xml" });
		context.start();
		HelloService demoService = context.getBean(HelloService.class); // ��ȡԶ�̷������
		String hello = demoService.sayHello("world"); // ִ��Զ�̷���
		System.out.println(hello + " " + demoService.getClass()); // ��ʾ���ý��

		context.close();
	}
}
