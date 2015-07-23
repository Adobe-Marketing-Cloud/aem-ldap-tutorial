Log snippet during initial successfull login
--------------------------------------------

````
22.07.2015 16:41:08.186 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.security.authentication.ldap.impl.LdapIdentityProvider search below ou=people,o=SevenSeas with (&(uid=wbush)(objectclass=person)) found cn=William Bush,ou=people,o=SevenSeas
22.07.2015 16:41:08.186 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.security.authentication.ldap.impl.LdapIdentityProvider getUser(wbush) (connect=1.92ms, lookup=3.03ms)
22.07.2015 16:41:08.186 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.security.authentication.ldap.impl.PoolableUnboundConnectionFactory activate connection: org.apache.directory.ldap.client.api.LdapNetworkConnection@3aa49064
22.07.2015 16:41:08.188 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.security.authentication.ldap.impl.PoolableUnboundConnectionFactory validating connection org.apache.directory.ldap.client.api.LdapNetworkConnection@3aa49064: true
22.07.2015 16:41:08.196 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.security.authentication.ldap.impl.LdapIdentityProvider authenticate(wbush) (connect=1.61ms, bind=8.30ms)
22.07.2015 16:41:08.196 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.security.authentication.ldap.impl.PoolableUnboundConnectionFactory passivate connection: org.apache.directory.ldap.client.api.LdapNetworkConnection@3aa49064
22.07.2015 16:41:08.196 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.ExternalLoginModule IDP ldap returned valid user LdapIdentity{ref=ExternalIdentityRef{id='cn=William Bush,ou=people,o=SevenSeas', providerName='ldap'}, id='wbush'}
22.07.2015 16:41:08.202 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler Properties of user 'wbush' need sync. rep:lastSynced not set.
22.07.2015 16:41:08.203 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler Membership of user 'wbush' need sync. rep:lastSynced not set.
22.07.2015 16:41:08.203 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler Syncing membership 'cn=William Bush,ou=people,o=SevenSeas;ldap' -> 'wbush'
22.07.2015 16:41:08.210 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.security.authentication.ldap.impl.LdapIdentityProvider search below ou=groups,o=SevenSeas with (&(uniquemember=cn=William Bush,ou=people,o=SevenSeas)(objectclass=groupOfUniqueNames)) found 1 entries. (connect=2.34ms, search=226.00us, iterate=3.28ms)
22.07.2015 16:41:08.211 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler - processing membership cn=HMS Lydia,ou=crews,ou=groups,o=SevenSeas
22.07.2015 16:41:08.215 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler - idp returned 'HMS Lydia'
22.07.2015 16:41:08.216 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler - user manager returned 'null'
22.07.2015 16:41:08.217 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler - created new group
22.07.2015 16:41:08.217 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler Properties of group 'HMS Lydia' need sync. rep:lastSynced not set.
22.07.2015 16:41:08.218 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler - added 'User 'wbush'' as member to 'Group 'HMS Lydia''
22.07.2015 16:41:08.218 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler - group nesting level for 'HMS Lydia' reached
22.07.2015 16:41:08.219 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler syncMembership(wbush) (fetching=6.26ms, reading=1.27ms, adding=7.41ms, removing=25.00us)
22.07.2015 16:41:08.219 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler sync(cn=William Bush,ou=people,o=SevenSeas;ldap) -> wbush (find=848.00us, create=2.03ms, sync=16.78ms)
22.07.2015 16:41:08.223 *DEBUG* [qtp1188867090-847] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.ExternalLoginModule syncUser(wbush) (sync=21.99ms, commit=4.05ms)
````

