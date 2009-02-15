package com.jagornet.dhcpv6.message;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.option.DhcpDnsServersOption;
import com.jagornet.dhcpv6.option.DhcpDomainSearchListOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.option.DhcpIaTaOption;
import com.jagornet.dhcpv6.option.DhcpInfoRefreshTimeOption;
import com.jagornet.dhcpv6.option.DhcpNisDomainNameOption;
import com.jagornet.dhcpv6.option.DhcpNisPlusDomainNameOption;
import com.jagornet.dhcpv6.option.DhcpNisPlusServersOption;
import com.jagornet.dhcpv6.option.DhcpNisServersOption;
import com.jagornet.dhcpv6.option.DhcpOption;
import com.jagornet.dhcpv6.option.DhcpOptionRequestOption;
import com.jagornet.dhcpv6.option.DhcpPreferenceOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.option.DhcpSipServerAddressesOption;
import com.jagornet.dhcpv6.option.DhcpSipServerDomainNamesOption;
import com.jagornet.dhcpv6.option.DhcpSntpServersOption;
import com.jagornet.dhcpv6.option.DhcpStatusCodeOption;
import com.jagornet.dhcpv6.option.DhcpVendorInfoOption;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.DnsServersOption;
import com.jagornet.dhcpv6.xml.DomainSearchListOption;
import com.jagornet.dhcpv6.xml.Filter;
import com.jagornet.dhcpv6.xml.FilterExpression;
import com.jagornet.dhcpv6.xml.InfoRefreshTimeOption;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.NisDomainNameOption;
import com.jagornet.dhcpv6.xml.NisPlusDomainNameOption;
import com.jagornet.dhcpv6.xml.NisPlusServersOption;
import com.jagornet.dhcpv6.xml.NisServersOption;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.PreferenceOption;
import com.jagornet.dhcpv6.xml.ServerIdOption;
import com.jagornet.dhcpv6.xml.SipServerAddressesOption;
import com.jagornet.dhcpv6.xml.SipServerDomainNamesOption;
import com.jagornet.dhcpv6.xml.SntpServersOption;
import com.jagornet.dhcpv6.xml.StandardOptions;
import com.jagornet.dhcpv6.xml.StatusCodeOption;
import com.jagornet.dhcpv6.xml.VendorInfoOption;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig;

/**
 * Title:        DhcpInfoRequestProcessor
 * Description:  The main class for processing INFO_REQUEST messages.
 * 
 * Copyright:    Copyright (c) 2003
 * Company:      AGR Consulting
 * @author A. Gregory Rabil
 * @version 1.0
 */

public class DhcpInfoRequestProcessor implements DhcpRequestProcessor
{
	private static Logger log = LoggerFactory.getLogger(DhcpInfoRequestProcessor.class);

    protected static DhcpV6ServerConfig dhcpServerConfig = 
                                        DhcpServerConfiguration.getConfig();

    protected final InetAddress clientLink;
    protected final DhcpMessage requestMsg;
    protected DhcpMessage replyMsg;
    protected List<Integer> requestedOptionCodes;
    
    /**
     * Construct an DhcpInfoRequest processor
     *
     * @param   clientLink  the interface address for the client link
     * @param   reqMsg  must be an INFO_REQUEST type DhcpMessage
     */
    public DhcpInfoRequestProcessor(InetAddress clientLink, DhcpMessage reqMsg)
    {
        this.clientLink = clientLink;
        this.requestMsg = reqMsg;
        Map<Integer, DhcpOption> optionMap = this.requestMsg.getDhcpOptions();
        if (optionMap != null) {
        	DhcpOptionRequestOption oro = 
        		(DhcpOptionRequestOption) optionMap.get(DhcpConstants.OPTION_ORO);
        	if (oro != null) {
        		int[] opCodeArray = oro.getOptionRequestOption().getRequestedOptionCodesArray();
        		if ( (opCodeArray != null) && (opCodeArray.length > 0)) {
        			requestedOptionCodes = new ArrayList<Integer>();
        			for (int opCode : opCodeArray) {
						requestedOptionCodes.add(opCode);
					}
        		}
        	}
        }
        else {
        	// TODO throw exception?
        	log.error("No options found in Info-RequestMessage!");
        }    	
    }

