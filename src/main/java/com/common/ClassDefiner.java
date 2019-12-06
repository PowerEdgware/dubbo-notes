package com.common;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class ClassDefiner {

	static sun.misc.Unsafe unsafe;
	static {
		try {
		      Field field = Unsafe.class.getDeclaredField("theUnsafe");
		      field.setAccessible(true);
		      unsafe= (Unsafe) field.get(null);
		    } catch (Exception e) {
		      e.printStackTrace();
		    }
	}
	public static void main(String[] args) {
		
		String code="public class A{"
				+ "private String name=\"xx\";"
				+ "private int age;"
				+ "public A(){"
				+	"System.out.println(this);"
				+ "}"
				+"public String getName(){"
				+ "return this.name}"
				+ "}";
		Class cls=ClassDefiner.class;
		try {
			Class.forName("A", true, org.apache.dubbo.common.utils.ClassUtils.getCallerClassLoader(cls));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			ClassLoader cloader=Thread.currentThread().getContextClassLoader();
			//public native Class<?> defineClass(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain);
			cls=unsafe.defineClass("A", code.getBytes(), 0, code.getBytes().length, cloader,cls.getProtectionDomain());
		}
		
					
	}
}
