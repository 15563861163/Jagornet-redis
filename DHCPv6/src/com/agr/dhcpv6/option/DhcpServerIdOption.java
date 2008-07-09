package com.agr.dhcpv6.option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.ServerIdOption;

/**
 * <p>Title: DhcpServerIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpServerIdOption extends BaseOpaqueDataOption
{
	private static Logger log = LoggerFactory.getLogger(DhcpServerIdOption.class);
    
    private ServerIdOption serverIdOption;
    
    public DhcpServerIdOption()
    {
        super();
        serverIdOption = new ServerIdOption();
    }
    public DhcpServerIdOption(ServerIdOption serverIdOption)
    {
        super();
        if (serverIdOption != null)
            this.serverIdOption = serverIdOption;
        else
            this.serverIdOption = new ServerIdOption();
    }

    public ServerIdOption getServerIdOption()
    {
        return serverIdOption;
    }

    public void setServerIdOption(ServerIdOption serverIdOption)
    {
        if (serverIdOption != null)
            this.serverIdOption = serverIdOption;
    }

    public int getCode()
    {
        return serverIdOption.getCode();
    }
    
    public String getName()
    {
        return serverIdOption.getName();
    }
    
    public OpaqueData getOpaqueData()
    {
        return (OpaqueData)this.serverIdOption;
    }
}
