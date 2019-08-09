package com.wrapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//com.alibaba.dubbo.common.bytecode.Wrapper1 extends Wrapper
public class Wrapper1 {
	// dynamic generate fields
	public static String[] pns;// property name array.
	public static java.util.Map pts;// property type map.<propertyName,Class>其中，propertyName从get/set/is/has方法中获取
	public static String[] mns;// all method names
	public static String[] dmns;//// declared method name array.
	public static Class[] mts0;// public methods param types
	// ..... 0-x个共有方法
	public static Class[] mtsx;
	// -------------------------------

	// 解析的目标对象的方法和属性值
//	 Map<String, Class<?>> pts = new HashMap<String, Class<?>>(); // <property name, property types>
//     Map<String, Method> ms = new LinkedHashMap<String, Method>(); // <method desc, Method instance>
//     List<String> mns = new ArrayList<String>(); // method names.
//     List<String> dmns = new ArrayList<String>(); // declaring method names.

	// c1 设置目标对象的属性值
	public void setPropertyValue(Object o, String n, Object v) {
		com.dubbo04.HelloServiceImpl w;
		try {
			w = ((com.dubbo04.HelloServiceImpl) $1);
		} catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
		if ($2.equals("defaultName")) {
			w.setDefaultName((java.lang.String) $3);
			return;
		}
		throw new com.alibaba.dubbo.common.bytecode.NoSuchPropertyException(
				"Not found property \"" + $2 + "\" filed or setter method in class com.dubbo04.HelloServiceImpl.");
	}

	// c2 获取目标对象属性值
	/**
	 * 
	 * @param o current object
	 * @param n propertyName
	 * @return property value
	 */
	public Object getPropertyValue(Object o, String n) {
		com.dubbo04.HelloServiceImpl w;
		try {
			// $1 ->o
			w = ((com.dubbo04.HelloServiceImpl) $1);
		} catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
		// $2 ->n
		if ($2.equals("defaultName")) {
			return ($w) w.getDefaultName();
		}
		throw new com.alibaba.dubbo.common.bytecode.NoSuchPropertyException(
				"Not found property \"" + $2 + "\" filed or setter method in class com.dubbo04.HelloServiceImpl.");
	}

	// c3 核心调用de 方法
	/**
	 * 
	 * @param o 目标类
	 * @param n 方法名称
	 * @param p 方法参数类型
	 * @param v 方法实参
	 * @return
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	public Object invokeMethod(Object o, String n, Class[] p, Object[] v)
			throws java.lang.reflect.InvocationTargetException {
		com.dubbo04.HelloServiceImpl w;
		try {
			w = ((com.dubbo04.HelloServiceImpl) $1);
		} catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
		try {
			if ("sayHello".equals($2) && $3.length == 1) {
				// 动态拼接而成
				return ($w) w.sayHello((java.lang.String) $4[0]);
			}
			if ("testMethod".equals($2) && $3.length == 1) {
				return ($w) w.testMethod((java.lang.String) $4[0]);
			}
			if ("getDefaultName".equals($2) && $3.length == 0) {
				return ($w) w.getDefaultName();
			}
			if ("setDefaultName".equals($2) && $3.length == 1) {
				w.setDefaultName((java.lang.String) $4[0]);
				return null;
			}
		} catch (Throwable e) {
			throw new java.lang.reflect.InvocationTargetException(e);
		}
		throw new com.alibaba.dubbo.common.bytecode.NoSuchMethodException(
				"Not found method \"" + $2 + "\" in class com.dubbo04.HelloServiceImpl.");
	}

	// TODO 其他的方法省略掉
}
