package com.dubbo05;

import java.io.PrintStream;

public class SystemOut implements Runnable{

	public static void main(String[] args) throws InterruptedException {
//		PrintStream p1=	System.out;
//		PrintStream p2=	System.out;
//		
//		assert p1==p2;
		
		SystemOut so=new SystemOut();
		Thread t=new Thread(so);
		t.start();
		
		so.m2();
		
		System.out.println("main thread b=" + so.b); //4
		
		
	}
	
	int b = 100;   
	synchronized void m1() throws InterruptedException {
		b=1000;
		Thread.sleep(500);
		System.out.println("b="+b);
	}
	
	synchronized void m2() throws InterruptedException {
		Thread.sleep(250);
		b=2000;
	}

	@Override
	public void run() {
		
		try {
			m1();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
