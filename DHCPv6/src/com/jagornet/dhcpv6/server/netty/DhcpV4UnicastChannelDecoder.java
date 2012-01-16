/*
 * Copyright 2011 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpUnicastChannelDecoder.java is part of DHCPv6.
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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.jagornet.dhcpv6.message.DhcpV4Message;

/**
 * The Class DhcpUnicastChannelDecoder.
 */
public class DhcpV4UnicastChannelDecoder extends DhcpV4ChannelDecoder 
{
	
	/**
	 * Instantiates a new dhcp unicast channel decoder.
	 *
	 * @param localSocketAddress the local socket address
	 */
	public DhcpV4UnicastChannelDecoder(InetSocketAddress localSocketAddress)
	{
		super(localSocketAddress);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.netty.DhcpChannelDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, java.lang.Object)
	 */
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
	{
		Object obj = super.decode(ctx, channel, msg);
		if (obj instanceof DhcpV4Message) {
			// this decoder is in the pipeline for unicast
			// channels only, so this must be a unicast packet
			((DhcpV4Message)obj).setUnicast(true);
		}
		return obj;
	}
}
