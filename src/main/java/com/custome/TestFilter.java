package com.custome;

import org.apache.dubbo.common.extension.ExtensionLoader;

public class TestFilter {

	public static void main(String[] args) {
		ExtensionLoader<IFilter> loader=ExtensionLoader.getExtensionLoader(IFilter.class);
		try {
			IFilter filter=loader.getAdaptiveExtension();
			System.out.println(filter.getClass());
		} catch (Exception e) {
		//	e.printStackTrace();
		}
		System.out.println("default="+loader.getDefaultExtension());
		System.out.println("named="+loader.getExtension("myfilter"));
	}
}
