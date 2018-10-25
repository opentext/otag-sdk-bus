package com.opentext.otag.sdk.types.v4;

import java.io.Serializable;
import java.util.Objects;

public class MobileZipRequest implements Serializable {

    private String otagToken;
    private String bearerToken;

    public MobileZipRequest(String otagToken, String bearerToken) {
        this.otagToken = otagToken;
        this.bearerToken = bearerToken;
    }

    public String getOtagToken() {
        return otagToken;
    }

    public void setOtagToken(String otagToken) {
        this.otagToken = otagToken;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MobileZipRequest that = (MobileZipRequest) o;
        return Objects.equals(otagToken, that.otagToken) &&
                Objects.equals(bearerToken, that.bearerToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(otagToken, bearerToken);
    }

    @Override
    public String toString() {
        return "MobileZipRequest{" +
                "otagToken='" + otagToken + '\'' +
                ", bearerToken='" + bearerToken + '\'' +
                '}';
    }
}
