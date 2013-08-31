package org.summercool.hsf.test.phone;

import org.summercool.hsf.annotation.RemoteServiceContract;

/**
 * @Title: PhoneService.java
 * @Package org.summercool.hsf.test.phone
 * @date 2012-3-20 上午12:07:10
 * @version V1.0
 */
@RemoteServiceContract
public interface PhoneService {
	public byte[] doExecute(byte[] msg);
}
