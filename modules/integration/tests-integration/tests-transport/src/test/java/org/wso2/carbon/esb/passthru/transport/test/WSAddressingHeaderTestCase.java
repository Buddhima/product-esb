/**
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.esb.passthru.transport.test;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import javax.xml.namespace.QName;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class WSAddressingHeaderTestCase extends ESBIntegrationTest {

    private ServerConfigurationManager configurationManager;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();

        AutomationContext autoCtx = new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN);
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(autoCtx);


        serverConfigurationManager.applyConfiguration(
                new File(getClass().getResource("/artifacts/ESB/proxyconfig/proxy/passThroughProxy/WSAddressingWSDL/axis2.xml").getPath()));

        loadESBConfigurationFromClasspath(
                "/artifacts/ESB/proxyconfig/proxy/passThroughProxy/WSAddressingWSDL/proxy_service_enabling_wsaddressing.xml");

    }

    @Test(groups = "wso2.esb", description = "- StockQuoteProxy proxy" +
                                             "- Publish WSDL Options - Specified Inline with WSAddressing")
    public void testWSAddressingProxy() throws Exception {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("StockQuoteProxy"), null, "WSO2");

        String lastPrice = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "last"))
                .getText();
        assertNotNull(lastPrice, "Fault: response message 'last' price null");

        String symbol = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "symbol"))
                .getText();
        assertEquals(symbol, "WSO2", "Fault: value 'symbol' mismatched");

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        try{
            super.cleanup();
        } catch (Exception ex){
            log.error("Issue with cleanup", ex);
        }
    }
}
