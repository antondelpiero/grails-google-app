package com.red2blue.plugin.google.drive

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import grails.core.GrailsApplication

/**
 * Created by antonprawiro on 11/12/2016.
 */
class GoogleDrive {
    /** Application name. */
    private static final String APPLICATION_NAME =  "Drive API Java Quickstart"

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.defaultInstance

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT

    Drive drive

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize(String clientId, String clientSecret, String refreshToken) throws IOException {
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientId, clientSecret)
                .build()
        credential.refreshToken = refreshToken
        credential
    }

    public GoogleDrive(String clientId, String clientSecret, String refreshToken) throws IOException {
        Credential credential = authorize(clientId, clientSecret, refreshToken)
        drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build()
    }

}
