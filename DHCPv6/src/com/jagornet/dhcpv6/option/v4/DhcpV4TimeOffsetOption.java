/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpElapsedTimeOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.base.BaseUnsignedIntOption;
import com.jagornet.dhcpv6.xml.V4TimeOffsetOption;

/**
 * <p>Title: DhcpV4TimeOffsetOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4TimeOffsetOption extends BaseUnsignedIntOption
{
	
	/**
	 * Instantiates a new dhcpv4 time offset option.
	 */
	public DhcpV4TimeOffsetOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcpv4 time offset option.
	 * 
	 * @param v4TimeOffsetOption the v4 time offset option
	 */
	public DhcpV4TimeOffsetOption(V4TimeOffsetOption v4TimeOffsetOption)
	{
		if (v4TimeOffsetOption != null)
			this.uIntOption = v4TimeOffsetOption;
		else
			this.uIntOption = V4TimeOffsetOption.Factory.newInstance();
		
		super.setV4(true);
	}
	
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return ((V4TimeOffsetOption)uIntOption).getCode();
    }
}