    /**
     * Process the client request.  Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     */
    public DhcpMessage process()
    {
/**
 * FROM RFC 3315:
 * 
 * 15.12. Information-request Message

   Clients MUST discard any received Information-request messages.

   Servers MUST discard any received Information-request message that
   meets any of the following conditions:

   -  The message includes a Server Identifier option and the DUID in
      the option does not match the server's DUID.

   -  The message includes an IA option.
 */     
        
        log.info("Processing: " + requestMsg.toString());

        ServerIdOption serverIdOption = dhcpServerConfig.getServerIdOption();
        if (serverIdOption == null) {
            log.error("Invalid configuration - ServerId option must not be null!");
            return null;
        }

        DhcpServerIdOption dhcpServerIdOption =
            new DhcpServerIdOption(serverIdOption);
        
        boolean hasIaOption = false;
        DhcpClientIdOption clientIdOption = null;
        DhcpServerIdOption requestedServerIdOption = null;
        Collection<DhcpOption> options = requestMsg.getOptions();
        if (options != null) {
            for (DhcpOption option : options) {
                log.debug(option.toString());
                if (option instanceof DhcpClientIdOption) {
                    clientIdOption = (DhcpClientIdOption)option;
                }
                else if (option instanceof DhcpServerIdOption) {
                    requestedServerIdOption = (DhcpServerIdOption)option;
                }
                else if (option instanceof DhcpIaNaOption) {
                    hasIaOption = true;
                }
                else if (option instanceof DhcpIaTaOption) {
                    hasIaOption = true;
                }
            }
        }
        
        // if the client message has an IA option (IA_NA, IA_TA)
        // then the Stateless DHCPv6 server must ignore the request
        if (hasIaOption) {
            log.warn("Ignoring Info-Request message: " +
                     " client message contains an IA option.");
            return null;
        }
        
        // if the client provided a ServerID option, then it MUST
        // match our configured ServerID, otherwise ignore the request
        if ( (requestedServerIdOption != null) &&
             !dhcpServerIdOption.equals(requestedServerIdOption) ) {
            log.warn("Ingoring Info-Request message: " +
                     "Requested ServerID: " + requestedServerIdOption +
                     "My ServerID: " + dhcpServerIdOption);
            return null;
        }
        
        // build a message destined to the host:port which sent the request
        replyMsg = new DhcpMessage(requestMsg.getHost(), requestMsg.getPort());
        // copy the transaction ID into the reply
        replyMsg.setTransactionId(requestMsg.getTransactionId());
        // this is a reply message
        replyMsg.setMessageType(DhcpConstants.REPLY);

        // MUST put Server Identifier DUID in REPLY message
        replyMsg.setOption(dhcpServerIdOption);
        
        // MUST copy Client Identifier DUID if given in INFO_REQUEST message
        if (clientIdOption != null) {
            replyMsg.setOption(clientIdOption);
        }
        else {
            log.warn("No ClientId option supplied in Info-Request");
        }

        // put any globally defined options in the reply packet
        setGlobalOptions();
        
        // process global filter groups
        processFilters(Arrays.asList(dhcpServerConfig.getFiltersArray()));
        
        // handle configuration for the client's link
        Link link = DhcpServerConfiguration.findLinkForAddress(clientLink);
        if (link != null) {
            log.info("Processing configuration for link: " + link.getAddress());
            processLink(link);
        }

        log.info("Built: " + replyMsg.toString());
        
        return replyMsg;
    }

    private void setGlobalOptions()
    {
    	setStandardOptions(dhcpServerConfig.getStandardOptions());
    }

