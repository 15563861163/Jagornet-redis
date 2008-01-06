package com.agr.dhcpv6.server.mina;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import com.agr.dhcpv6.message.DhcpMessage;
import com.agr.dhcpv6.message.DhcpRelayMessage;
import com.agr.dhcpv6.option.DhcpRelayOption;
import com.agr.dhcpv6.server.DhcpInfoRequestProcessor;
import com.agr.dhcpv6.util.DhcpConstants;

public class MinaDhcpHandler extends IoHandlerAdapter
{
    private static Log log = LogFactory.getLog(MinaDhcpHandler.class);
    
    private MinaDhcpServer server;

    public MinaDhcpHandler(MinaDhcpServer server)
    {
        this.server = server;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception 
    {
        log.error("Session exception caught", cause);
        session.close();
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception 
    {
        SocketAddress remoteAddress = session.getRemoteAddress();
        if (message instanceof DhcpMessage) {
            
            DhcpMessage dhcpMessage = (DhcpMessage) message;
            server.recvUpdate(remoteAddress, dhcpMessage);

            DhcpMessage replyMessage = null;
            if (dhcpMessage.getMessageType() == DhcpConstants.INFO_REQUEST) {
                replyMessage = 
                    handleInfoRequest(((InetSocketAddress)session.getLocalAddress()).getAddress(),
                                       dhcpMessage);
            }
            else if (dhcpMessage.getMessageType() == DhcpConstants.RELAY_FORW) {
                if (message instanceof DhcpRelayMessage) {
                    DhcpRelayMessage relayMessage = (DhcpRelayMessage) dhcpMessage;
                    replyMessage = handleRelayFoward(relayMessage);
                }
                else {
                    // Note: in theory, we can't get here, because the
                    // codec would have thrown an exception beforehand
                    log.error("Received unknown relay message object: " + 
                              message.getClass());
                }
            }
            else {
                log.warn("Ignoring unsupported message type: " + 
                         DhcpConstants.getMessageString(dhcpMessage.getMessageType()));
            }
            
            if (replyMessage != null) {
                // do we really want to write to the remoteAddress
                // from the session - i.e. is this really just going
                // to send the reply message to the client or relay
                // that sent or forwarded the request?
                session.write(replyMessage, remoteAddress);
            }
            else {
                log.warn("Null DHCP reply message returned from processor");
            }
            
        }
        else {
            // Note: in theory, we can't get here, because the
            // codec would have thrown an exception beforehand
            log.error("Received unknown message object: " + message.getClass());
        }
    }
    
    private DhcpMessage handleInfoRequest(InetAddress linkAddr, 
                                          DhcpMessage dhcpMessage)
    {
        DhcpInfoRequestProcessor processor = 
            new DhcpInfoRequestProcessor(linkAddr, dhcpMessage);
        
        DhcpMessage reply = processor.process();
        return reply;
    }
    
    private DhcpMessage handleRelayFoward(DhcpRelayMessage relayMessage)
    {
        InetAddress linkAddr = relayMessage.getLinkAddress();
        DhcpRelayOption relayOption = relayMessage.getRelayOption();
        if (relayOption != null) {
            DhcpMessage relayOptionMessage = relayOption.getRelayMessage();
            while (relayOptionMessage != null) {
                // check what kind of message is in the option
                if (relayOptionMessage instanceof DhcpRelayMessage) {
                    // encapsulated message is another relay message
                    DhcpRelayMessage anotherRelayMessage = 
                        (DhcpRelayMessage)relayOptionMessage; 
                    // reset the client link reference
                    linkAddr = anotherRelayMessage.getLinkAddress();
                    // reset the current relay option reference to the
                    // encapsulated relay message's relay option
                    relayOption = anotherRelayMessage.getRelayOption();
                    // reset the relayOptionMessage reference to recurse
                    relayOptionMessage = relayOption.getRelayMessage();
                }
                else {
                    // we've peeled off all the layers of the relay message(s),
                    // so now go handle the Info-Request, assuming it is one
                    if (relayOptionMessage.getMessageType() == DhcpConstants.INFO_REQUEST) {
                        DhcpMessage replyMessage = 
                            handleInfoRequest(linkAddr, relayOptionMessage);
                        if (replyMessage != null) {
                            // replace the original Info-Request message inside
                            // the relayed message with the generated Reply message
                            relayOption.setRelayMessage(replyMessage);
                            // return the relay message we started with, 
                            // which should look the same except the lowest 
                            // level message should be our reply
                            return relayMessage;
                        }
                    }
                    else {
                        log.error("Lowest level message in relay message is not an Info-Request");
                    }
                    relayOptionMessage = null;  // done with relayed messages
                }
            }
        }
        else {
            log.error("Relay message does not contain a relay option");
        }
        // if we get here, no reply was generated
        return null;
    }
}
