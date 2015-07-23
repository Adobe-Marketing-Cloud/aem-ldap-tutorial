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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.oak.spi.security.ConfigurationParameters;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalGroup;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentity;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentityException;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentityProvider;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentityRef;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalUser;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code JsonFileIdentityProvider} implements an external identity provider that reads users and groups from
 * a simple json file. the structure is very simple, using the authorizable id as key. if the object has a 'members' property,
 * it is considered a group. all password are just plaintext.
 *
 * for simpler group membership lookup, we also store the groups in the user objects.
 *
 * Example:
 *
 * <xmp>
 * {
 *     "enterprise": {
 *         "id": "enterprise",
 *         "members": ["kirk", "spock"]
 *     },
 *     "kirk": {
 *         "id": "kirk",
 *         "fullname": "James T. Kirk",
 *         "password": "pass",
 *         "groups": ["enterprise"]
 *     },
 *     "spock": {
 *         "id": "spock",
 *         "fullname": "Spock",
 *         "password": "pass",
 *         "groups": ["enterprise"]
 *     }
 * }
 * </xmp>
 */
@Component(
        label = "JSON File Identity Provider",
        configurationFactory = true,
        metatype = true,
        policy = ConfigurationPolicy.REQUIRE
)
@Service
public class JsonFileIdentityProvider implements ExternalIdentityProvider {

    /**
     * property name for members
     */
    public static final String PN_MEMBERS = "members";

    /**
     * property name for groups
     */
    public static final String PN_GROUPS = "groups";

    /**
     * property name for password
     */
    public static final String PN_PASSWORD = "password";

    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(JsonFileIdentityProvider.class);

    /**
     * @see #getName()
     */
    public static final String PARAM_NAME_DEFAULT = "json";

    /**
     * @see #getName()
     */
    @Property(
            label = "Provider Name",
            description = "Name of this provider configuration. This is used to reference this provider by the login modules.",
            value = PARAM_NAME_DEFAULT
    )
    public static final String PARAM_NAME = "provider.name";

    /**
     * The default value of the json file
     */
    public static final String PARAM_FILE_NAME_DEFAULT = "authorizables.json";

    /**
     * The property for the json file
     */
    @Property(
            label = "JSON Filename",
            description = "Filename (path) of the json file that stores the user and group information.",
            value = PARAM_FILE_NAME_DEFAULT
    )
    public static final String PARAM_FILE_NAME = "filename";

    /**
     * name of this provider
     */
    private String name;

    /**
     * configured filename
     */
    private String fileName;

    /**
     * resolved json file
     */
    private File jsonFile;

    @SuppressWarnings("UnusedDeclaration")
    @Activate
    private void activate(Map<String, Object> properties) {
        ConfigurationParameters cfg = ConfigurationParameters.of(properties);
        name = cfg.getConfigValue(PARAM_NAME, PARAM_NAME_DEFAULT);
        fileName = cfg.getConfigValue(PARAM_FILE_NAME, PARAM_FILE_NAME_DEFAULT);
        init();
    }

    @SuppressWarnings("UnusedDeclaration")
    @Deactivate
    private void deactivate() {
    }

    /**
     * Initialized the provider and validates the properties.
     */
    private void init() {
        if (jsonFile == null || !jsonFile.exists()) {
            try {
                jsonFile = new File(fileName).getCanonicalFile();
                log.info("json file IDP initialized. using file: {}", jsonFile.getPath());
            } catch (IOException e) {
                jsonFile = null;
                log.warn("error while initializing json file IDP. ", e);
            }
        }
    }

    /**
     * Loads the authorizable JSON.
     * @return the JSON object of the data.
     * @throws IOException if an error occurrs
     */
    @Nonnull
    private JSONObject loadJSON() throws IOException {
        init();
        if (jsonFile != null) {
            String json = FileUtils.readFileToString(jsonFile, "utf-8");
            try {
                return new JSONObject(json);
            } catch (JSONException e) {
                log.error("error while parsing json {}", fileName, e);
                throw new IOException("Error while parsing json");
            }
        } else {
            throw new FileNotFoundException("JSON file not found: " + fileName);
        }
    }

    /**
     * Checks if the given identity reference has the same provider name as this one.
     * @param ref the reference
     * @return {@code true} if the reference originates from this provider.
     */
    private boolean isMyRef(@Nonnull ExternalIdentityRef ref) {
        final String refProviderName = ref.getProviderName();
        return refProviderName == null || refProviderName.isEmpty() || getName().equals(refProviderName);
    }

    /**
     * Creates a new external identity of the given {@code type} or {@code null} if the type does not match the object
     * @param id the id
     * @param ref the extern reference or {@code null}
     * @param obj the json data
     * @param type the desired type
     * @return the new identity or {@code null}
     * @throws JSONException
     */
    @CheckForNull
    private <T> T createIdentity(@Nonnull String id, @Nullable ExternalIdentityRef ref,
                                 @Nullable JSONObject obj, @Nonnull Class<T> type) throws JSONException {
        if (obj == null) {
            return null;
        }
        if ((type == ExternalGroup.class || type == ExternalIdentity.class) && obj.has(PN_MEMBERS)) {
            if (ref == null) {
                ref = new ExternalIdentityRef(id, getName());
            }
            //noinspection unchecked
            return (T) new ExternalGroupImpl(getName(), ref, id, convertJSONtoMap(obj));

        } else if ((type == ExternalUser.class || type == ExternalIdentity.class) && !obj.has(PN_MEMBERS)) {
            if (ref == null) {
                ref = new ExternalIdentityRef(id, getName());
            }
            //noinspection unchecked
            return (T) new ExternalUserImpl(getName(), ref, id, convertJSONtoMap(obj));
        } else {
            return null;
        }
    }

