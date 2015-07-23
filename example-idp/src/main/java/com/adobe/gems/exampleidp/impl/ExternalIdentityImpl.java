/*************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 * <p/>
 * Copyright ${today.year} Adobe Systems Incorporated
 * All Rights Reserved.
 * <p/>
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
package com.adobe.gems.exampleidp.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentity;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentityException;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentityRef;
import org.apache.sling.commons.json.JSONArray;

/**
 * {@code ExternalIdentityImpl}...
 */
public abstract class ExternalIdentityImpl implements ExternalIdentity {

    protected final JsonFileIdentityProvider provider;

    protected final ExternalIdentityRef ref;

    protected final String id;

    private Set<ExternalIdentityRef> groups;

    protected final Map<String, Object> properties;

    protected ExternalIdentityImpl(JsonFileIdentityProvider provider, ExternalIdentityRef ref, String id, Map<String, Object> properties) {
        this.provider = provider;
        this.ref = ref;
        this.id = id;
        this.properties = properties;
    }


    @Nonnull
    @Override
    public ExternalIdentityRef getExternalId() {
        return ref;
    }

    @Nonnull
    @Override
    public String getId() {
        return id;
    }

    @Nonnull
    @Override
    public String getPrincipalName() {
        return ref.getId();
    }

    @Override
    public String getIntermediatePath() {
        return null;
    }

    @Nonnull
    @Override
    public Iterable<ExternalIdentityRef> getDeclaredGroups() throws ExternalIdentityException {
        if (groups == null) {
            groups = new HashSet<ExternalIdentityRef>();
            JSONArray gs = (JSONArray) properties.get("groups");
            if (gs != null) {
                for (int i = 0; i<gs.length(); i++) {
                    String gid = gs.optString(i);
                    if (gid != null) {
                        groups.add(new ExternalIdentityRef(gid, provider.getName()));
                    }
                }
            }
        }
        return groups;
    }

    @Nonnull
    @Override
    public Map<String, ?> getProperties() {
        return properties;
    }
}