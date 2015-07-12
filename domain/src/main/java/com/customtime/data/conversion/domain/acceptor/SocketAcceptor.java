package com.customtime.data.conversion.domain.acceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.customtime.data.conversion.domain.acceptor.command.Command;
import com.customtime.data.conversion.domain.context.ResourceContext;
import com.customtime.data.conversion.plugin.annotaion.TMProperty;

public class SocketAcceptor implements Acceptor {
	@TMProperty(defaultValue="7914")
	private int port;
	private ServerSocket server;
	
	public Acceptor init() throws IOException {
		return this;
	}

	public SocketAcceptor()throws IOException{
		server=new ServerSocket(port);
	}

	public List<Command> receive() {
		List<Command> commands = new ArrayList<Command>();
		Socket socket = null;
		BufferedReader in = null;
		try {
			socket = server.accept();
			in=new BufferedReader(new InputStreamReader(socket.getInputStream())); 
			String recMessage = in.readLine();
			if(EXIT.equals(recMessage)){
				socket.close();
				System.exit(0);
			}
			Command cd = ResourceContext.getCommand();
	    	if(cd.init(recMessage)){
				commands.add(cd);
	    	}
			return commands;
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(in!=null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(socket!=null)
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return null;
	}

}