Log snippet when user (foo) does not exist:
-------------------------------------
````
22.07.2015 17:00:30.029 *DEBUG* [qtp1188867090-843] org.apache.jackrabbit.oak.security.authentication.ldap.impl.LdapIdentityProvider search below ou=people,o=SevenSeas with (&(uid=foo)(objectclass=person)) found 0 entries.
22.07.2015 17:00:30.029 *DEBUG* [qtp1188867090-843] org.apache.jackrabbit.oak.security.authentication.ldap.impl.LdapIdentityProvider getUser(foo) (connect=1.88ms, lookup=3.31ms)
22.07.2015 17:00:30.029 *DEBUG* [qtp1188867090-843] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.ExternalLoginModule IDP ldap returned null for simple creds of foo
````

Log snippet for invalid password:
---------------------------------
````
22.07.2015 17:02:04.886 *DEBUG* [qtp1188867090-843] org.apache.jackrabbit.oak.security.authentication.ldap.impl.LdapIdentityProvider search below ou=people,o=SevenSeas with (&(uid=wbush)(objectclass=person)) found cn=William Bush,ou=people,o=SevenSeas
22.07.2015 17:02:04.886 *DEBUG* [qtp1188867090-843] org.apache.jackrabbit.oak.security.authentication.ldap.impl.LdapIdentityProvider getUser(wbush) (connect=1.79ms, lookup=2.97ms)
22.07.2015 17:02:04.886 *DEBUG* [qtp1188867090-843] org.apache.jackrabbit.oak.security.authentication.ldap.impl.PoolableUnboundConnectionFactory activate connection: org.apache.directory.ldap.client.api.LdapNetworkConnection@3aa49064
22.07.2015 17:02:04.887 *DEBUG* [qtp1188867090-843] org.apache.jackrabbit.oak.security.authentication.ldap.impl.PoolableUnboundConnectionFactory validating connection org.apache.directory.ldap.client.api.LdapNetworkConnection@3aa49064: true
22.07.2015 17:02:04.894 *DEBUG* [qtp1188867090-843] org.apache.jackrabbit.oak.security.authentication.ldap.impl.PoolableUnboundConnectionFactory passivate connection: org.apache.directory.ldap.client.api.LdapNetworkConnection@3aa49064
22.07.2015 17:02:04.894 *DEBUG* [qtp1188867090-843] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.ExternalLoginModule IDP ldap throws login exception for 'wbush': Unable to authenticate against LDAP server: INVALID_CREDENTIALS: Bind failed: ERR_229 Cannot authenticate user cn=William Bush,ou=people,o=SevenSeas:
org.apache.directory.api.ldap.model.exception.LdapAuthenticationException: ERR_229 Cannot authenticate user cn=William Bush,ou=people,o=SevenSeas
	at org.apache.directory.server.core.authn.AuthenticationInterceptor.bind(AuthenticationInterceptor.java:671)
	at org.apache.directory.server.core.DefaultOperationManager.bind(DefaultOperationManager.java:439)
	at org.apache.directory.server.ldap.handlers.request.BindRequestHandler.handleSimpleAuth(BindRequestHandler.java:184)
	at org.apache.directory.server.ldap.handlers.request.BindRequestHandler.handle(BindRequestHandler.java:636)
	at org.apache.directory.server.ldap.handlers.request.BindRequestHandler.handle(BindRequestHandler.java:66)
	at org.apache.directory.server.ldap.handlers.LdapRequestHandler.handleMessage(LdapRequestHandler.java:193)
	at org.apache.directory.server.ldap.handlers.LdapRequestHandler.handleMessage(LdapRequestHandler.java:56)
	at org.apache.mina.handler.demux.DemuxingIoHandler.messageReceived(DemuxingIoHandler.java:221)
	at org.apache.directory.server.ldap.LdapProtocolHandler.messageReceived(LdapProtocolHandler.java:217)
	at org.apache.mina.core.filterchain.DefaultIoFilterChain$TailFilter.messageReceived(DefaultIoFilterChain.java:854)
	at org.apache.mina.core.filterchain.DefaultIoFilterChain.callNextMessageReceived(DefaultIoFilterChain.java:542)
	at org.apache.mina.core.filterchain.DefaultIoFilterChain.access$1300(DefaultIoFilterChain.java:48)
	at org.apache.mina.core.filterchain.DefaultIoFilterChain$EntryImpl$1.messageReceived(DefaultIoFilterChain.java:943)
	at org.apache.mina.core.filterchain.IoFilterEvent.fire(IoFilterEvent.java:74)
	at org.apache.mina.core.session.IoEvent.run(IoEvent.java:63)
	at org.apache.mina.filter.executor.UnorderedThreadPoolExecutor$Worker.runTask(UnorderedThreadPoolExecutor.java:475)
	at org.apache.mina.filter.executor.UnorderedThreadPoolExecutor$Worker.run(UnorderedThreadPoolExecutor.java:429)
	at java.lang.Thread.run(Thread.java:744)


