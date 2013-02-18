/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4ChannelDecoder.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.netty;

import java.net.InetSocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpV4Message;
import com.jagornet.dhcpv6.server.DhcpV6Server;

/**
 * Title: DhcpV4ChannelDecoder
 * Description: The protocol decoder used by the NETTY-based DHCPv4 server
 * when receiving packets.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV4ChannelDecoder extends OneToOneDecoder
{
    
    /** The log. */
    private static Logger log = LoggerFactory.getLogger(DhcpV4ChannelDecoder.class);

    /** The local socket address. */
    protected InetSocketAddress localSocketAddress = null;
    
    /** The remote socket address. */
    protected InetSocketAddress remoteSocketAddress = null;
    
    public DhcpV4ChannelDecoder(InetSocketAddress localSocketAddress)
    {
    	this.localSocketAddress = localSocketAddress;
    }
    
    /*
     * Decodes a received ChannelBuffer into a DhcpMessage.
     * (non-Javadoc)
     * @see org.jboss.netty.handler.codec.oneone.OneToOneDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, java.lang.Object)
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
    {    	
    	if (DhcpV6Server.getAllIPv4Addrs().contains(remoteSocketAddress.getAddress())) {
    		log.debug("Ignoring packet from self: address=" + 
    					remoteSocketAddress.getAddress());
    		return null;
    	}
    	
        if (msg instanceof ChannelBuffer) {
        	ChannelBuffer buf = (ChannelBuffer) msg;
            DhcpV4Message dhcpMessage =  
            	DhcpV4Message.decode(buf.toByteBuffer(), localSocketAddress, remoteSocketAddress);
            return dhcpMessage;
        }
        else {
            String errmsg = "Unknown message object class: " + msg.getClass();
            log.error(errmsg);
            return msg;
        }
    }
    
    /* (non-Javadoc)
     * @see org.jboss.netty.handler.codec.oneone.OneToOneDecoder#handleUpstream(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelEvent)
     */
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent evt) throws Exception
    {
    	if (evt instanceof MessageEvent) {
    		remoteSocketAddress = (InetSocketAddress) ((MessageEvent)evt).getRemoteAddress();
    	}
    	super.handleUpstream(ctx, evt);
    }
}
