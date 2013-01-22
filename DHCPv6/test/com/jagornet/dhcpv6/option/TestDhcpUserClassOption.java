/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpUserClassOption.java is part of DHCPv6.
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

import java.nio.ByteBuffer;
import java.util.List;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.ClientClassExpression;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.UserClassOption;

/**
 * The Class TestDhcpUserClassOption.
 */
public class TestDhcpUserClassOption extends TestCase
{
	protected DhcpUserClassOption duco;
	
	public TestDhcpUserClassOption()
	{
		duco = new DhcpUserClassOption();
        duco.addOpaqueData("UserClass 1");   // 13 (len=2 bytes, data=11 bytes)
        duco.addOpaqueData("UserClass 2");   // 13 (len=2 bytes, data=11 bytes)		
	}
	
    /**
     * Test encode.
     * 
     * @throws Exception the exception
     */
    public void testEncode() throws Exception
    {
        ByteBuffer bb = duco.encode();
        assertNotNull(bb);
        assertEquals(30, bb.capacity());    // +4 (code=2 bytes, len=2 bytes)
        assertEquals(30, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.OPTION_USER_CLASS, bb.getShort());
        assertEquals((short)26, bb.getShort());   // length
        assertEquals((short)11, bb.getShort());
    }

    /**
     * Test decode.
     * 
     * @throws Exception the exception
     */
    public void testDecode() throws Exception
    {
        // just 28 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(28);
        bb.putShort((short)26);     // length of option
        bb.putShort((short)11);     // length of "UserClass 1"
        bb.put("UserClass 1".getBytes());
        bb.putShort((short)11);
        bb.put("UserClass 2".getBytes());
        bb.flip();
        DhcpUserClassOption _duco = new DhcpUserClassOption();
        _duco.decode(bb);
        assertNotNull(_duco.getOpaqueDataListOptionType());
        List<OpaqueData> userClasses = _duco.getOpaqueDataListOptionType().getOpaqueDataList();
        assertNotNull(userClasses);
        assertEquals(2, userClasses.size());
        assertEquals("UserClass 1", userClasses.get(0).getAsciiValue());
        assertEquals("UserClass 2", userClasses.get(1).getAsciiValue());
    }
    
    public void testMatches() throws Exception
    {
        ClientClassExpression expression = ClientClassExpression.Factory.newInstance();
        assertFalse(duco.matches((DhcpUserClassOption)expression.getUserClassOption(), Operator.EQUALS));
        DhcpUserClassOption _duco = new DhcpUserClassOption();
        expression.setUserClassOption((UserClassOption)_duco.getOpaqueDataListOptionType());
        assertFalse(duco.matches(_duco, Operator.EQUALS));
        _duco.addOpaqueData("UserClass 1");
        _duco.addOpaqueData("UserClass 2");
        assertTrue(duco.matches(_duco, Operator.EQUALS));
    }
    
    /**
     * Test to string.
     */
    public void testToString()
    {
        System.out.println(duco);
    }
}
