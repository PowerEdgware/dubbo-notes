package com.test;

public class StringTest {

	public static void main(String[] args) {
		// StringBuilder
		String s2 = "1";
		String s1 = new String("1");
		s1.intern();

		System.out.println(s1 == s2);

		String str1 = "aaa";

		String str2 = "bbb";

		String str3 = "aaabbb";

		String str4 = str1 + str2;
		String str5 = "aaa" + "bbb";
		System.out.println(str3 == str4);

		System.out.println(str3 == str4.intern()); //
		System.out.println(str3 == str5);// true or false

	}
}