BindRequest =
MessageType : BIND_REQUEST
Message ID : 16
    BindRequest
        Version : '3'
        Name : 'cn=William Bush,ou=people,o=SevenSeas'
        Simple authentication : '(omitted-for-safety)'

22.07.2015 17:02:04.894 *INFO* [qtp1188867090-843] org.apache.sling.auth.core.impl.SlingAuthenticator handleLoginFailure: Unable to authenticate null: UserId/Password mismatch.
````

Log snippet when user is resynced
---------------------------------
````
22.07.2015 17:27:26.663 *DEBUG* [qtp1188867090-1077] org.apache.jackrabbit.oak.security.authentication.ldap.impl.LdapIdentityProvider search below ou=people,o=SevenSeas with (&(uid=wbush)(objectclass=person)) found cn=William Bush,ou=people,o=SevenSeas
22.07.2015 17:27:26.664 *DEBUG* [qtp1188867090-1077] org.apache.jackrabbit.oak.security.authentication.ldap.impl.LdapIdentityProvider getUser(wbush) (connect=1.79ms, lookup=2.87ms)
22.07.2015 17:27:26.664 *DEBUG* [qtp1188867090-1077] org.apache.jackrabbit.oak.security.authentication.ldap.impl.PoolableUnboundConnectionFactory activate connection: org.apache.directory.ldap.client.api.LdapNetworkConnection@3aa49064
22.07.2015 17:27:26.665 *DEBUG* [qtp1188867090-1077] org.apache.jackrabbit.oak.security.authentication.ldap.impl.PoolableUnboundConnectionFactory validating connection org.apache.directory.ldap.client.api.LdapNetworkConnection@3aa49064: true
22.07.2015 17:27:26.668 *DEBUG* [qtp1188867090-1077] org.apache.jackrabbit.oak.security.authentication.ldap.impl.LdapIdentityProvider authenticate(wbush) (connect=1.46ms, bind=2.83ms)
22.07.2015 17:27:26.668 *DEBUG* [qtp1188867090-1077] org.apache.jackrabbit.oak.security.authentication.ldap.impl.PoolableUnboundConnectionFactory passivate connection: org.apache.directory.ldap.client.api.LdapNetworkConnection@3aa49064
22.07.2015 17:27:26.668 *DEBUG* [qtp1188867090-1077] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.ExternalLoginModule IDP ldap returned valid user LdapIdentity{ref=ExternalIdentityRef{id='cn=William Bush,ou=people,o=SevenSeas', providerName='ldap'}, id='wbush'}
22.07.2015 17:27:26.669 *DEBUG* [qtp1188867090-1077] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler Properties of user 'wbush' need sync. rep:lastSynced expired (2113690 > 10000)
22.07.2015 17:27:26.670 *DEBUG* [qtp1188867090-1077] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler Membership of user 'wbush' do not need sync.
22.07.2015 17:27:26.670 *DEBUG* [qtp1188867090-1077] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler sync(cn=William Bush,ou=people,o=SevenSeas;ldap) -> wbush (find=436.00us, sync=1.24ms)
22.07.2015 17:27:26.671 *DEBUG* [qtp1188867090-1077] org.apache.jackrabbit.oak.spi.security.authentication.external.impl.ExternalLoginModule syncUser(wbush) (sync=1.79ms, commit=826.00us)
````

