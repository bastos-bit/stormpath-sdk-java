/*
 * Copyright 2014 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.sdk.impl.tenant

import com.fasterxml.jackson.databind.util.ISO8601DateFormat
import com.stormpath.sdk.account.Account
import com.stormpath.sdk.account.Accounts
import com.stormpath.sdk.application.Application
import com.stormpath.sdk.application.ApplicationStatus
import com.stormpath.sdk.application.Applications
import com.stormpath.sdk.client.ClientIT
import com.stormpath.sdk.directory.Directories
import com.stormpath.sdk.directory.Directory
import com.stormpath.sdk.group.Group
import com.stormpath.sdk.group.Groups
import com.stormpath.sdk.lang.Duration
import com.stormpath.sdk.provider.*
import com.stormpath.sdk.tenant.Tenant
import org.testng.annotations.Test

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

import static org.testng.Assert.*

/**
 * @since 0.8
 */
class TenantIT extends ClientIT {

    //TODO - enable this test after assertions are made with the Cache statistics API.
    // This currently just prints output and isn't a valid test for asserting caching.
    @Test(enabled = false)
    void testCaching() {

        //create a bunch of apps:
        def tenant = client.currentTenant

        /*
        120.times { i ->
            Application app = client.instantiate(Application)
            app.name = "Testing Application ${i+1} " + UUID.randomUUID().toString()

            Application created =
                tenant.createApplication(Applications.newCreateRequestFor(app).createDirectory().build())

            resourcesToDelete.add(created);
        }*/

        int count = 0;
        def hrefs = []
        def list = tenant.getApplications()
        for( Application app : list) {
            hrefs << app.href
            count++
        }
        println "Applications seen: $count"

        hrefs.each { href ->
            client.getResource(href, Application)
        }

        count = 0
        hrefs = []
        list = tenant.getDirectories()
        for( Directory directory : list) {
            hrefs << directory.href
            count++
        }
        println "Directories seen: $count"

        hrefs.each { href ->
            client.getResource(href, Directory)
        }

        tenant = client.getResource(tenant.getHref(), Tenant)

        list = tenant.getApplications()
        for(Application app : list) {
            println "Application $app.name"
        }
        list = tenant.getDirectories()
        for(Directory dir : list) {
            println "Directory $dir.name"
        }

        def s = client.dataStore.cacheManager.toString()
        println '"cacheManager": ' + s;

        Thread.sleep(20)
    }

    //@since 1.0.beta
    @Test
    void testCreateDirWithoutProvider() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: TenantIT.testCreateDirWithoutProvider")
        dir = client.currentTenant.createDirectory(dir)
        deleteOnTeardown(dir)

        def provider = dir.getProvider()

