package com.jagornet.dhcpv6.server.request;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.V6ClientIdOption;
import com.jagornet.dhcpv6.db.BaseTestCase;
import com.jagornet.dhcpv6.message.DhcpV6Message;
import com.jagornet.dhcpv6.message.DhcpV6RelayMessage;
import com.jagornet.dhcpv6.message.TestDhcpRelayMessage;
import com.jagornet.dhcpv6.option.DhcpV6ClientIdOption;
import com.jagornet.dhcpv6.option.DhcpV6InterfaceIdOption;
import com.jagornet.dhcpv6.option.DhcpV6RelayOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

public class TestDhcpV6MessageHandler extends BaseTestCase
{
	
	public void testHandleMessage() throws Exception
	{
		DhcpV6RelayMessage relayMessage = TestDhcpRelayMessage.buildMockDhcpRelayMessage();
		
		// change the mock relay_reply message to a relay_forward
		relayMessage.setMessageType(DhcpConstants.RELAY_FORW);
		
        DhcpV6Message dhcpMessage = 
        	new DhcpV6Message(new InetSocketAddress("2001:db8:1::1", DhcpConstants.SERVER_PORT),
        			new InetSocketAddress("fe80::1", DhcpConstants.CLIENT_PORT));
        dhcpMessage.setMessageType(DhcpConstants.INFO_REQUEST);    // 1 byte
        dhcpMessage.setTransactionId(90599);                // 3 bytes

        OpaqueData opaque = OpaqueData.Factory.newInstance();
        opaque.setAsciiValue("jagornet-dhcpv6");	// 15 bytes

        V6ClientIdOption clientId = V6ClientIdOption.Factory.newInstance();
        clientId.setOpaqueData(opaque);
        dhcpMessage.putDhcpOption(new DhcpV6ClientIdOption(clientId));
        
        // replace the reply message with a client info-request message
        DhcpV6RelayOption relayOption = relayMessage.getRelayOption();
        relayOption.setDhcpMessage(dhcpMessage);
        relayMessage.setRelayOption(relayOption);
        
		relayMessage.putDhcpOption(TestDhcpRelayMessage.buildMockInterfaceIdOption());
		DhcpV6Message replyMessage = 
			DhcpV6MessageHandler.handleMessage(InetAddress.getByName("3ffe::1"), relayMessage);
		DhcpV6InterfaceIdOption ifIdOption = 
			(DhcpV6InterfaceIdOption) replyMessage.getDhcpOption(DhcpConstants.OPTION_INTERFACE_ID);
		assertNotNull(ifIdOption);
	}
}
