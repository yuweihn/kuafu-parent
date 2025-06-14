package com.yuweix.kuafu.core.mq.rabbit;


import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * @author yuwei
 */
@ConfigurationProperties(prefix = "kuafu.rabbit", ignoreUnknownFields = true)
public class RabbitProperties {
    private String addresses;
    private String username;
    private String password;
    private String vhost;
    private String publisherConfirmType;


    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    public String getPublisherConfirmType() {
        return publisherConfirmType;
    }

    public void setPublisherConfirmType(String publisherConfirmType) {
        this.publisherConfirmType = publisherConfirmType;
    }
}
