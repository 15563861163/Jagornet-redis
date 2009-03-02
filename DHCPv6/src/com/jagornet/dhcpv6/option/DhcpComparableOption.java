/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpComparableOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option;

import com.jagornet.dhcpv6.xml.OptionExpression;

/**
 * Title: DhcpComparableOption
 * Description: The Interface for options that can be compared using
 * an OptionExpression.
 * 
 * @author A. Gregory Rabil
 */
public interface DhcpComparableOption
{
    /**
     * Test if this DHCP option matches an OptionExpression.
     * 
     * @param expression the OptionExpression to use for comparing the option
     * 
     * @return true, if successful
     */
    public abstract boolean matches(OptionExpression expression);
}
