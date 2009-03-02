/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file ShortOptionDTO.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.dto;

/**
 * The abstract class ShortOptionDTO.
 * 
 * @author A. Gregory Rabil
 */
public abstract class ShortOptionDTO extends BaseOptionDTO
{
    /** The value. */
    protected Short value;

    /**
     * Gets the value.
     * 
     * @return the value
     */
    public Short getValue()
    {
        return getShortValue();
    }

    /**
     * Gets the short value.
     * 
     * @return the short value
     */
    public Short getShortValue()
    {
    	return value;
    }
    
    /**
     * Sets the value.
     * 
     * @param value the new value
     */
    public void setValue(Short value)
    {
        setShortValue(value);
    }

    /**
     * Sets the short value.
     * 
     * @param value the new short value
     */
    public void setShortValue(Short value)
    {
    	this.value = value;
    }
}
