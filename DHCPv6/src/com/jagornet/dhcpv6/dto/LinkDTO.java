/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file LinkDTO.java is part of DHCPv6.
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

/*
 *   This file LinkDTO.java is part of DHCPv6.
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

import java.io.Serializable;
import java.util.List;

/**
 * The Class LinkDTO.
 * 
 * @author A. Gregory Rabil
 */
public class LinkDTO implements Serializable
{
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/** The name. */
	protected String name;
    
    /** The address. */
    protected String address;
	
	/** The policies. */
	protected List<PolicyDTO> policies;
    
    /** The server id option. */
    protected ServerIdOptionDTO serverIdOption;
    
    /** The standard options. */
    protected StandardOptionsDTO standardOptions;
    
    //    protected List<OptionDTO> otherOptions;
    
    /** The filters. */
    protected List<FilterDTO> filters;

    //    protected List<PoolDTO> pools;

    /**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the address.
	 * 
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * Sets the address.
	 * 
	 * @param address the new address
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	
	/**
	 * Gets the policies.
	 * 
	 * @return the policies
	 */
	public List<PolicyDTO> getPolicies() {
		return policies;
	}
	
	/**
	 * Sets the policies.
	 * 
	 * @param policies the new policies
	 */
	public void setPolicies(List<PolicyDTO> policies) {
		this.policies = policies;
	}
	
	/**
	 * Gets the server id option.
	 * 
	 * @return the server id option
	 */
	public ServerIdOptionDTO getServerIdOption() {
		return serverIdOption;
	}
	
	/**
	 * Sets the server id option.
	 * 
	 * @param serverIdOption the new server id option
	 */
	public void setServerIdOption(ServerIdOptionDTO serverIdOption) {
		this.serverIdOption = serverIdOption;
	}
	
	/**
	 * Gets the standard options.
	 * 
	 * @return the standard options
	 */
	public StandardOptionsDTO getStandardOptions() {
		return standardOptions;
	}
	
	/**
	 * Sets the standard options.
	 * 
	 * @param standardOptions the new standard options
	 */
	public void setStandardOptions(StandardOptionsDTO standardOptions) {
		this.standardOptions = standardOptions;
	}
	
	/**
	 * Gets the filters.
	 * 
	 * @return the filters
	 */
	public List<FilterDTO> getFilters() {
		return filters;
	}
	
	/**
	 * Sets the filters.
	 * 
	 * @param filters the new filters
	 */
	public void setFilters(List<FilterDTO> filters) {
		this.filters = filters;
	}
}

