package com.customtime.data.conversion.test.acceptor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AcceptorTest {
	
	private static final Log logger = LogFactory.getLog(AcceptorTest.class);

//	@Test
	public void testSocketSend() throws Exception {
		Socket server = new Socket(InetAddress.getLocalHost(), 7914);
		PrintWriter out = new PrintWriter(server.getOutputStream());
		out.println("defaultCommand");
		out.flush();
		out.close();
		server.close();
	}
	
//	@Test
	public void testNIOAcceptor()throws Exception{
		BufferedReader wt = new BufferedReader(new InputStreamReader(System.in));
		CharsetEncoder encoder = Charset.forName("GB2312").newEncoder();
		SocketChannel client = SocketChannel.open();
		client.configureBlocking(false);
		client.connect(new InetSocketAddress("localhost",7914));
		if(client.isConnectionPending())
			client.finishConnect();
		while(true){
			String str = wt.readLine();
			logger.info(str);
			client.write(encoder.encode(CharBuffer.wrap(str)));
		}
//		client.close();
	}
}
