package com.common;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.dubbo.common.compiler.Compiler;
import org.apache.dubbo.common.compiler.support.JdkCompiler;

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
		
		String code=""
				+ "public class A {"
				+" static{"
				+ " System.out.println(\"abc\");"
				+"}"
				+ "private String name=\"xx\"; "
				+ "private int age; "
				+ "public A(){ "
				+	"System.out.println(this); "
				+ "} "
				+"public String getName(){"
				+ "return this.name;}"
				+ "}";
		Class cls=ClassDefiner.class;
		try {
			Class.forName("A", true, org.apache.dubbo.common.utils.ClassUtils.getCallerClassLoader(cls));
		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
			ClassLoader cloader=Thread.currentThread().getContextClassLoader();
			//public native Class<?> defineClass(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain);
			//Exception in thread "main" java.lang.ClassFormatError: Incompatible magic value 1885430635 in class file A
			//cls=unsafe.defineClass("A", code.getBytes(), 0, code.getBytes().length, cloader,cls.getProtectionDomain());
		}
		
		//ExtensionLoader
//		java.lang.Compiler
//		Compiler
		//JavaCompiler
		//jsp如果被重新编译?
		//java动态代理如何生成的字节码
		//Proxy
		//javax.annotation.processing.Processor
		
		Compiler compiler=new JdkCompiler();
		try {
			Class<?> aclazz=compiler.compile(code, ClassDefiner.class.getClassLoader());
			System.out.println(aclazz);
			Stream.of(aclazz.getDeclaredMethods())
				.forEach(x->System.out.println(x.toString()));
			
			aclazz.newInstance();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		//JavacTaskImpl
		System.out.println(CLASS_PATTERN.matcher("class A ").matches());
					
	}
	static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");
}

//jls8 9.6 Annotation Types 两个独立的tokens（distinct tokens）他们之前可包含空格，但不建议这么做。
@ interface AAA {
	
}

enum BBB implements Serializable{
	
}
