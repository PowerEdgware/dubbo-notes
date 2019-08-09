package com.dubbo04;

import java.util.Random;

public class HelloServiceImpl implements HelloService {

	private static int bound = 1000;
	private String defaultName = "allger";

	@Override
	public String sayHello(String name) {
		if (name == null) {
			name = defaultName;
		}
		return "hello," + name;
	}

	public int testMethod(String arg1) {
		return getRnd().nextInt(bound);
	}

	private Random getRnd() {
		return new Random();
	}

	public String getDefaultName() {
		return defaultName;
	}

	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

}
