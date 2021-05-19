package com.example.eurekadiscovery;

import org.apache.catalina.connector.Connector;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.cloud.bootstrap.BootstrapConfiguration;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;

@Configuration
@BootstrapConfiguration
class SslConfig {

    @Autowired
    ConfigClientProperties properties;

    @Primary
    @Bean
    public ConfigServicePropertySourceLocator configServicePropertySourceLocator() throws Exception {
        final char[] password = {'d','r','y','t','c','g','v','h','b','j','k'};
        final File keyStoreFile = new File("src/main/resources/discoveryService.p12");
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStoreFile, password, password)
                .loadTrustMaterial(keyStoreFile, password).build();
        CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        ConfigServicePropertySourceLocator configServicePropertySourceLocator = new ConfigServicePropertySourceLocator(properties);
        configServicePropertySourceLocator.setRestTemplate(new RestTemplate(requestFactory));
        return configServicePropertySourceLocator;
    }

    @Bean
    public ServletWebServerFactory tomcatEmbeddedServletContainerFactory() {
        final TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory ();
        factory.addAdditionalTomcatConnectors(this.createConnection());
        return factory;
    }

    private Connector createConnection() {
        final String protocol = "org.apache.coyote.http11.Http11NioProtocol";
        final Connector connector = new Connector(protocol);

        connector.setScheme("http");
        connector.setPort(8762);
        connector.setRedirectPort(8761);
        return connector;
    }
}
