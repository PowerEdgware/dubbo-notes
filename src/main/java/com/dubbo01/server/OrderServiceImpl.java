package com.dubbo01.server;

import com.dubbo01.api.IOrderService;

public class OrderServiceImpl implements IOrderService {

	@Override
	public String order(String accNum, int price) {
		if (price > 0) {
			return "accNum=" + accNum + " order suc." + price;
		}
		return "accNum=" + accNum + " order failed." + price;
	}

}