    private void setStandardOptions(StandardOptions standardOptions)
    {
    	if (standardOptions != null) {
	        setPreferenceOption(standardOptions.getPreferenceOption());
	        // don't set this option for stateless servers?
	        // setServerUnicastOption(dhcpServerConfig.getServerUnicastOption());
	        setStatusCodeOption(standardOptions.getStatusCodeOption());
	        setVendorInfoOption(standardOptions.getVendorInfoOption());
	        setDnsServersOption(standardOptions.getDnsServersOption());
	        setDomainSearchListOption(standardOptions.getDomainSearchListOption());
	        setSipServerAddressesOption(standardOptions.getSipServerAddressesOption());
	        setSipServerDomainNamesOption(standardOptions.getSipServerDomainNamesOption());
	        setNisServersOption(standardOptions.getNisServersOption());
	        setNisDomainNameOption(standardOptions.getNisDomainNameOption());
	        setNisPlusServersOption(standardOptions.getNisPlusServersOption());
	        setNisPlusDomainNameOption(standardOptions.getNisPlusDomainNameOption());
	        setSntpServersOption(standardOptions.getSntpServersOption());
	        setInfoRefreshTimeOption(standardOptions.getInfoRefreshTimeOption());
    	}
    }
    
    private boolean clientWantsOption(int optionCode)
    {
    	if (requestedOptionCodes != null)
    		return requestedOptionCodes.contains(optionCode);
    	
    	// if there is no ORO, then the client doesn't care,
    	// so go ahead and send it if so configured
    	return true;
    }
    
    private void setPreferenceOption(PreferenceOption preferenceOption)
    {
        if (preferenceOption != null) {
        	if (clientWantsOption(preferenceOption.getCode()))
        		replyMsg.setOption(new DhcpPreferenceOption(preferenceOption)); 
        }
    }    
    
    private void setStatusCodeOption(StatusCodeOption statusCodeOption)
    {
        if (statusCodeOption != null) {
        	if (clientWantsOption(statusCodeOption.getCode()))
        		replyMsg.setOption(new DhcpStatusCodeOption(statusCodeOption)); 
        }
    }    

    private void setVendorInfoOption(VendorInfoOption vendorInfoOption)
    {
        if (vendorInfoOption != null) {
        	if (clientWantsOption(vendorInfoOption.getCode()))
        		replyMsg.setOption(new DhcpVendorInfoOption(vendorInfoOption)); 
        }
    }
    
    private void setDnsServersOption(DnsServersOption dnsServersOption)
    {
        if (dnsServersOption != null) {
        	if (clientWantsOption(dnsServersOption.getCode()))
        			replyMsg.setOption(new DhcpDnsServersOption(dnsServersOption)); 
        }
    }

    private void setDomainSearchListOption(DomainSearchListOption domainSearchListOption)
    {
        if (domainSearchListOption != null) {
        	if (clientWantsOption(domainSearchListOption.getCode()))
        			replyMsg.setOption(new DhcpDomainSearchListOption(domainSearchListOption)); 
        }
    }
    
    private void setSipServerAddressesOption(SipServerAddressesOption sipServerAddressesOption)
    {
        if (sipServerAddressesOption != null) {
        	if (clientWantsOption(sipServerAddressesOption.getCode()))
        		replyMsg.setOption(new DhcpSipServerAddressesOption(sipServerAddressesOption)); 
        }
    }

    private void setSipServerDomainNamesOption(SipServerDomainNamesOption sipServerDomainNamesOption)
    {
        if (sipServerDomainNamesOption != null) {
        	if (clientWantsOption(sipServerDomainNamesOption.getCode()))
        		replyMsg.setOption(new DhcpSipServerDomainNamesOption(sipServerDomainNamesOption)); 
        }
    }

