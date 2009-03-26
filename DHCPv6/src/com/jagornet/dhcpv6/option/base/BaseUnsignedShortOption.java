/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedShortOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OpaqueDataOptionType;
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.UnsignedShortOptionType;

/**
 * <p>Title: BaseUnsignedShortOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedShortOption extends BaseDhcpOption implements DhcpComparableOption
{ 
	private static Logger log = LoggerFactory.getLogger(BaseUnsignedShortOption.class);

    /** The unsigned short option. */
    protected UnsignedShortOptionType uShortOption;
    
    /**
     * Instantiates a new unsigned short option.
     */
    public BaseUnsignedShortOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new unsigned short option.
     * 
     * @param uShortOption the elapsed time option
     */
    public BaseUnsignedShortOption(UnsignedShortOptionType uShortOption)
    {
        super();
        if (uShortOption != null)
            this.uShortOption = uShortOption;
        else
            this.uShortOption = UnsignedShortOptionType.Factory.newInstance();
    }

    /**
     * Gets the elapsed time option.
     * 
     * @return the elapsed time option
     */
    public UnsignedShortOptionType getUnsignedShortOption()
    {
        return uShortOption;
    }

    /**
     * Sets the elapsed time option.
     * 
     * @param uShortOption the new elapsed time option
     */
    public void setUnsignedShortOption(UnsignedShortOptionType uShortOption)
    {
        if (uShortOption != null)
            this.uShortOption = uShortOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        return 2;   // always two bytes (short)
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putShort((short)uShortOption.getUnsignedShort());
        return iobuf.flip().buf();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(ByteBuffer buf) throws IOException
    {
    	IoBuffer iobuf = IoBuffer.wrap(buf);
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
    		uShortOption.setUnsignedShort(iobuf.getUnsignedShort());
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpComparableOption#matches(com.jagornet.dhcpv6.xml.OptionExpression)
     */
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;
        if (uShortOption == null)
        	return false;

        int myUshort = uShortOption.getUnsignedShort();
        
        UnsignedShortOptionType that = expression.getUShortOption();
        if (that != null) {
        	int ushort = that.getUnsignedShort();
        	Operator.Enum op = expression.getOperator();
        	if (op.equals(Operator.EQUALS)) {
        		return (myUshort == ushort);
        	}
        	else if (op.equals(Operator.LESS_THAN)) {
        		return (myUshort < ushort);
        	}
        	else if (op.equals(Operator.LESS_THAN_OR_EQUAL)) {
        		return (myUshort <= ushort);
        	}
        	else if (op.equals(Operator.GREATER_THAN)) {
        		return (myUshort > ushort);
        	}
        	else if (op.equals(Operator.GREATER_THAN_OR_EQUAL)) {
        		return (myUshort >= ushort);
        	}
            else {
            	log.warn("Unsupported expression operator: " + op);
            }
        }
        
        // then see if we have an opaque option
        OpaqueDataOptionType opaqueOption = expression.getOpaqueDataOption();
        if (opaqueOption != null) {
	        OpaqueData opaque = opaqueOption.getOpaqueData();
	        if (opaque != null) {
	            String ascii = opaque.getAsciiValue();
	            if (ascii != null) {
	                try {
	                	// need an Integer to handle unsigned short
	                    if (uShortOption.getUnsignedShort() == Integer.parseInt(ascii)) {
	                        return true;
	                    }
	                }
	                catch (NumberFormatException ex) { }
	            }
	            else {
	                byte[] hex = opaque.getHexValue();
	                if ( (hex != null) && 
	                     (hex.length >= 1) && (hex.length <= 2) ) {
	                	int hexUnsignedShort = Integer.valueOf(Util.toHexString(hex), 16);
	                    if (uShortOption.getUnsignedShort() == hexUnsignedShort) {
	                        return true;
	                    }
	                }
	            }
	        }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append(uShortOption.getUnsignedShort());
        return sb.toString();
    }
    
}
