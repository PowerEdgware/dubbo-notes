package com.custome;

import org.apache.dubbo.common.extension.SPI;

@SPI("spifilter")//default extension name
public interface IFilter {

	void doFilter();
}
