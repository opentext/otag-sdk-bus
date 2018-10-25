/**
 * Copyright © 2016 Open Text.  All Rights Reserved.
 */
package com.opentext.otag.sdk.types.v3.message;

import com.opentext.otag.sdk.types.v3.OtagServiceEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * AppWorks Gateway message used to inform a deployment about a change to a
 * Setting entry in the Gateway's database they might be interested in.
 *
 * @author Rhys Evans rhyse@opentext.com
 * @version 16.2
 */
public class SettingsChangeMessage extends OtagMessageImpl {

    public static final HashSet<OtagServiceEvent> SETTINGS_EVENTS =
            new HashSet<>(Collections.singletonList(OtagServiceEvent.SETTING_UPDATED));

    private String key;

    private String newValue;

    public SettingsChangeMessage() {
    }

    public SettingsChangeMessage(String key, String newValue) {
        this.key = key;
        this.newValue = newValue;
    }

    public SettingsChangeMessage(OtagServiceEvent event, String key, String newValue) {
        super(event);
        this.key = key;
        this.newValue = newValue;
    }

    @Override
    public Set<OtagServiceEvent> getSupportedTypes() {
        return SETTINGS_EVENTS;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SettingsChangeMessage that = (SettingsChangeMessage) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        return newValue != null ? newValue.equals(that.newValue) : that.newValue == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SettingsChangeMessage{" +
                "key='" + key + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }

}
