AEM 6.1 LDAP Authentication Tutorial
====================================

((DRAFT))

Step 03 - Test that it works!
-----------------------------

In this step we verify that the LDAP authentication works and if the users are synced properly.

### Requirements
1. LDAP Server with example data (from step 01)
2. AEM 6.1 installation with configured LDAP authentication (from step 02)

#### 1. simple test - login as a ldap user
The first test is to login as a user that does not exist in the repository but only on the ldap.

- ensure to logout previous session or clear all browser cookie or use a different browser, hostname or IP
- open browser to aem: http://127.0.0.1:4502/
- login as `wbush` with password `pass`

if the login succeeds, you should now see the authoring environment because we configured the `user.autoMembership` to include the `contributor` group.

Looking at the users and groups should show the user _William Bush_ and his group:

- open the [AEM useradmin](http://localhost:4502/useradmin)
- search for `seven`

![image](test-01-useradmin.png)

If you look at the log files, you should see something like here: [log-snip-01.md](log-snip-01.md)

#### 2. add more config for first- and givenname
as you can see in the AEM user admin, the fields for _First Name_ and _Last Name_ are empty, because the useradmin reads the `profile/givenName` and `profile/familyName`.

so let's alter the config so that this information is populated as well.

- open the [Felix Configuration Manager](http://localhost:4502/system/console/configMgr) and search for the _"Default Sync Handler"_ factory config and click on the first confg to edit it

- change the value for _User property mapping_ and add the mappings.
- also lower the value for _User expiration time_ so we can see the effects sooner


Enter the following information:

| Name                          | Value
|-------------------------------|--------------------
| User Expiration Time          | `10s`
| User property mapping         | `rep:fullname=cn` <br> `profile/nt:primaryType="nt:unstructured"` <br> `profile/givenName=givenname` <br> `profile/familyName=sn` |

And save the config

todo: add screen shot of new config
todo: add screen shot of synced properties

