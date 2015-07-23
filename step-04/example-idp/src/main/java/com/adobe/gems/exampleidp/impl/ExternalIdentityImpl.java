/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
 * {@code ExternalIdentityImpl} implements an external identity based on properties
 */
public abstract class ExternalIdentityImpl implements ExternalIdentity {

    protected final String providerName;

    protected final ExternalIdentityRef ref;

    protected final String id;

    private Set<ExternalIdentityRef> groups;

    protected final Map<String, Object> properties;

    protected ExternalIdentityImpl(String providerName, ExternalIdentityRef ref, String id, Map<String, Object> properties) {
        this.providerName = providerName;
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
            JSONArray gs = (JSONArray) properties.get(JsonFileIdentityProvider.PN_GROUPS);
            if (gs != null) {
                for (int i = 0; i<gs.length(); i++) {
                    String gid = gs.optString(i);
                    if (gid != null) {
                        groups.add(new ExternalIdentityRef(gid, providerName));
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

    @Override
    public String toString() {
        return "ExternalIdentityImpl{" + "ref=" + ref + ", id='" + id + '\'' + '}';
    }

}