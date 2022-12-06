// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

// <ImportSnippet>
package graphtutorial;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.DeviceCodeInfo;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.DriveRecentCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;

import okhttp3.Request;
// </ImportSnippet>

public class Graph {
    // <UserAuthConfigSnippet>
    private static Properties _properties;
    private static DeviceCodeCredential _deviceCodeCredential;
    private static GraphServiceClient<Request> _userClient;

    public static void initializeGraphForUserAuth(Properties properties, Consumer<DeviceCodeInfo> challenge) throws Exception {
        // Ensure properties isn't null
        if (properties == null) {
            throw new Exception("Properties cannot be null");
        }

        _properties = properties;

        final String clientId = properties.getProperty("app.clientId");
        final String authTenantId = properties.getProperty("app.authTenant");
        final List<String> graphUserScopes = Arrays
                .asList(properties.getProperty("app.graphUserScopes").split(","));

        _deviceCodeCredential = new DeviceCodeCredentialBuilder()
                .clientId(clientId)
                .tenantId(authTenantId)
                .challengeConsumer(challenge)
                .build();

        final TokenCredentialAuthProvider authProvider =
                new TokenCredentialAuthProvider(graphUserScopes, _deviceCodeCredential);

        _userClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

    public static String getUserToken() throws Exception {
        // Ensure credential isn't null
        if (_deviceCodeCredential == null) {
            throw new Exception("Graph has not been initialized for user auth");
        }

        final String[] graphUserScopes = _properties.getProperty("app.graphUserScopes").split(",");

        final TokenRequestContext context = new TokenRequestContext();
        context.addScopes(graphUserScopes);

        final AccessToken token = _deviceCodeCredential.getToken(context).block();
        return token.getToken();
    }

    public static User getUser() throws Exception {
        if (_userClient == null) {
            throw new Exception("Graph has not been initialized for user auth");
        }

        return _userClient.me()
                .buildRequest()
                .select("displayName,mail,userPrincipalName")
                .get();
    }


    public static void listFiles() {
        // GraphServiceClient graphClient = GraphServiceClient.builder().authenticationProvider( authProvider ).buildClient();
        System.out.println("Invoked Graph.listFiles()");
        DriveItemCollectionPage children = _userClient.me().drive().root().children()
                .buildRequest()
                .get();
        children.getCurrentPage().stream().forEach(di -> System.out.println(di.webUrl));
        System.out.println("Completed: Invoked Graph.listFiles()");
    }

    private static ClientSecretCredential _clientSecretCredential;
    private static GraphServiceClient<Request> _appClient;

    private static void ensureGraphForAppOnlyAuth() throws Exception {
        if (_properties == null) {
            throw new Exception("Properties cannot be null");
        }

        if (_clientSecretCredential == null) {
            final String clientId = _properties.getProperty("app.clientId");
            final String tenantId = _properties.getProperty("app.tenantId");
            final String clientSecret = _properties.getProperty("app.clientSecret");

            _clientSecretCredential = new ClientSecretCredentialBuilder()
                    .clientId(clientId)
                    .tenantId(tenantId)
                    .clientSecret(clientSecret)
                    .build();
        }

        if (_appClient == null) {
            final TokenCredentialAuthProvider authProvider =
                    new TokenCredentialAuthProvider(
                            List.of("https://graph.microsoft.com/.default"), _clientSecretCredential);

            _appClient = GraphServiceClient.builder()
                    .authenticationProvider(authProvider)
                    .buildClient();
        }
    }

    public static void makeGraphCallToGetAllFiles() {
        DriveItemCollectionPage children = _appClient.me().drive().root().children()
                .buildRequest()
                .get();

        // INSERT YOUR CODE HERE
        // Note: if using _appClient, be sure to call ensureGraphForAppOnlyAuth
        // before using it.
        // ensureGraphForAppOnlyAuth();
    }

    public static void makeGraphCallToGetRecentFiles(){
        DriveRecentCollectionPage recent = _appClient.me().drive()
                .recent()
                .buildRequest()
                .get();
    }

    public static void createFolder(){
        DriveItem driveItem = new DriveItem();
        driveItem.name = "New Folder";
        Folder folder = new Folder();
        driveItem.folder = folder;

        _appClient.me().drive().root().children()
                .buildRequest()
                .post(driveItem);

    }
}
