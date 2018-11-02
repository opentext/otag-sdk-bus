package com.opentext.otag.sdk.types.v3;

import java.io.Serializable;

/**
 * Representation of an individual AppWorks app/service that relays
 * whether the Gateway has enabled it for external access.
 */
public class AppData implements Serializable {

    private boolean enabled;

    public AppData() {
    }

    public AppData(boolean isEnabled) {
        this.enabled = isEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "AppData{" +
                "enabled=" + enabled +
                '}';
    }
}
