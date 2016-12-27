package grails.plugin.google.drive

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive

class GoogleDrive {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.defaultInstance
    private static HttpTransport HTTP_TRANSPORT

    Drive drive

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        } catch (Throwable t) {
            t.printStackTrace()
            System.exit(1)
        }
    }

    private static Credential authorize(String clientId, String clientSecret, String refreshToken) {
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientId, clientSecret)
                .build()
        credential.refreshToken = refreshToken
        credential
    }

    GoogleDrive(String clientId, String clientSecret, String refreshToken) {
        Credential credential = authorize(clientId, clientSecret, refreshToken)
        drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build()
    }

}
