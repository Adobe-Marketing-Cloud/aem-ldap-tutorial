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

import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalGroup;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentityException;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentityRef;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalUser;
import org.apache.sling.commons.json.JSONArray;

/**
 * {@code ExternalGroupImpl}...
 */
public class ExternalGroupImpl extends ExternalIdentityImpl implements ExternalGroup {

    private Set<ExternalIdentityRef> members;

    public ExternalGroupImpl(JsonFileIdentityProvider provider, ExternalIdentityRef ref, String id, Map<String, Object> properties) {
        super(provider, ref, id, properties);
    }

    @Nonnull
    @Override
    public Iterable<ExternalIdentityRef> getDeclaredMembers() throws ExternalIdentityException {
        if (members == null) {
            members = new HashSet<ExternalIdentityRef>();
            JSONArray gs = (JSONArray) properties.get("members");
            if (gs != null) {
                for (int i = 0; i<gs.length(); i++) {
                    String gid = gs.optString(i);
                    if (gid != null) {
                        members.add(new ExternalIdentityRef(gid, provider.getName()));
                    }
                }
            }
        }
        return members;
    }
}