/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestClient.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpElapsedTimeOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.server.netty.DhcpChannelDecoder;
import com.jagornet.dhcpv6.server.netty.DhcpChannelEncoder;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * A test client that sends request messages to a DHCPv6 server
 * via either unicast or multicast.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class TestClient extends SimpleChannelUpstreamHandler
{
	private static Logger log = LoggerFactory.getLogger(TestClient.class);

    protected Options options = new Options();
    protected CommandLineParser parser = new BasicParser();
    protected HelpFormatter formatter;
    
    protected NetworkInterface mcastNetIf = null;
    protected InetAddress serverAddr = DhcpConstants.LOCALHOST_V6;
    protected int serverPort = DhcpConstants.SERVER_PORT;
    protected int clientPort = DhcpConstants.CLIENT_PORT;
    protected boolean rapidCommit = false;
    protected int numRequests = 100;
    protected AtomicInteger solicitsSent = new AtomicInteger();
    protected AtomicInteger advertsReceived = new AtomicInteger();
    protected AtomicInteger requestsSent = new AtomicInteger();
    protected AtomicInteger repliesReceived = new AtomicInteger();
    protected int successCnt = 0;
    protected long startTime = 0;
    protected long endTime = 0;

    protected InetSocketAddress server = null;
    protected InetSocketAddress client = null;

    protected DatagramChannel channel = null;
	protected ExecutorService executor = Executors.newCachedThreadPool();
    
    /** The request map. */
    protected Map<Integer, DhcpMessage> solicitMap =
    	Collections.synchronizedMap(new HashMap<Integer, DhcpMessage>());
    
    /** The request map. */
    protected Map<Integer, DhcpMessage> requestMap =
    	Collections.synchronizedMap(new HashMap<Integer, DhcpMessage>());

    /**
     * Instantiates a new test client.
     * 
     * @param args the args
     */
    public TestClient(String[] args) 
    {
        setupOptions();

        if(!parseOptions(args)) {
            formatter = new HelpFormatter();
            String cliName = this.getClass().getName();
            formatter.printHelp(cliName, options);
//            PrintWriter stderr = new PrintWriter(System.err, true);	// auto-flush=true
//            formatter.printHelp(stderr, 80, cliName, null, options, 2, 2, null);
            System.exit(0);
        }
        
        try {
			start();

		} 
        catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
	/**
	 * Setup options.
	 */
	private void setupOptions()
    {
		Option numOption = new Option("n", "number", true,
										"Number of client requests to send" +
										" [" + numRequests + "]");
		options.addOption(numOption);
		
        Option addrOption = new Option("a", "address", true,
        								"Address of DHCPv6 Server" +
        								" [" + serverAddr.getHostAddress() + "]");		
        options.addOption(addrOption);

        Option mcastOption = new Option("m", "multicast", true,
									   "Multicast interface [none]");
        options.addOption(mcastOption);

        Option cpOption = new Option("cp", "clientport", true,
        							  "Client Port Number" +
        							  " [" + clientPort + "]");
        options.addOption(cpOption);

        Option spOption = new Option("sp", "serverport", true,
        							  "Server Port Number" +
        							  " [" + serverPort + "]");
        options.addOption(spOption);
        
        Option rOption = new Option("r", "rapidcommit", false,
        							"Send rapid-commit Solicit requests");
        options.addOption(rOption);

        Option helpOption = new Option("?", "help", false, "Show this help page.");
        
        options.addOption(helpOption);
    }

    /**
     * Parses the options.
     * 
     * @param args the args
     * 
     * @return true, if successful
     */
    protected boolean parseOptions(String[] args)
    {
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("?")) {
                return false;
            }
            if (cmd.hasOption("n")) {
            	String n = cmd.getOptionValue("n");
            	try {
            		numRequests = Integer.parseInt(n);
            	}
            	catch (NumberFormatException ex) {
            		numRequests = 100;
            		System.err.println("Invalid number of requests: '" + n +
            							"' using default: " + numRequests +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("a")) {
            	String a = cmd.getOptionValue("a");
            	try {
            		serverAddr = InetAddress.getByName(a);
            	}
            	catch (UnknownHostException ex) {
            		serverAddr = DhcpConstants.LOCALHOST_V6;
            		System.err.println("Invalid address: '" + a +
            							"' using default: " + serverAddr +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("m")) {
            	String m = cmd.getOptionValue("m");
            	try {
            		mcastNetIf = NetworkInterface.getByName(m);
            	}
            	catch (SocketException ex) {
            		System.err.println("Invalid interface: " + m +
            							" - " + ex);
            	}
            }
            if (cmd.hasOption("cp")) {
            	String p = cmd.getOptionValue("cp");
            	try {
            		clientPort = Integer.parseInt(p);
            	}
            	catch (NumberFormatException ex) {
            		clientPort = DhcpConstants.CLIENT_PORT;
            		System.err.println("Invalid client port number: '" + p +
            							"' using default: " + clientPort +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("sp")) {
            	String p = cmd.getOptionValue("sp");
            	try {
            		serverPort = Integer.parseInt(p);
            	}
            	catch (NumberFormatException ex) {
            		serverPort = DhcpConstants.SERVER_PORT;
            		System.err.println("Invalid server port number: '" + p +
            							"' using default: " + serverPort +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("r")) {
            	rapidCommit = true;
            }
        }
        catch (ParseException pe) {
            System.err.println("Command line option parsing failure: " + pe);
            return false;
		}
        return true;
    }
    
    /**
     * Start sending DHCPv6 INFORM_REQUESTs.
     */
    public void start()
    {
    	DatagramChannelFactory factory = null;
    	if (mcastNetIf != null) {
    		factory = new OioDatagramChannelFactory(Executors.newCachedThreadPool());
    		serverAddr = DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS;
    	}
    	else {
    		factory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
    	}
    	
    	server = new InetSocketAddress(serverAddr, serverPort);
    	client = new InetSocketAddress(clientPort);
    	
		ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("logger", new LoggingHandler());
        pipeline.addLast("encoder", new DhcpChannelEncoder());
        pipeline.addLast("decoder", new DhcpChannelDecoder(client, false));
        pipeline.addLast("handler", this);
    	
    	if (mcastNetIf != null) {
            channel = factory.newChannel(pipeline);
    		channel.getConfig().setNetworkInterface(mcastNetIf);
    	}
    	else {
            channel = factory.newChannel(pipeline);
    	}
    	channel.bind(client);

    	List<DhcpMessage> solicitMsgs = buildSolicitMessages();
    	
    	for (DhcpMessage msg : solicitMsgs) {
    		executor.execute(new RequestSender(msg, server));
    	}

    	long ms = 0;
		if (rapidCommit)
			ms = solicitMsgs.size()*200;
		else
			ms = solicitMsgs.size()*20;

    	synchronized (requestMap) {
        	try {
        		log.info("Waiting total of " + ms + " milliseconds for completion");
//        		requestMap.wait(ms);
        		requestMap.wait();
        	}
        	catch (InterruptedException ex) {
        		log.error("Interrupted", ex);
        	}
		}

		log.info("Complete: solicitsSent=" + solicitsSent +
				" advertsReceived=" + advertsReceived +
				" requestsSent=" + requestsSent +
				" repliesReceived=" + repliesReceived +
				" elapsedTime = " + (endTime - startTime) + " milliseconds");

    	log.info("Shutting down executor...");
    	executor.shutdownNow();
    	log.info("Closing channel...");
    	channel.close();
    	log.info("Done.");
    	System.exit(0);
    }

    /**
     * The Class RequestSender.
     */
    class RequestSender implements Runnable, ChannelFutureListener
    {
    	
	    /** The msg. */
	    DhcpMessage msg;
    	
	    /** The server. */
	    InetSocketAddress server;
    	
    	/**
	     * Instantiates a new request sender.
	     *
	     * @param msg the msg
	     * @param server the server
	     */
	    public RequestSender(DhcpMessage msg, InetSocketAddress server)
    	{
    		this.msg = msg;
    		this.server = server;
    	}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			ChannelFuture future = channel.write(msg, server);
			future.addListener(this);
		}
		
		/* (non-Javadoc)
		 * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
		 */
		@Override
		public void operationComplete(ChannelFuture future) throws Exception
		{
			int id = msg.getTransactionId();
			if (future.isSuccess()) {
				if (startTime == 0) {
					startTime = System.currentTimeMillis();
				}
				if (msg.getMessageType() == DhcpConstants.SOLICIT) {
					solicitsSent.getAndIncrement();
					log.info("Succesfully sent solicit message id=" + id + 
							", solicitsSent=" + solicitsSent);
					solicitMap.put(id, msg);
				}
				else if (msg.getMessageType() == DhcpConstants.REQUEST) {
					requestsSent.getAndIncrement();
					log.info("Succesfully sent request message id=" + id +
							", requestsSent=" + requestsSent);
					requestMap.put(id, msg);
				}
			}
			else {
				log.warn("Failed to send message id=" + msg.getTransactionId());
			}
		}
    }
    
    /**
     * Builds the solicit messages.
     * 
     * @return the list< dhcp message>
     */
    private List<DhcpMessage> buildSolicitMessages()
    {
    	List<DhcpMessage> requests = new ArrayList<DhcpMessage>();   	
        for (int id=0; id<numRequests; id++) {
            DhcpMessage msg = 
            	new DhcpMessage(null, new InetSocketAddress(serverAddr, serverPort));

            msg.setTransactionId(id);
            String clientId = "clientid-" + id;
            DhcpClientIdOption dhcpClientId = new DhcpClientIdOption();
            dhcpClientId.getOpaqueData().setAscii(clientId);
            
            msg.putDhcpOption(dhcpClientId);
            
            DhcpElapsedTimeOption dhcpElapsedTime = new DhcpElapsedTimeOption();
            dhcpElapsedTime.setUnsignedShort(1);
            msg.putDhcpOption(dhcpElapsedTime);
            
        	msg.setMessageType(DhcpConstants.SOLICIT);
            DhcpIaNaOption dhcpIaNa = new DhcpIaNaOption();
            dhcpIaNa.setIaId(1);
            msg.putDhcpOption(dhcpIaNa);
            
            requests.add(msg);
        }
        return requests;
    }
    
    private DhcpMessage buildRequestMessage(DhcpMessage advert) {
    	
        DhcpMessage msg = 
            	new DhcpMessage(null, new InetSocketAddress(serverAddr, serverPort));

        int xid = advert.getTransactionId();
        msg.setTransactionId(xid);
        
        String clientId = "clientid-" + xid;
        DhcpClientIdOption dhcpClientId = new DhcpClientIdOption();
        dhcpClientId.getOpaqueData().setAscii(clientId);
        
        msg.putDhcpOption(dhcpClientId);
        
        DhcpElapsedTimeOption dhcpElapsedTime = new DhcpElapsedTimeOption();
        dhcpElapsedTime.setUnsignedShort(1);
        msg.putDhcpOption(dhcpElapsedTime);
        
    	msg.setMessageType(DhcpConstants.REQUEST);

    	DhcpServerIdOption dhcpServerId = 
    			(DhcpServerIdOption)advert.getDhcpOption(DhcpConstants.OPTION_SERVERID);
    	msg.putDhcpOption(dhcpServerId);
    	DhcpIaNaOption dhcpIaNa = (DhcpIaNaOption)advert.getDhcpOption(DhcpConstants.OPTION_IA_NA);
        msg.putDhcpOption(dhcpIaNa);
        
        return msg;
    }


	/*
	 * (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
    	Object message = e.getMessage();
        if (message instanceof DhcpMessage) {
            
            DhcpMessage dhcpMessage = (DhcpMessage) message;
            if (log.isDebugEnabled())
            	log.debug("Received: " + dhcpMessage.toStringWithOptions());
            else
            	log.info("Received: " + dhcpMessage.toString());

            if (dhcpMessage.getMessageType() == DhcpConstants.ADVERTISE) {
	            DhcpMessage solicitMessage = solicitMap.remove(dhcpMessage.getTransactionId());
	            if (solicitMessage != null) {
	            	advertsReceived.getAndIncrement();
	            	log.info("Removed message from solicit map: cnt=" + advertsReceived);
	            	synchronized (solicitMap) {
	            		if (solicitMap.isEmpty()) {
	            			solicitMap.notify();
	            		}
	            		else {
	            			log.debug("Solicit map size: " + solicitMap.size());
	            		}
	            	}
	            	// queue the request message now
	            	executor.execute(new RequestSender(buildRequestMessage(dhcpMessage), server));
	            }
	            else {
	            	log.error("Message not found in solicit map: xid=" + dhcpMessage.getTransactionId());
	            }
            }
            else if (dhcpMessage.getMessageType() == DhcpConstants.REPLY) {
	            DhcpMessage requestMessage = requestMap.remove(dhcpMessage.getTransactionId());
	            if (requestMessage != null) {
	            	repliesReceived.getAndIncrement();
	            	log.info("Removed message from request map: cnt=" + repliesReceived);
	            	endTime = System.currentTimeMillis();
	            	synchronized (requestMap) {
	            		if (requestMap.isEmpty()) {
	            			requestMap.notify();
	            		}
	            		else {
	            			log.debug("Request map size: " + requestMap.size());
	            		}
	            	}
	            }
	            else {
	            	log.error("Message not found in request map: xid=" + dhcpMessage.getTransactionId());
	            }
            }
            else {
            	log.warn("Received unhandled message type: " + dhcpMessage.getMessageType());
            }
        }
        else {
            // Note: in theory, we can't get here, because the
            // codec would have thrown an exception beforehand
            log.error("Received unknown message object: " + message.getClass());
        }
    }
	 
	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
	{
    	log.error("Exception caught: ", e.getCause());
    	e.getChannel().close();
	}
    
    /**
     * The main method.
     * 
     * @param args the arguments
     */
    public static void main(String[] args) {
        new TestClient(args);
    }

}
