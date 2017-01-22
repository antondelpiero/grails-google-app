# **Google Apps Grails Plugin** [![Build Status](https://travis-ci.org/antondelpiero/grails-google-app.svg?branch=master)](https://travis-ci.org/antondelpiero/grails-google-app)

Convenience plugin to connect to google products. At the moment only google drive is supported. Other google products will be added in future updates. Be sure to check the **important notice**

## **Installation**

Add to your build config the following dependency:

    compile "org.grails.plugins:grails-google-app:0.1"

## **Setup**

Following config pattern is needed to be added to application.yml:

    grails:
	    plugin:
		    google:
			    drive:
				    credential: 
					    (details see below)
There are two ways on how you can put your google app credentials, in application.yml or to use a domain, where the latter is easier and more flexible to use, as you can dynamically change the credential in runtime.

#### **Domain**
Add `domainClassName: 'com.yourcompany.google.drive.Credential'` under `credential` in application.yml and create a domain class in your application, e.g:

    package com.yourcompany.google.drive

    class Credential {
        AuthorizationType type
        String applicationName
        String clientId
        String clientSecret
        String refreshToken
        byte[] jsonCredential
        List<String> scopes
        
        static constraints = {
	        // type REFRESH_TOKEN needs clientId, clientSecret and refreshToken
	        // type SERVICE_ACCOUNT needs jsonCredential and scopes
        }
    }

#### **Without Domain**
Depends on AuthorizationType, you'll need to provide the property configs.
If `'SERVICE_ACCOUNT'` is used, then:
	
	type: 'SERVICE_ACCOUNT'
	jsonCredential: 'my_project.json'
	scopes: 'DRIVE'
##### Legend:
###### **jsonCredential**: a file resides in src/main/resources
######**scopes**: Scopes defined by Google, you can add multiple scopes

## **How to use**

Just declare `def googleDriveService` in your controller or service, it'll be autowired.

If the credential was updated, just call googleDriveService.init() or use grails event with key `'plugin.google.drive.restart'` to reinitialize the service.

## **Important Notice**

 - At the moment only 'SERVICE_ACCOUNT' or 'REFRESH_TOKEN' credential
   type can be used!!
 - If you have improvements or bug fixing, please don't hestitate to contact me or make pull request
 