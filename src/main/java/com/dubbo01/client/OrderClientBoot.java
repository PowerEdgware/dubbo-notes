package com.dubbo01.client;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dubbo01.api.IOrderService;

public class OrderClientBoot {

	public static void main(String[] args) {

		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("dubbo01/application.xml");

		IOrderService orderService = context.getBean(IOrderService.class);
		String result = orderService.order("19200201", 10);
		System.out.println("cli ret=" + result);
	}
}