    /**
     * Returns an iterator over all identities of the given type
     * @param type the type
     * @return an iterator
     * @throws ExternalIdentityException
     */
    @Nonnull
    private <T> Iterator<T> listIdentities(@Nonnull Class<T> type) throws ExternalIdentityException {
        try {
            List<T> identities = new ArrayList<T>();
            JSONObject obj = loadJSON();
            JSONArray names = obj.names();
            for (int i=0; i<names.length(); i++) {
                String id = names.getString(i);
                T identity = createIdentity(id, null, obj.getJSONObject(id), type);
                if (identity != null) {
                    identities.add(identity);
                }
            }
            return identities.iterator();
        } catch (Exception e) {
            throw new ExternalIdentityException();
        }
    }


    /**
     * Simple helper that converts the given json object into a hash map non recursively.
     * @param obj the json data
     * @return a map for the data
     * @throws JSONException if an error occurrs
     */
    @Nonnull
    private Map<String, Object> convertJSONtoMap(@Nonnull JSONObject obj) throws JSONException {
        Map<String, Object> props = new HashMap<String, Object>();
        JSONArray names = obj.names();
        for (int i=0; i<names.length(); i++) {
            String name = names.getString(i);
            Object o = obj.get(name);
            props.put(name, o);
        }
        return props;
    }

    /**
     * Returns the name of this provider.
     * @return the provider name.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Returns the identity for the given reference or {@code null} if it does not exist. The provider should check if
     * the {@link ExternalIdentityRef#getProviderName() provider name} matches his own name or is {@code null} and
     * should not return a foreign identity.
     *
     * @param ref the reference
     * @return an identity or {@code null}
     *
     * @throws ExternalIdentityException if an error occurs.
     */
    @CheckForNull
    public ExternalIdentity getIdentity(@Nonnull ExternalIdentityRef ref) throws ExternalIdentityException {
        try {
            if (!isMyRef(ref)) {
                return null;
            }
            JSONObject obj = loadJSON().optJSONObject(ref.getId());
            return createIdentity(ref.getId(), ref, obj, ExternalIdentity.class);
        } catch (Exception e) {
            throw new ExternalIdentityException(e);
        }
    }

    /**
     * Returns the user for the given (local) id. if the user does not exist {@code null} is returned.
     * @param userId the user id.
     * @return the user or {@code null}
     *
     * @throws ExternalIdentityException if an error occurs.
     */
    @CheckForNull
    public ExternalUser getUser(@Nonnull String userId) throws ExternalIdentityException {
        try {
            JSONObject userObj = loadJSON().optJSONObject(userId);
            return createIdentity(userId, null, userObj, ExternalUser.class);
        } catch (Exception e) {
            throw new ExternalIdentityException(e);
        }
    }

    /**
     * Authenticates the user represented by the given credentials and returns it. If the user does not exist in this
     * provider, {@code null} is returned. If the authentication fails, a LoginException is thrown.
     *
     * @param credentials the credentials
     * @return the user or {@code null}
     * @throws ExternalIdentityException if an error occurs
     * @throws javax.security.auth.login.LoginException if the user could not be authenticated
     */
    @CheckForNull
    public ExternalUser authenticate(@Nonnull Credentials credentials) throws ExternalIdentityException, LoginException {
        if (!(credentials instanceof SimpleCredentials)) {
            throw new LoginException("invalid credentials class " + credentials.getClass());
        }
        try {
            // extract the user id from the credentials and lookup the user
            SimpleCredentials sc = (SimpleCredentials) credentials;
            JSONObject userObj = loadJSON().optJSONObject(sc.getUserID());

            // if the user does not exist, return null
            if (userObj == null) {
                log.debug("authenticate: user '{}' not found in json file", sc.getUserID());
                return null;
            }
            log.debug("authenticate: user '{}' found in json file.", sc.getUserID());

            // verify the password and throw login exception on mismatch
            String pwd = userObj.optString(PN_PASSWORD, "");
            if (pwd.equals(new String(sc.getPassword()))) {
                // if all good, return the user as external identity
                log.debug("authenticate: users '{}' credentials validated.", sc.getUserID());
                return createIdentity(sc.getUserID(), null, userObj, ExternalUser.class);
            } else {
                throw new LoginException("invalid user or password");
            }
        } catch (IOException e) {
            throw new ExternalIdentityException(e);
        } catch (JSONException e) {
            throw new ExternalIdentityException(e);
        }
    }

    /**
     * Returns the group for the given (local) group name. if the group does not exist {@code null} is returned.
     * @param name the group name
     * @return the group or {@code null}
     *
     * @throws ExternalIdentityException if an error occurs.
     */
    @CheckForNull
    public ExternalGroup getGroup(@Nonnull String name) throws ExternalIdentityException {
        try {
            JSONObject grpObj = loadJSON().optJSONObject(name);
            return createIdentity(name, null, grpObj, ExternalGroup.class);
        } catch (Exception e) {
            throw new ExternalIdentityException(e);
        }

    }

    /**
     * List all external users.
     * @return an iterator over all external users
     * @throws ExternalIdentityException if an error occurs.
     */
    @Nonnull
    public Iterator<ExternalUser> listUsers() throws ExternalIdentityException {
        return listIdentities(ExternalUser.class);
    }

    /**
     * List all external groups.
     * @return an iterator over all external groups
     * @throws ExternalIdentityException if an error occurs.
     */
    @Nonnull
    public Iterator<ExternalGroup> listGroups() throws ExternalIdentityException {
        return listIdentities(ExternalGroup.class);
    }

}