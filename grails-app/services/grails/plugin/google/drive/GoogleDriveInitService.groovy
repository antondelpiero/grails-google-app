package grails.plugin.google.drive

import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import groovy.util.logging.Slf4j

@Slf4j
class GoogleDriveInitService {

    private static final String APPLICATION_NAME = "Google Drive Plugin"

    def grailsApplication

    Drive init() {
        def credential = grailsApplication.config.grails.plugin?.google?.drive?.credential?.domainClassName ? credentialFromDomain : credentialFromConfig

        switch (credential?.type) {
            case AuthorizationType.USER_LOGIN:
                log.info('Use USER_LOGIN as authorization type')
                initWithUserLogin(credential)
                break
            case AuthorizationType.SERVICE_ACCOUNT:
                log.info('Used SERVICE_ACCOUNT as authorization type')
                initWithServiceAccount(credential)
                break
            case AuthorizationType.REFRESH_TOKEN:
                log.info('Used REFRESH_TOKEN as authorization type')
                initWithRefreshToken(credential)
                break
            default:
                log.info('Authorization type is not set, no google drive api was created')
                null
                break
        }
    }

    private Map getCredentialFromConfig() {
        def credential = grailsApplication.config.grails.plugin.google.drive.credential

        if (!credential)
            return [:]

        credential.type = credential.type as AuthorizationType
        credential.applicationName = credential.applicationName ?: APPLICATION_NAME
        credential.credentialStream = credential.jsonCredential ? this.class.getClassLoader().getResourceAsStream(credential.jsonCredential) : null
        credential
    }

    private Map getCredentialFromDomain() {
        def className = grailsApplication.config.grails.plugin.google.drive.credential.domainClassName

        try {
            def credential = grailsApplication.getDomainClass(className)?.clazz?.get(1)

            if (!credential)
                return [:]

            [
                    type            : credential.type,
                    applicationName : credential.applicationName ?: APPLICATION_NAME,
                    clientId        : credential.clientId,
                    clientSecret    : credential.clientSecret,
                    refreshToken    : credential.refreshToken,
                    credentialStream: credential.jsonCredential ? new BufferedInputStream(new ByteArrayInputStream(credential.jsonCredential)) : null,
                    scopes          : credential.scopes
            ]
        } catch (RuntimeException e) {
            log.error('Exception was thrown when calling the database. Please check your database or tables for errors!', e.cause)
            null
        }
    }

    //TODO: implement me!
    private static Drive initWithUserLogin(config) {
        log.error('This method is not yet implemented, please use SERVICE_ACCOUNT or REFRESH_TOKEN method')
        null
    }

    private static Drive initWithServiceAccount(config) {
        try {
            config.scopes = !config.scopes ?: config.scopes.collect { DriveScopes."${it}" }
        } catch (MissingPropertyException e) {
            log.error('No valid DriveScopes for google drive can be found, please check your configuration again!', e.cause)
            return null
        }

        if (!config.credentialStream || !config.scopes) {
            log.error('No valid SERVICE_ACCOUNT credential for google drive can be found, please check your configuration again!')
            return null
        }

        new GoogleDrive(config.applicationName, config.credentialStream, config.scopes).drive
    }

    private static Drive initWithRefreshToken(config) {
        if (!config.clientId || !config.clientSecret || !config.refreshToken) {
            log.error('No valid REFRESH_TOKEN credential for google drive can be found, please check your configuration again!')
            return null
        }

        new GoogleDrive(config.applicationName, config.clientId.toString(), config.clientSecret.toString(), config.refreshToken.toString()).drive
    }

}
