package org.summercool.hsf.test.mock.service;

import org.summercool.hsf.annotation.RemoteServiceContract;

@RemoteServiceContract
public interface ServerService {
	String callServer(String txt);
}