    private void setNisServersOption(NisServersOption nisServersOption)
    {
        if (nisServersOption != null) {
        	if (clientWantsOption(nisServersOption.getCode()))
        		replyMsg.setOption(new DhcpNisServersOption(nisServersOption)); 
        }
    }

    private void setNisDomainNameOption(NisDomainNameOption nisDomainNameOption)
    {
        if (nisDomainNameOption != null) {
        	if (clientWantsOption(nisDomainNameOption.getCode()))
        		replyMsg.setOption(new DhcpNisDomainNameOption(nisDomainNameOption)); 
        }
    }

    private void setNisPlusServersOption(NisPlusServersOption nisPlusServersOption)
    {
        if (nisPlusServersOption != null) {
        	if (clientWantsOption(nisPlusServersOption.getCode()))
        		replyMsg.setOption(new DhcpNisPlusServersOption(nisPlusServersOption)); 
        }
    }

    private void setNisPlusDomainNameOption(NisPlusDomainNameOption nisPlusDomainNameOption)
    {
        if (nisPlusDomainNameOption != null) {
        	if (clientWantsOption(nisPlusDomainNameOption.getCode()))
        		replyMsg.setOption(new DhcpNisPlusDomainNameOption(nisPlusDomainNameOption)); 
        }
    }

    private void setSntpServersOption(SntpServersOption sntpServersOption)
    {
        if (sntpServersOption != null) {
        	if (clientWantsOption(sntpServersOption.getCode()))
        		replyMsg.setOption(new DhcpSntpServersOption(sntpServersOption)); 
        }
    }

    private void setInfoRefreshTimeOption(InfoRefreshTimeOption infoRefreshTimeOption)
    {
        if (infoRefreshTimeOption != null) {
        	if (clientWantsOption(infoRefreshTimeOption.getCode()))
        		replyMsg.setOption(new DhcpInfoRefreshTimeOption(infoRefreshTimeOption)); 
        }
    }
    
    private void processFilters(List<Filter> filters)
    {
        if (filters != null) {
            for (Filter filter : filters) {
                List<FilterExpression> expressions = Arrays.asList(filter.getFilterExpressionsArray());
                if (expressions != null) {
                    boolean matches = true;     // assume match
                    for (FilterExpression expression : expressions) {
                    	OptionExpression optexpr = expression.getOptionExpression();
                    	// TODO: handle CustomExpression filters
                        DhcpOption option = requestMsg.getOption(optexpr.getCode());
                        if (option != null) {
                            // found the filter option in the request,
                            // so check if the expression matches
                            if (!evaluateExpression(optexpr, option)) {
                                // it must match all expressions for the filter
                                // group (i.e. expressions are ANDed), so if
                                // just one doesn't match, then we're done
                                matches = false;
                                break;
                            }
                        }
                        else {
                            // if the expression option wasn't found in the
                            // request message, then it can't match
                            matches = false;
                            break;
                        }
                    }
                    if (matches) {
                        // got a match, apply filter group options to the reply message
                        log.info("Request matches filter: " + filter.getName());
                        setFilterOptions(filter);                        
                    }
                }
            }
        }        
    }
    
    private boolean evaluateExpression(OptionExpression expression, DhcpOption option)
    {
        boolean matches = false;
        if (option instanceof DhcpComparableOption) {
            matches = ((DhcpComparableOption)option).matches(expression);
        }
        else {
            log.error("Configured option expression is not comparable:" +
                      " code=" + expression.getCode());
        }
        return matches;
    }
    
    private void setFilterOptions(Filter filter)
    {
    	setStandardOptions(filter.getStandardOptions());
    }
    
    private void processLink(Link link)
    {
        if (link != null) {
            setLinkOptions(link);
            if (link.getFiltersArray() != null) {
            	List<Filter> filters = Arrays.asList(link.getFiltersArray());
                processFilters(filters);
            }
        }
    }
    
    private void setLinkOptions(Link link)
    {
    	setStandardOptions(link.getStandardOptions());
    }
}
