package com.customtime.data.conversion.domain.acceptor;

import java.io.IOException;
import java.util.List;

import com.customtime.data.conversion.domain.acceptor.command.Command;

public interface Acceptor {
	
	public static final String EXIT = "exitTE";
	public List<Command> receive();
	public Acceptor init()throws IOException;
	
}
