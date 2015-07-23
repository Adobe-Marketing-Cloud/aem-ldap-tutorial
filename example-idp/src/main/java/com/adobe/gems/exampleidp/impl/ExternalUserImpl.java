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

import java.util.Map;

import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentityRef;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalUser;

/**
 * {@code ExternalUserImpl}...
 */
public class ExternalUserImpl extends ExternalIdentityImpl implements ExternalUser {

    public ExternalUserImpl(JsonFileIdentityProvider provider, ExternalIdentityRef ref, String id, Map<String, Object> properties) {
        super(provider, ref, id, properties);
    }
}