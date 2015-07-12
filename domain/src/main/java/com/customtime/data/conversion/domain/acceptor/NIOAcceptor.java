package com.customtime.data.conversion.domain.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.domain.acceptor.command.Command;
import com.customtime.data.conversion.domain.context.ResourceContext;
import com.customtime.data.conversion.plugin.annotaion.TMProperty;

public class NIOAcceptor implements Acceptor {
	private static final Log logger = LogFactory.getLog(NIOAcceptor.class);
	
	private Selector selector;
	@TMProperty(defaultValue="7914")
	private int port;
	@TMProperty(defaultValue="1024")
	private int bufferSize;
	private ByteBuffer byteBuffer;
	private CharsetDecoder decoder;
	private CharsetEncoder encoder;
	@TMProperty(defaultValue="UTF8")
	private String charSet;
	
	public NIOAcceptor init() throws IOException{
		byteBuffer = ByteBuffer.allocate(bufferSize);
		decoder = Charset.forName(charSet).newDecoder();
		encoder = Charset.forName(charSet).newEncoder();
		selector = Selector.open(); 
		ServerSocketChannel server = ServerSocketChannel.open();  
	    server.configureBlocking(false);  
	    ServerSocket socket = server.socket();  
	    InetSocketAddress address = new InetSocketAddress(port);  
	    socket.bind(address);  
	    server.register(selector, SelectionKey.OP_ACCEPT);  
	    return this;
	}

	public List<Command> receive() {
		List<Command> commands = new ArrayList<Command>();
		try {
			selector.select();
			Set<SelectionKey> keys = selector.selectedKeys(); 
			Iterator<SelectionKey> iter = keys.iterator();
			while(iter.hasNext()){
				SelectionKey key = iter.next();
				iter.remove();
				if (key.isAcceptable()) {  
					ServerSocketChannel server = (ServerSocketChannel) key.channel();  
					SocketChannel channel = server.accept();  
					channel.configureBlocking(false);  
					channel.register(selector, SelectionKey.OP_READ);  
			    }else if(key.isReadable()){
			    	SocketChannel channel = (SocketChannel) key.channel();
			    	if(channel.isConnected()){
			    		try {
							byteBuffer.clear();
							channel.read(byteBuffer);
							byteBuffer.flip();
							String readString = decoder.decode(byteBuffer).toString();
							logger.info(readString);
							if(EXIT.equals(readString)){
								selector.close();
								System.exit(0);
							}
							Command cd = ResourceContext.getCommand();
							if(cd.init(readString)){
								channel.write(encoder.encode(CharBuffer.wrap("the plan with filename :"+cd.getProcessPlanPath()+" is excute!")));
								commands.add(cd);
							}
						} catch (Exception e) {
							e.printStackTrace();
							if(channel!=null)
								channel.close();
						}
			    	}else{
			    		channel.close();
			    	}
			    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return commands;
	}

}
