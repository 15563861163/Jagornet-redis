/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file PrefixBindingManagerImpl.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.db.IaPrefix;
import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessageInterface;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaPdOption;
import com.jagornet.dhcpv6.option.DhcpIaPrefixOption;
import com.jagornet.dhcpv6.server.config.DhcpConfigObject;
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;
import com.jagornet.dhcpv6.xml.LinkFiltersType;
import com.jagornet.dhcpv6.xml.PrefixBinding;
import com.jagornet.dhcpv6.xml.PrefixBindingsType;
import com.jagornet.dhcpv6.xml.PrefixPool;
import com.jagornet.dhcpv6.xml.PrefixPoolsType;

/**
 * The Class PrefixBindingManagerImpl.
 * 
 * @author A. Gregory Rabil
 */
public class PrefixBindingManagerImpl 
		extends BaseBindingManager 
		implements PrefixBindingManager
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(PrefixBindingManagerImpl.class);
    
	/**
	 * Instantiates a new prefix binding manager impl.
	 */
	public PrefixBindingManagerImpl()
	{
		super();
	}
	
	protected void startReaper()
	{
		//TODO: separate properties for address/prefix binding managers?
		long reaperStartupDelay = 
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_REAPER_STARTUP_DELAY);
		long reaperRunPeriod =
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_REAPER_RUN_PERIOD);

		reaper = new Timer("BindingReaper");
		reaper.schedule(new ReaperTimerTask(), reaperStartupDelay, reaperRunPeriod);
	}
    
    
    /**
     * Build the list of PrefixBindingPools from the list of configured PrefixPools
     * for the given configuration Link container object. The list of PrefixBindingPools
     * starts with the filtered PrefixPools followed by non-filtered PrefixPools.
     * 
     * @param link the configuration Link object
     * 
     * @return the list of PrefixBindingPools (<? extends BindingPool>)
     * 
     * @throws DhcpServerConfigException if there is a problem parsing a configured range
     */
    protected List<? extends BindingPool> buildBindingPools(Link link) 
    		throws DhcpServerConfigException
    {
		List<PrefixBindingPool> bindingPools = new ArrayList<PrefixBindingPool>();
		// Put the filtered pools first in the list of pools on this link
		LinkFiltersType linkFiltersType = link.getLinkFilters();
		if (linkFiltersType != null) {
			List<LinkFilter> linkFilters = linkFiltersType.getLinkFilterList();
			if ((linkFilters != null) && !linkFilters.isEmpty()) {
				for (LinkFilter linkFilter : linkFilters) {
					PrefixPoolsType poolsType = linkFilter.getPrefixPools();
					if (poolsType != null) {
						// add the filtered pools to the mapped list
		    			List<PrefixPool> pools = poolsType.getPoolList();
		    			if ((pools != null) && !pools.isEmpty()) {
		    				for (PrefixPool pool : pools) {
		    					PrefixBindingPool abp = buildBindingPool(pool, link, linkFilter);
								bindingPools.add(abp);
		    				}
		    			}
		    			else {
		    				log.error("PoolList is null for PoolsType: " + poolsType);
		    			}
					}
					else {
						log.info("PoolsType is null for LinkFilter: " + linkFilter.getName());
					}
				}
			}
		}
		PrefixPoolsType poolsType = link.getPrefixPools();
		if (poolsType != null) {
			// add the unfiltered pools to the mapped list
			List<PrefixPool> pools = poolsType.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				for (PrefixPool pool : pools) {
					PrefixBindingPool abp = buildBindingPool(pool, link);
					bindingPools.add(abp);
				}
			}
			else {
				log.error("PoolList is null for PoolsType: " + poolsType);
			}
		}
		else {
			log.info("PoolsType is null for Link: " + link.getName());
		}
		
		reconcilePools(bindingPools);
		
		return bindingPools;
    }
    
    /**
     * Reconcile pools.  Delete any IaAddress objects not contained
     * within the given list of PrefixBindingPools.
     * 
     * @param bindingPools the list of PrefixBindingPools
     */
    protected void reconcilePools(List<PrefixBindingPool> bindingPools)
    {
    	if ((bindingPools != null) && !bindingPools.isEmpty()) {
    		List<Range> ranges = new ArrayList<Range>();
    		for (PrefixBindingPool bp : bindingPools) {
    			Range range = new Range(bp.getStartAddress(), bp.getEndAddress());
				ranges.add(range);
			}
        	iaMgr.reconcileIaAddresses(ranges);
    	}
    }
    
    /**
     * Builds a binding pool from an PrefixPool using the given link.
     * 
     * @param pool the PrefixPool to wrap as an PrefixBindingPool
     * @param link the link
     * 
     * @return the binding pool
     * 
     * @throws DhcpServerConfigException if there is a problem parsing the configured range
     */
    protected PrefixBindingPool buildBindingPool(PrefixPool pool, Link link) 
    		throws DhcpServerConfigException
    {
    	return buildBindingPool(pool, link, null);
    }

    /**
     * Builds a binding pool from an PrefixPool using the given link and filter.
     * 
     * @param pool the AddressPool to wrap as an PrefixBindingPool
     * @param link the link
     * @param linkFilter the link filter
     * 
     * @return the binding pool
     * 
     * @throws DhcpServerConfigException if there is a problem parsing the configured range
     */
    protected PrefixBindingPool buildBindingPool(PrefixPool pool, Link link, 
    		LinkFilter linkFilter) throws DhcpServerConfigException
    {
		PrefixBindingPool bp = new PrefixBindingPool(pool);
		long pLifetime = 
			DhcpServerPolicies.effectivePolicyAsLong((DhcpConfigObject) bp, 
					link, Property.PREFERRED_LIFETIME);
		bp.setPreferredLifetime(pLifetime);
		long vLifetime = 
			DhcpServerPolicies.effectivePolicyAsLong((DhcpConfigObject) bp, 
					link, Property.VALID_LIFETIME);
		bp.setValidLifetime(vLifetime);
		bp.setLinkFilter(linkFilter);
		
		List<InetAddress> usedIps = iaMgr.findExistingIPs(bp.getStartAddress(), bp.getEndAddress());
		if ((usedIps != null) && !usedIps.isEmpty()) {
			for (InetAddress ip : usedIps) {
				//TODO: for the quickest startup?...
				// set IP as used without checking if the binding has expired
				// let the reaper thread deal with all binding cleanup activity
				bp.setUsed(ip);
			}
		}
    	return bp;
    }
    
    protected List<? extends StaticBinding> buildStaticBindings(Link link) 
			throws DhcpServerConfigException
	{
		List<StaticPrefixBinding> staticBindings = new ArrayList<StaticPrefixBinding>();
		PrefixBindingsType bindingsType = link.getPrefixBindings();
		if (bindingsType != null) {
			List<PrefixBinding> bindings = bindingsType.getBindingList();
			if ((bindings != null) && !bindings.isEmpty()) {
				for (PrefixBinding binding : bindings) {
					StaticPrefixBinding spb = buildStaticBinding(binding, link);
					staticBindings.add(spb);
				}
			}
		}
		
		return staticBindings;
	}
    
    protected StaticPrefixBinding buildStaticBinding(PrefixBinding binding, Link link) 
    		throws DhcpServerConfigException
    {
    	try {
    		InetAddress inetAddr = InetAddress.getByName(binding.getPrefix());
    		StaticPrefixBinding sb = new StaticPrefixBinding(binding);
    		setIpAsUsed(link, inetAddr);
    		return sb;
    	}
    	catch (UnknownHostException ex) {
    		log.error("Invalid static binding address", ex);
    		throw new DhcpServerConfigException("Invalid static binding address", ex);
    	}
    }
    
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager#findCurrentBinding(com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaPdOption, com.jagornet.dhcpv6.message.DhcpMessageInterface)
	 */
	public Binding findCurrentBinding(Link clientLink, DhcpClientIdOption clientIdOption, 
			DhcpIaPdOption iaPdOption, DhcpMessageInterface requestMsg)
	{
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaPdOption.getIaPdOption().getIaId();
		
		return super.findCurrentBinding(clientLink, duid, IdentityAssoc.PD_TYPE, 
				iaid, requestMsg);
		
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager#createSolicitBinding(com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaPdOption, com.jagornet.dhcpv6.message.DhcpMessageInterface, boolean)
	 */
	public Binding createSolicitBinding(Link clientLink, DhcpClientIdOption clientIdOption, 
			DhcpIaPdOption iaPdOption, DhcpMessageInterface requestMsg, byte state)
	{	
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaPdOption.getIaPdOption().getIaId();

		StaticBinding staticBinding = 
			findStaticBinding(clientLink, duid, IdentityAssoc.PD_TYPE, iaid, requestMsg);
		
		if (staticBinding != null) {
			return super.createStaticBinding(clientLink, duid, IdentityAssoc.PD_TYPE, 
					iaid, staticBinding, requestMsg);
		}
		else {
			return super.createBinding(clientLink, duid, IdentityAssoc.PD_TYPE, 
					iaid, getInetAddrs(iaPdOption), requestMsg, state);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager#updateBinding(com.jagornet.dhcpv6.server.request.binding.Binding, com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaPdOption, com.jagornet.dhcpv6.message.DhcpMessageInterface, byte)
	 */
	public Binding updateBinding(Binding binding, Link clientLink, 
			DhcpClientIdOption clientIdOption, DhcpIaPdOption iaPdOption,
			DhcpMessageInterface requestMsg, byte state)
	{
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaPdOption.getIaPdOption().getIaId();

		StaticBinding staticBinding = 
			findStaticBinding(clientLink, duid, IdentityAssoc.PD_TYPE, iaid, requestMsg);
		
		if (staticBinding != null) {
			return super.updateStaticBinding(binding, clientLink, duid, IdentityAssoc.PD_TYPE, 
					iaid, staticBinding, requestMsg);
		}
		else {
			return super.updateBinding(binding, clientLink, duid, IdentityAssoc.PD_TYPE,
					iaid, getInetAddrs(iaPdOption), requestMsg, state);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager#releaseIaPrefix(com.jagornet.dhcpv6.db.IaPrefix)
	 */
	public void releaseIaPrefix(IaPrefix iaPrefix)
	{
		try {
			if (DhcpServerPolicies.globalPolicyAsBoolean(
					Property.BINDING_MANAGER_DELETE_OLD_BINDINGS)) {
				iaMgr.deleteIaPrefix(iaPrefix);
				// free the prefix only if it is deleted from the db,
				// otherwise, we will get a unique constraint violation
				// if another client obtains this released prefix
				freeAddress(iaPrefix.getIpAddress());
			}
			else {
				iaPrefix.setStartTime(null);
				iaPrefix.setPreferredEndTime(null);
				iaPrefix.setValidEndTime(null);
				iaPrefix.setState(IaPrefix.RELEASED);
				iaMgr.updateIaPrefix(iaPrefix);
			}
		}
		catch (Exception ex) {
			log.error("Failed to release address", ex);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager#declineIaPrefix(com.jagornet.dhcpv6.db.IaPrefix)
	 */
	public void declineIaPrefix(IaPrefix iaPrefix)
	{
		try {
			iaPrefix.setStartTime(null);
			iaPrefix.setPreferredEndTime(null);
			iaPrefix.setValidEndTime(null);
			iaPrefix.setState(IaPrefix.DECLINED);
			iaMgr.updateIaPrefix(iaPrefix);
		}
		catch (Exception ex) {
			log.error("Failed to decline address", ex);
		}
	}
	
	/**
	 * Callback from the ExpireTimerTask started when the lease was granted.
	 * NOT CURRENTLY USED
	 * 
	 * @param iaPrefix the ia prefix
	 */
	public void expireIaPrefix(IaPrefix iaPrefix)
	{
		try {
			if (DhcpServerPolicies.globalPolicyAsBoolean(
					Property.BINDING_MANAGER_DELETE_OLD_BINDINGS)) {
				log.debug("Deleting expired prefix: " + iaPrefix.getIpAddress());
				iaMgr.deleteIaPrefix(iaPrefix);
				// free the prefix only if it is deleted from the db,
				// otherwise, we will get a unique constraint violation
				// if another client obtains this released prefix
				freeAddress(iaPrefix.getIpAddress());
			}
			else {
				iaPrefix.setStartTime(null);
				iaPrefix.setPreferredEndTime(null);
				iaPrefix.setValidEndTime(null);
				iaPrefix.setState(IaPrefix.EXPIRED);
				log.debug("Updating expired prefix: " + iaPrefix.getIpAddress());
				iaMgr.updateIaPrefix(iaPrefix);
			}
		}
		catch (Exception ex) {
			log.error("Failed to expire address", ex);
		}
	}
	
	/**
	 * Callback from the RepearTimerTask started when the BindingManager initialized.
	 * Find any expired prefixes as of now, and expire them already.
	 */
	public void expirePrefixes()
	{
		List<IaPrefix> expiredPrefs = iaMgr.findExpiredIaPrefixes();
		if ((expiredPrefs != null) && !expiredPrefs.isEmpty()) {
			for (IaPrefix iaPrefix : expiredPrefs) {
				expireIaPrefix(iaPrefix);
			}
		}
	}
	
	/**
	 * Extract the list of IP addresses from within the given IA_PD option.
	 * 
	 * @param iaNaOption the IA_PD option
	 * 
	 * @return the list of InetAddresses for the IPs in the IA_PD option
	 */
	private List<InetAddress> getInetAddrs(DhcpIaPdOption iaPdOption)
	{
		List<InetAddress> inetAddrs = null;
		List<DhcpIaPrefixOption> iaPrefs = iaPdOption.getIaPrefixOptions();
		if ((iaPrefs != null) && !iaPrefs.isEmpty()) {
			inetAddrs = new ArrayList<InetAddress>();
			for (DhcpIaPrefixOption iaPrefix : iaPrefs) {
				InetAddress inetAddr = iaPrefix.getInetAddress();
				inetAddrs.add(inetAddr);
			}
		}
		return inetAddrs;
	}
	
	/**
	 * Create a Binding given an IdentityAssoc loaded from the database.
	 * 
	 * @param ia the ia
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding
	 */
	protected Binding buildBindingFromIa(IdentityAssoc ia, 
			Link clientLink, DhcpMessageInterface requestMsg)
	{
		Binding binding = new Binding(ia, clientLink);
		Collection<? extends IaAddress> iaPrefs = ia.getIaAddresses();
		if ((iaPrefs != null) && !iaPrefs.isEmpty()) {
			List<BindingPrefix> bindingPrefixes = new ArrayList<BindingPrefix>();
			for (IaAddress iaAddr : iaPrefs) {
				BindingPrefix bindingPrefix = null;
        		StaticBinding staticBinding =
        			findStaticBinding(clientLink, ia.getDuid(), 
        					ia.getIatype(), ia.getIaid(), requestMsg);
        		if (staticBinding != null) {
        			bindingPrefix = 
        				buildStaticBindingFromIaPrefix((IaPrefix)iaAddr, staticBinding);
        		}
        		else {
        			bindingPrefix =
        				buildBindingAddrFromIaPrefix((IaPrefix)iaAddr, clientLink, requestMsg);
        		}
				if (bindingPrefix != null)
					bindingPrefixes.add(bindingPrefix);
			}
			// replace the collection of IaPrefixes with BindingPrefixes
			binding.setIaAddresses(bindingPrefixes);
		}
		else {
			log.warn("IA has no prefixes, binding is empty.");
		}
		return binding;
	}

	/**
	 * Create a BindingPrefix given an IaPrefix loaded from the database.
	 * 
	 * @param iaPrefix the ia prefix
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding address
	 */
	private BindingPrefix buildBindingAddrFromIaPrefix(IaPrefix iaPrefix, 
			Link clientLink, DhcpMessageInterface requestMsg)
	{
		InetAddress inetAddr = iaPrefix.getIpAddress();
		BindingPool bp = findBindingPool(clientLink, inetAddr, requestMsg);
		if (bp != null) {
			// TODO store the configured options in the persisted binding?
			// ipAddr.setDhcpOptions(bp.getDhcpOptions());
			return new BindingPrefix(iaPrefix, (PrefixBindingPool)bp);
		}
		else {
			log.error("Failed to create BindingPrefix: No BindingPool found for IP=" + 
					inetAddr.getHostAddress());
		}
		// MUST have a BindingPool, otherwise something's broke
		return null;
	}
	
	/**
	 * Build a BindingPrefix given an IaAddress loaded from the database
	 * and a static binding for the client request.
	 * 
	 * @param iaPrefix
	 * @param staticBinding
	 * @return
	 */
	private BindingPrefix buildStaticBindingFromIaPrefix(IaPrefix iaPrefix, 
			StaticBinding staticBinding)
	{
		BindingPrefix bindingPrefix = new BindingPrefix(iaPrefix, staticBinding);
		return bindingPrefix;
	}
	
	/**
	 * Build a BindingPrefix for the given InetAddress and Link.
	 * 
	 * @param inetAddr the inet addr
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding address
	 */
	protected BindingObject buildBindingObject(InetAddress inetAddr, 
			Link clientLink, DhcpMessageInterface requestMsg)
	{
		PrefixBindingPool bp = (PrefixBindingPool) findBindingPool(clientLink, inetAddr, requestMsg);
		if (bp != null) {
			bp.setUsed(inetAddr);	// TODO check if this is necessary
			IaPrefix iaPrefix = new IaPrefix();
			iaPrefix.setIpAddress(inetAddr);
			iaPrefix.setPrefixLength((short)bp.getAllocPrefixLen());
			BindingPrefix bindingPrefix = new BindingPrefix(iaPrefix, bp);
			setBindingObjectTimes(bindingPrefix, 
					bp.getPreferredLifetimeMs(), bp.getPreferredLifetimeMs());
			// TODO store the configured options in the persisted binding?
			// bindingPrefix.setDhcpOptions(bp.getDhcpOptions());
			return bindingPrefix;
		}
		else {
			log.error("Failed to create BindingPrefix: No BindingPool found for IP=" + 
					inetAddr.getHostAddress());
		}
		// MUST have a BindingPool, otherwise something's broke
		return null;
	}
	
	/**
	 * The Class ReaperTimerTask.
	 */
	class ReaperTimerTask extends TimerTask
	{		
		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
//			log.debug("Expiring addresses...");
			expirePrefixes();
		}
	}
}
