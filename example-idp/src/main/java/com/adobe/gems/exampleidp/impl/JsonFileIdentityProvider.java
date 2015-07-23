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

    private String name;

    private String fileName;

    private File jsonFile;

    //----------------------------------------------------< SCR integration >---
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

    private boolean isMyRef(@Nonnull ExternalIdentityRef ref) {
        final String refProviderName = ref.getProviderName();
        return refProviderName == null || refProviderName.isEmpty() || getName().equals(refProviderName);
    }

    private ExternalUser createUser(String id, JSONObject obj) throws JSONException {
        ExternalIdentityRef ref = new ExternalIdentityRef(id, this.getName());
        return new ExternalUserImpl(this, ref, id, convertJSONtoMap(obj));
    }

    private ExternalGroup createGroup(String id, JSONObject obj) throws JSONException {
        ExternalIdentityRef ref = new ExternalIdentityRef(id, this.getName());
        return new ExternalGroupImpl(this, ref, id, convertJSONtoMap(obj));
    }

    private Map<String, Object> convertJSONtoMap(JSONObject obj) throws JSONException {
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
            if (obj == null) {
                return null;
            } else if (obj.has("members")) {
                return createGroup(ref.getId(), obj);
            } else {
                return createUser(ref.getId(), obj);
            }
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
            if (userObj == null || userObj.has("members")) {
                return null;
            }
            return createUser(userId, userObj);
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
            SimpleCredentials sc = (SimpleCredentials) credentials;
            JSONObject userObj = loadJSON().optJSONObject(sc.getUserID());
            if (userObj == null) {
                return null;
            }
            String pwd = userObj.optString("password", "");
            if (pwd.equals(new String(sc.getPassword()))) {
                return createUser(sc.getUserID(), userObj);
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
            if (grpObj == null || !grpObj.has("members")) {
                return null;
            }
            return createGroup(name, grpObj);
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
        try {
            List<ExternalUser> users = new ArrayList<ExternalUser>();
            JSONObject obj = loadJSON();
            JSONArray names = obj.names();
            for (int i=0; i<names.length(); i++) {
                String id = names.getString(i);
                JSONObject authObj = obj.getJSONObject(id);
                if (!authObj.has("members")) {
                    users.add(createUser(id, authObj));
                }
            }
            return users.iterator();
        } catch (Exception e) {
            throw new ExternalIdentityException();
        }
    }

    /**
     * List all external groups.
     * @return an iterator over all external groups
     * @throws ExternalIdentityException if an error occurs.
     */
    @Nonnull
    public Iterator<ExternalGroup> listGroups() throws ExternalIdentityException {
        try {
            List<ExternalGroup> groups = new ArrayList<ExternalGroup>();
            JSONObject obj = loadJSON();
            JSONArray names = obj.names();
            for (int i=0; i<names.length(); i++) {
                String id = names.getString(i);
                JSONObject authObj = obj.getJSONObject(id);
                if (authObj.has("members")) {
                    groups.add(createGroup(id, authObj));
                }
            }
            return groups.iterator();
        } catch (Exception e) {
            throw new ExternalIdentityException();
        }
    }

}