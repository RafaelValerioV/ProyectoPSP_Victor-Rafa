package com.drivepsp.drivepsp_backend.config;

import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura el SSL de Tomcat en lugar de usar las
 * propiedades server.ssl.* de Spring, para tener control directo sobre las
 * clases de seguridad de Java que intervienen.
 *
 * El proceso es el siguiente: se carga el fichero PKCS12 en un KeyStore,
 * se inicializa un KeyManagerFactory con ese almacen, se crea un SSLContext
 * con TLS 1.3 y se establece como contexto por defecto de la JVM. Por ultimo
 * se configura el conector HTTPS de Tomcat apuntando a ese certificado.
 *
 * El certificado autofirmado se genera previamente con keytool. Los navegadores
 * mostraran una advertencia porque no esta firmado por una CA de confianza, lo
 * cual es normal en un entorno de desarrollo.
 */
@Configuration
public class SslConfig {

    @Value("${drivepsp.ssl.keystore-path}")
    private String keystorePath;

    @Value("${drivepsp.ssl.keystore-password}")
    private String keystorePassword;

    @Value("${server.port}")
    private int serverPort;

    /**
     * Bean que personaliza Tomcat para arrancar en HTTPS con el certificado
     * configurado. Spring Boot lo invoca automaticamente al iniciar.
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> sslCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.setPort(serverPort);
                connector.setSecure(true);
                connector.setScheme("https");
                connector.setProperty("SSLEnabled", "true");

                try {
                    KeyStore keyStore = KeyStore.getInstance("PKCS12");
                    keyStore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());

                    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                    kmf.init(keyStore, keystorePassword.toCharArray());

                    SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
                    sslContext.init(kmf.getKeyManagers(), null, null);

                    // Lo establecemos como contexto por defecto para que Tomcat lo use al crear conexiones.
                    SSLContext.setDefault(sslContext);

                    SSLHostConfig sslHostConfig = new SSLHostConfig();
                    sslHostConfig.setProtocols("TLSv1.3");

                    SSLHostConfigCertificate cert = new SSLHostConfigCertificate(
                            sslHostConfig, SSLHostConfigCertificate.Type.RSA);
                    cert.setCertificateKeystore(keyStore);
                    cert.setCertificateKeystorePassword(keystorePassword);
                    sslHostConfig.addCertificate(cert);

                    connector.addSslHostConfig(sslHostConfig);
                } catch (Exception e) {
                    throw new RuntimeException("Error al configurar SSL: " + e.getMessage(), e);
                }
            });
        };
    }
}
