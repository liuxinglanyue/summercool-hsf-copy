package org.summercool.hsf.test.mock.service;

public class ClientServiceImpl implements ClientService {

	@Override
	public String callClient(String txt) {
		return "callClient " + txt;
	}

}