        assertEquals(provider.getHref(), dir.getHref() + "/provider")
        assertEquals(provider.getProviderId(), "stormpath")
        assertNotNull(provider.getCreatedAt())
        assertNotNull(provider.getModifiedAt())
    }

    //@since 1.0.beta
    @Test
    void testCreateDirWithGoogleProvider() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: TenantIT.testCreateDirWithGoogleProvider")
        def clientId = uniquify("999999911111111")
        def clientSecret = uniquify("a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0")

        def request = Directories.newCreateRequestFor(dir).
                forProvider(Providers.GOOGLE.builder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setRedirectUri("https://www.myAppURL:8090/index.jsp")
                        .build()
                ).build()

        dir = client.currentTenant.createDirectory(request)
        deleteOnTeardown(dir)

        def provider = dir.getProvider()

        assertEquals(provider.getHref(), dir.getHref() + "/provider")
        assertEquals(provider.getProviderId(), "google")
        assertNotNull(provider.getCreatedAt())
        assertNotNull(provider.getModifiedAt())
        assertTrue(GoogleProvider.isAssignableFrom(provider.getClass()))
        assertEquals(((GoogleProvider)provider).getClientId(), clientId)
        assertEquals(((GoogleProvider)provider).getClientSecret(), clientSecret)
        assertEquals(((GoogleProvider)provider).getRedirectUri(), "https://www.myAppURL:8090/index.jsp")
    }

    //@since 1.0.beta
    @Test
    void testCreateDirWithFacebookProvider() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: TenantIT.testCreateDirWithFacebookProvider")
        def clientId = uniquify("999999911111111")
        def clientSecret = uniquify("a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0")

        def request = Directories.newCreateRequestFor(dir).
                forProvider(Providers.FACEBOOK.builder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .build()
                ).build()

        dir = client.currentTenant.createDirectory(request)
        deleteOnTeardown(dir)

        def provider = dir.getProvider()

        assertEquals(provider.getHref(), dir.getHref() + "/provider")
        assertEquals(provider.getProviderId(), "facebook")
        assertNotNull(provider.getCreatedAt())
        assertNotNull(provider.getModifiedAt())
        assertTrue(FacebookProvider.isAssignableFrom(provider.getClass()))
        assertEquals(((FacebookProvider)provider).getClientId(), clientId)
        assertEquals(((FacebookProvider)provider).getClientSecret(), clientSecret)
    }

    //@since 1.0.RC3
    @Test
    void testCreateDirWithLinkedInProvider() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: TenantIT.testCreateDirWithLinkedInProvider")
        def clientId = uniquify("999999911111111")
        def clientSecret = uniquify("a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0")

        def request = Directories.newCreateRequestFor(dir).
                forProvider(Providers.LINKEDIN.builder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .build()
                ).build()

        dir = client.currentTenant.createDirectory(request)
        deleteOnTeardown(dir)

        def provider = dir.getProvider()

        assertEquals(provider.getHref(), dir.getHref() + "/provider")
        assertEquals(provider.getProviderId(), "linkedin")
        assertNotNull(provider.getCreatedAt())
        assertNotNull(provider.getModifiedAt())
        assertTrue(LinkedInProvider.isAssignableFrom(provider.getClass()))
        assertEquals(((LinkedInProvider)provider).getClientId(), clientId)
        assertEquals(((LinkedInProvider)provider).getClientSecret(), clientSecret)
    }

    //@since 1.0.0
    @Test
    void testGetAccounts() {
        def tenant = client.currentTenant
        def count = 0
        def accounts = tenant.getAccounts()
        for(Account acct : accounts) {
            count++
            //Let's iterate 3 times only, enough to know the collection was received while avoiding unnecessary traffic
            if(count > 2) break
        }
    }

    //@since 1.0.RC3
    @Test
    void testGetAccountsWithCriteria() {
        def uniqueEmail = uniquify("myUnique") + "@email.com"

        def criteria = Accounts.criteria().add(Accounts.email().eqIgnoreCase(uniqueEmail))

        def tenant = client.currentTenant

        def originalQty = 0
        def accounts = tenant.getAccounts(criteria)
        for(Account acct : accounts) {
            originalQty++
        }

        def app = createTempApp()
        Account account = client.instantiate(Account)
        account.email = uniqueEmail
        account.username = uniquify("username")
        account.password = uniquify("paS5w&")
        account.givenName = uniquify("givenName")
        account.surname = uniquify("surname")
        account = app.createAccount(account)
        deleteOnTeardown(account)

        def newQty = 0
        accounts = tenant.getAccounts(criteria)
        for(Account acct : accounts) {
            newQty++
        }

        assertEquals newQty, originalQty + 1
    }

    //@since 1.0.RC3
    @Test
    void testGetAccountsWithMap() {
        def uniqueEmail = uniquify("myUnique") + "@email.com"

        def queryParams = ['email': uniqueEmail]

        def tenant = client.currentTenant

        def originalQty = 0
        def accounts = tenant.getAccounts(queryParams);
        for(Account acct : accounts) {
            originalQty++
        }

        def app = createTempApp()
        Account account = client.instantiate(Account)
        account.email = uniqueEmail
        account.username = uniquify("username")
        account.password = uniquify("paS5w&")
        account.givenName = uniquify("givenName")
        account.surname = uniquify("surname")
        account = app.createAccount(account)
        deleteOnTeardown(account)

        def newQty = 0
        accounts = tenant.getAccounts(queryParams)
        for(Account acct : accounts) {
            newQty++
        }

        assertEquals newQty, originalQty + 1
    }

    //@since 1.0.RC3
    @Test
    void testGetGroups() {
        def tenant = client.currentTenant
        def count = 0
        def groups = tenant.getGroups()
        for(Group group : groups) {
            count++
            //Let's iterate 3 times only, enough to know the collection was received while avoiding unnecessary traffic
            if(count > 2) break
        }
    }

    //@since 1.0.RC3
    @Test
    void testGetGroupsWithCriteria() {
        def uniqueName = uniquify("uniqueGroupName")

        def criteria = Groups.criteria().add(Groups.name().eqIgnoreCase(uniqueName))

        def tenant = client.currentTenant

        def originalQty = 0
        def groups = tenant.getGroups(criteria)
        for(Group grp : groups) {
            originalQty++
        }

        def app = createTempApp()
        Group group = client.instantiate(Group)
        group.name = uniqueName
        group = app.createGroup(group)
        deleteOnTeardown(group)

        def newQty = 0
        groups = tenant.getGroups(criteria)
        for(Group grp : groups) {
            newQty++
        }

        assertEquals newQty, originalQty + 1
    }

    //@since 1.0.RC3
    @Test
    void testGetGroupsWithMap() {
        def uniqueName = uniquify("uniqueGroupName")

        def queryParams = ['name': uniqueName]

        def tenant = client.currentTenant

        def originalQty = 0
        def groups = tenant.getGroups(queryParams);
        for(Group grp : groups) {
            originalQty++
        }

        def app = createTempApp()
        Group group = client.instantiate(Group)
        group.name = uniqueName
        group = app.createGroup(group)
        deleteOnTeardown(group)

        def newQty = 0
        groups = tenant.getGroups(queryParams)
        for(Group grp : groups) {
            newQty++
        }

        assertEquals newQty, originalQty + 1
    }

    //@since 1.0.0
    @Test
    void testCreateDirWithGithubProvider() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: TenantIT.testCreateDirWithGithubProvider")
        def clientId = uniquify("999999911111111")
        def clientSecret = uniquify("a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0")

        def request = Directories.newCreateRequestFor(dir).
                forProvider(Providers.GITHUB.builder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .build()
                ).build()

        dir = client.currentTenant.createDirectory(request)
        deleteOnTeardown(dir)

        def provider = dir.getProvider()

        assertEquals(provider.getHref(), dir.getHref() + "/provider")
        assertEquals(provider.getProviderId(), "github")
        assertNotNull(provider.getCreatedAt())
        assertNotNull(provider.getModifiedAt())
        assertTrue(GithubProvider.isAssignableFrom(provider.getClass()))
        assertEquals(((GithubProvider)provider).getClientId(), clientId)
        assertEquals(((GithubProvider)provider).getClientSecret(), clientSecret)
    }

    /**
     * @since 1.0.RC4
     */
    @Test
    void testGetDirectoriesWithTimestampFilter() {
        def tenant = client.currentTenant
        Directory directory = client.instantiate(Directory)
        directory.name = uniquify("Java SDK: TenantIT.testGetDirectoriesWithTimestampFilters")
        directory = client.createDirectory(directory);
        deleteOnTeardown(directory)
        assertNotNull directory.href

        DateFormat df = new ISO8601DateFormat();
        String creationTimestamp = df.format(directory.createdAt)

        def dirList = tenant.getDirectories(Directories.where(Directories.createdAt().matches(creationTimestamp)))

        assertNotNull dirList
        def retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals df.format(retrieved.createdAt), creationTimestamp
    }

    /**
     * @since 1.0.RC4
     */
    @Test
    void testGetDirectoriesWithWrongTimestampFilter() {
        def tenant = client.currentTenant
        Directory directory = client.instantiate(Directory)
        directory.name = uniquify("Java SDK: TenantIT.testGetDirectoriesWithWrongTimestampFilters")
        directory = client.createDirectory(directory);
        deleteOnTeardown(directory)
        assertNotNull directory.href

        try {
            def dirList = tenant.getDirectories(
                    Directories.where(Directories.modifiedAt().matches("wrong match expression")))
            fail("should have thrown")
        } catch (Exception e){
            assertEquals(e.getMessage(), "HTTP 400, Stormpath 2103 (http://docs.stormpath.com/errors/2103): modifiedAt query criteria parameter value is invalid or an unexpected type.")
        }
    }

    @Test
    void testGetApplicationsWithTimestampFilter(){
        def tenant = client.currentTenant
        DateFormat df = new ISO8601DateFormat();

        Application application = client.instantiate(Application)
        application.setName(uniquify("testGetApplicationsWithTimestampFilter app"))
        application = tenant.createApplication(Applications.newCreateRequestFor(application).createDirectory().build())

        Date appCreationTimestamp = application.createdAt

        //matches
        def creationTimestamp = df.format(application.getCreatedAt());
        def appList = client.getApplications(Applications.where(Applications.createdAt().matches(creationTimestamp)))
        assertNotNull appList.href

        def retrieved = appList.iterator().next()
        assertEquals retrieved.href, application.href
        assertEquals df.format(retrieved.createdAt), creationTimestamp

        //equals
        appList = client.getApplications(Applications.where(Applications.createdAt().equals(appCreationTimestamp)))
        assertNotNull appList.href

        retrieved = appList.iterator().next()
        assertEquals retrieved.href, application.href
        assertEquals df.format(retrieved.createdAt), creationTimestamp

        //gt
        appList = client.getApplications(Applications.where(Applications.name().eqIgnoreCase(application.name))
                .and(Applications.createdAt().gt(appCreationTimestamp)))
        assertNotNull appList.href
        assertFalse appList.iterator().hasNext()

        //gte
        appList = client.getApplications(Applications.where(Applications.name().eqIgnoreCase(application.name))
                .and(Applications.createdAt().gte(appCreationTimestamp)))
        assertNotNull appList.href
        assertTrue appList.iterator().hasNext()
        retrieved = appList.iterator().next()
        assertEquals retrieved.href, application.href
        assertEquals retrieved.name, application.name
        assertEquals df.format(retrieved.createdAt), creationTimestamp

        //lt
        appList = client.getApplications(Applications.where(Applications.name().eqIgnoreCase(application.name))
                .and(Applications.createdAt().lt(appCreationTimestamp)))
        assertNotNull appList.href
        assertFalse appList.iterator().hasNext()

        //lte
        appList = client.getApplications(Applications.where(Applications.name().eqIgnoreCase(application.name))
                .and(Applications.createdAt().lte(appCreationTimestamp)))
        assertNotNull appList.href
        assertTrue appList.iterator().hasNext()
        retrieved = appList.iterator().next()
        assertEquals retrieved.href, application.href
        assertEquals retrieved.name, application.name
        assertEquals df.format(retrieved.createdAt), creationTimestamp

        //in
        Calendar cal = Calendar.getInstance()
        cal.setTime(appCreationTimestamp)
        cal.add(Calendar.SECOND, 2)
        Date afterCreationDate = cal.getTime()

        appList = client.getApplications(Applications.where(Applications.name().eqIgnoreCase(application.name))
                .and(Applications.createdAt().in(appCreationTimestamp, afterCreationDate)))
        assertNotNull appList.href
        assertTrue appList.iterator().hasNext()
        retrieved = appList.iterator().next()
        assertEquals retrieved.href, application.href
        assertEquals retrieved.name, application.name
        assertEquals df.format(retrieved.createdAt), creationTimestamp

        //in
        cal.setTime(appCreationTimestamp)
        cal.add(Calendar.SECOND, -10)
        Date newDate = cal.getTime()
        appList = client.getApplications(Applications.where(Applications.name().eqIgnoreCase(application.name))
                .and(Applications.createdAt().in(newDate, new Duration(1, TimeUnit.SECONDS))))
        assertNotNull appList.href
        assertFalse appList.iterator().hasNext()

        //in
        appList = client.getApplications(Applications.where(Applications.name().eqIgnoreCase(application.name))
                .and(Applications.createdAt().in(appCreationTimestamp, new Duration(1, TimeUnit.MINUTES))))
        assertNotNull appList.href
        assertTrue appList.iterator().hasNext()
        retrieved = appList.iterator().next()
        assertEquals retrieved.href, application.href
        assertEquals retrieved.name, application.name
        assertEquals df.format(retrieved.createdAt), creationTimestamp
    }
}
