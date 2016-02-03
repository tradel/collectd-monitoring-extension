/**
 * @author Todd Radel <tradel@appdynamics.com>
 */

package com.appdynamics.extensions.collectd.config;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Configuration {
    private String metricPrefix;
    private String listenAddress;
    private String interfaceAddress;
    private boolean sendOnlyChanges = false;

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }

    public String getListenAddress() {
        return listenAddress;
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    public String getInterfaceAddress() {
        return interfaceAddress;
    }

    public void setInterfaceAddress(String interfaceAddress) {
        this.interfaceAddress = interfaceAddress;
    }

    public boolean isSendOnlyChanges() {
        return sendOnlyChanges;
    }

    public void setSendOnlyChanges(boolean sendOnlyChanges) {
        this.sendOnlyChanges = sendOnlyChanges;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
