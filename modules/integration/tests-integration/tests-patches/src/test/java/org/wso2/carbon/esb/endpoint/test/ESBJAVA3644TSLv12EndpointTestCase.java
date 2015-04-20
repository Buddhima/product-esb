/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.endpoint.test;

import org.apache.axiom.om.OMElement;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;

public class ESBJAVA3644TSLv12EndpointTestCase extends ESBIntegrationTest {

    private final Tomcat tomcat = new Tomcat();
    private final String separator = File.separator;
    private final String resourceFolderPath = getESBResourceLocation() + separator + "passthru" + separator + "transport" + separator + "tslv12" + separator;


    @BeforeClass(alwaysRun = true)
    public void deployService() throws Exception {
        // Initializing server configuration
        super.init();

        // Initialize serverConfigurationManager
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(context);

        // Apply the nhttp axis2 configuration
        serverConfigurationManager.applyConfiguration(
                new File(resourceFolderPath + "axis2.xml"),
                new File(CarbonUtils.getCarbonHome() + separator + "repository" + separator + "conf" + separator + "axis2" + separator + "axis2.xml")
        );

        // Configure the standard host
        StandardHost stdHost = (StandardHost) tomcat.getHost();
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

        // Setting up the connector
        Connector connector = new Connector();
        connector.setPort(8443);
        connector.setProtocol("HTTP/1.1");
        connector.setProperty("SSLEnabled", "true");
        connector.setProperty("maxThreads", "150");
        connector.setScheme("https");
        connector.setSecure(true);
        connector.setProperty("clientAuth", "false");
        connector.setProperty("sslProtocol", "TLSv1.2");
//        connector.setProperty("sslEnabledProtocols", "TLSv1.2");
        connector.setProperty("keystoreFile", new File(resourceFolderPath + "ssl_check_keystore.jks").getAbsolutePath());
        connector.setProperty("keystorePass", "localhost");

        tomcat.getService().addConnector(connector);

        // Starting Tomcat
        tomcat.start();

        // Re-initializing server configuration
        super.init();

        // Deploying the artifact defined in the ssl_check.xml
        loadESBConfigurationFromClasspath("/artifacts/ESB/passthru/transport/tslv12/httpProxy.xml");
    }

    @Test(groups = "wso2.esb", description = "To check the SSL certificate failure redirect to fault sequence ")
    public void testTSLv12Handshaking() throws Exception {

        // Sending a message to the main sequence
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/",null, "WSO2");

        // Check the return message -- only the fault sequence can respond in this case
        Assert.assertTrue(response.toString().contains("WSO2"));

    }

    @AfterClass(alwaysRun = true)
    public void unDeployService() throws Exception {
        // Undeploying deployed artifact
        super.cleanup();
        tomcat.stop();
        tomcat.destroy();
    }
}
