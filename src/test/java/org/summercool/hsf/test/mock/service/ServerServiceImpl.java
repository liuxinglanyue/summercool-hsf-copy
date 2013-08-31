package org.summercool.hsf.test.mock.service;

public class ServerServiceImpl implements ServerService {

	@Override
	public String callServer(String txt) {
		return "callServer " + txt;
	}

}
