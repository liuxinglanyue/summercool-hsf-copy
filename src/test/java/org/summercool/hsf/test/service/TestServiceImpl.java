package org.summercool.hsf.test.service;

/**
 * @Title: TestServiceImpl.java
 * @Description: TODO(添加描述)
 * @date 2012-2-23 上午01:00:45
 * @version V1.0
 */
public class TestServiceImpl implements TestService {

	@Override
	public String test(String ctx) {
		return String.valueOf("hello " + ctx);
	}
}
