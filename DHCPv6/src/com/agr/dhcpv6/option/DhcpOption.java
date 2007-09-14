package com.agr.dhcpv6.option;


/**
 * <p>Title: DhcpOptionInterface </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public interface DhcpOption extends Encodable, Decodable
{
    public abstract short getCode();

//    public abstract void setCode(Short code);

//    public abstract String getName();

//    public abstract void setName(String name);
    
    public abstract short getLength();
    
    public abstract String toString();
}
