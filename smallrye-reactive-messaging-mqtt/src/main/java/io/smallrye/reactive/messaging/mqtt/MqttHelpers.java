package io.smallrye.reactive.messaging.mqtt;

import java.time.Duration;
import java.util.Optional;

import io.smallrye.reactive.messaging.mqtt.session.ConstantReconnectDelayOptions;
import io.smallrye.reactive.messaging.mqtt.session.MqttClientSessionOptions;
import io.smallrye.reactive.messaging.mqtt.session.ReconnectDelayOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.core.net.TrustOptions;

public class MqttHelpers {

    private MqttHelpers() {
        // avoid direct instantiation.
    }

    static MqttClientSessionOptions createMqttClientOptions(MqttConnectorCommonConfiguration config) {
        MqttClientSessionOptions options = new MqttClientSessionOptions();
        options.setCleanSession(config.getAutoCleanSession());
        options.setAutoGeneratedClientId(config.getAutoGeneratedClientId());
        options.setAutoKeepAlive(config.getAutoKeepAlive());
        options.setClientId(config.getClientId().orElse(null));
        options.setConnectTimeout(config.getConnectTimeoutSeconds());
        options.setHostname(config.getHost());
        options.setKeepAliveInterval(config.getKeepAliveSeconds());
        options.setMaxInflightQueue(config.getMaxInflightQueue());
        options.setMaxMessageSize(config.getMaxMessageSize());
        options.setPassword(config.getPassword().orElse(null));
        options.setPort(config.getPort().orElseGet(() -> config.getSsl() ? 8883 : 1883));
        options.setReconnectDelay(getReconnectDelayOptions(config));
        options.setSsl(config.getSsl());
        options.setKeyCertOptions(getKeyCertOptions(config));
        options.setServerName(config.getServerName());
        options.setTrustOptions(getTrustOptions(config));
        options.setTrustAll(config.getTrustAll());
        options.setUsername(config.getUsername().orElse(null));
        options.setWillQoS(config.getWillQos());
        options.setWillFlag(config.getWillFlag());
        options.setWillRetain(config.getWillRetain());
        return options;
    }

    /**
     * Create KeyCertOptions value from the configuration.
     * Attribute Name: ssl.keystore
     * Description: Set whether keystore type, location and password. In case of pem type the location and password are the cert
     * and key path.
     * Default Value: PfxOptions
     *
     * @return the KeyCertOptions
     */
    private static KeyCertOptions getKeyCertOptions(MqttConnectorCommonConfiguration config) {
        Optional<String> sslKeystoreLocation = config.getSslKeystoreLocation();
        Optional<String> sslKeystorePassword = config.getSslKeystorePassword();
        if (config.getSsl() && sslKeystoreLocation.isPresent()) {
            String keyStoreLocation = sslKeystoreLocation.get();
            String sslKeystoreType = config.getSslKeystoreType();

            if (sslKeystorePassword.isPresent()) {
                String keyStorePassword = sslKeystorePassword.get();
                if ("jks".equalsIgnoreCase(sslKeystoreType)) {
                    return new JksOptions()
                            .setPath(keyStoreLocation)
                            .setPassword(keyStorePassword);
                } else if ("pem".equalsIgnoreCase(sslKeystoreType)) {
                    return new PemKeyCertOptions()
                            .setCertPath(keyStoreLocation)
                            .setKeyPath(keyStorePassword);
                }
                // Default
                return new PfxOptions()
                        .setPath(keyStoreLocation)
                        .setPassword(keyStorePassword);
            } else {
                throw new IllegalArgumentException(
                        "The attribute `ssl.keystore.password` on connector 'smallrye-mqtt' (channel: "
                                + config.getChannel() + ") must be set for `ssl.keystore.type`" + sslKeystoreType);
            }
        }
        return null;
    }

    /**
     * Gets the truststore value from the configuration.
     * Attribute Name: ssl.truststore
     * Description: Set whether keystore type, location and password. In case of pem type the location is the cert path.
     * Default Value: PfxOptions
     *
     * @return the TrustOptions
     */

    private static TrustOptions getTrustOptions(MqttConnectorCommonConfiguration config) {
        Optional<String> sslTruststoreLocation = config.getSslTruststoreLocation();
        Optional<String> sslTruststorePassword = config.getSslTruststorePassword();
        if (config.getSsl() && sslTruststoreLocation.isPresent()) {
            String truststoreLocation = sslTruststoreLocation.get();
            String truststoreType = config.getSslTruststoreType();

            if ("pem".equalsIgnoreCase(truststoreType)) {
                return new PemTrustOptions()
                        .addCertPath(truststoreLocation);
            } else {
                if (sslTruststorePassword.isPresent()) {
                    String truststorePassword = sslTruststorePassword.get();
                    if ("jks".equalsIgnoreCase(truststoreType)) {
                        return new JksOptions()
                                .setPath(truststoreLocation)
                                .setPassword(truststorePassword);
                    }
                    // Default
                    return new PfxOptions()
                            .setPath(truststoreLocation)
                            .setPassword(truststorePassword);
                } else {
                    throw new IllegalArgumentException(
                            "The attribute `ssl.keystore.password` on connector 'smallrye-mqtt' (channel: "
                                    + config.getChannel() + ") must be set for `ssl.keystore.type`" + truststoreType);
                }
            }
        }
        return null;
    }

    private static ReconnectDelayOptions getReconnectDelayOptions(MqttConnectorCommonConfiguration config) {
        ConstantReconnectDelayOptions options = new ConstantReconnectDelayOptions();
        options.setDelay(Duration.ofSeconds(config.getReconnectIntervalSeconds()));
        return options;
    }

}
