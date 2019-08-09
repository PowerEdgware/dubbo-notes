package com.dubbo01.server;

import com.alibaba.dubbo.container.Main;

//import org.apache.dubbo.container.Main;

public class OrderProviderBoot {

	public static void main(String[] args) {

//		String configPathKey="dubbo.spring.config";
		System.setProperty("dubbo.shutdown.hook", "true");
		Main.main(args);
	}
}
