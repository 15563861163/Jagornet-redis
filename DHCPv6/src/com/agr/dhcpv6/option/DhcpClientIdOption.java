package com.agr.dhcpv6.option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.server.config.xml.ClientIdOption;
import com.agr.dhcpv6.server.config.xml.OpaqueData;

/**
 * <p>Title: DhcpClientIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpClientIdOption extends BaseOpaqueDataOption
{
	private static Logger log = LoggerFactory.getLogger(DhcpClientIdOption.class);
    
    private ClientIdOption clientIdOption;
    
    public DhcpClientIdOption()
    {
        super();
        clientIdOption = new ClientIdOption();
    }
    public DhcpClientIdOption(ClientIdOption clientIdOption)
    {
        super();
        if (clientIdOption != null)
            this.clientIdOption = clientIdOption;
        else
            this.clientIdOption = new ClientIdOption();
    }

    public ClientIdOption getClientIdOption()
    {
        return clientIdOption;
    }

    public void setClientIdOption(ClientIdOption clientIdOption)
    {
        if (clientIdOption != null)
            this.clientIdOption = clientIdOption;
    }
    
    public int getCode()
    {
        return clientIdOption.getCode();
    }

    public String getName()
    {
        return clientIdOption.getName();
    }
    
    public OpaqueData getOpaqueData()
    {
        return (OpaqueData)this.clientIdOption;
    }
}
