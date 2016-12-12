package com.red2blue.plugin.google.drive

import com.google.api.services.drive.Drive

import javax.annotation.PostConstruct

class GoogleDriveService {

    def grailsApplication

    Drive drive

    @PostConstruct
    def init() {
        def config = grailsApplication.config.google.drive.credential

        assert config.clientId
        assert config.clientSecret
        assert config.refreshToken

        drive = new GoogleDrive(config.clientId.toString(), config.clientSecret.toString(), config.refreshToken.toString()).drive
    }

    Drive.Files.Get get(String id) {
        drive.files().get(id)
    }
}
