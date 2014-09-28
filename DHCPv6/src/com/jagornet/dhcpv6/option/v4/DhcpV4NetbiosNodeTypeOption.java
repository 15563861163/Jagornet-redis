/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4NetbiosNodeTypeOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.v4;

import com.jagornet.dhcpv6.option.base.BaseUnsignedByteOption;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcp.xml.V4NetbiosNodeTypeOption;

/**
 * <p>Title: DhcpV4NetbiosNodeTypeOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4NetbiosNodeTypeOption extends BaseUnsignedByteOption
{
	/**
	 * Instantiates a new dhcpv4 netbios node type option.
	 */
	public DhcpV4NetbiosNodeTypeOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcpv4 netbios node type option.
	 * 
	 * @param v4NetbiosNodeTypeOption the v4 netbios node type option
	 */
	public DhcpV4NetbiosNodeTypeOption(V4NetbiosNodeTypeOption v4NetbiosNodeTypeOption)
	{
		super(v4NetbiosNodeTypeOption);
		setCode(DhcpConstants.V4OPTION_NETBIOS_NODE_TYPE);
		setV4(true);
	}
}
