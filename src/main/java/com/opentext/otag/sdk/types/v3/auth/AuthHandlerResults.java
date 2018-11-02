/**
 * Copyright © 2016 Open Text.  All Rights Reserved.
 */
package com.opentext.otag.sdk.types.v3.auth;

import com.opentext.otag.sdk.util.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuthHandlerResults implements Serializable {

    private List<AuthHandlerResult> results = new ArrayList<>();

    public AuthHandlerResults() {
    }

    public void addResult(AuthHandlerResult result) {
        results.add(result);
    }

    public List<AuthHandlerResult> getResults() {
        return results;
    }

    @Override
    public String toString() {
        return "AuthHandlerResults{" +
                "results=" + (results != null ? StringUtil.toListString(
                results.stream().map(AuthHandlerResult::toString).collect(Collectors.toSet())) : "{}") +
                '}';
    }
}
