package com.jagornet.dhcpv6.server.request.ddns;

public interface DdnsCallback {
	
	public void fwdAddComplete(boolean success);
	public void fwdDeleteComplete(boolean success);
	public void revAddComplete(boolean success);
	public void revDeleteComplete(boolean success);
}
