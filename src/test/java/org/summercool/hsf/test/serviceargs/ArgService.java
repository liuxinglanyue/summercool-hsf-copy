package org.summercool.hsf.test.serviceargs;

import java.util.List;

import org.summercool.hsf.annotation.RemoteServiceContract;

/**
 * @Description: TODO
 * @author Kolor
 * @date 2012-5-8 下午5:59:25
 */
@RemoteServiceContract
public interface ArgService {
	public String testArgs(List<String> list,List<String> list2,List<String> list3,Object obj, boolean isBoy);
}
