package grails.plugin.google.drive

import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import grails.plugin.google.CustomProgressListener
import grails.transaction.Transactional
import org.springframework.web.multipart.MultipartFile
import reactor.spring.context.annotation.Consumer
import reactor.spring.context.annotation.Selector

import javax.annotation.PostConstruct

@Consumer
class GoogleDriveService {

    static final String FOLDER_TYPE = "application/vnd.google-apps.folder"
    static final String FOLDERS_QUERY = "mimeType='${FOLDER_TYPE}' and trashed=false"

    def googleDriveInitService

    Drive drive

    @PostConstruct
    @Selector('plugin.google.drive.restart')
    @Transactional
    void init() {
        // to make this service unit testable
        drive = googleDriveInitService?.init()
    }

    Drive.Files.Get get(String id) {
        drive.files().get(id)
    }

    @Transactional
    File getOrCreateFolder(String folderName) {
        File result = drive.files().list().setQ(FOLDERS_QUERY).execute().getFiles().find { it.name == folderName }
        result != null ? result : createFolder(folderName)
    }

    @Transactional
    File createFolder(String folderName) {
        File fileMeta = new File(name: folderName, mimeType: FOLDER_TYPE)
        drive.files().create(fileMeta).execute()
    }

    @Transactional
    File create(MultipartFile file, String folderName = null) {
        File fileMeta = getFileMeta(file.originalFilename, folderName)
        InputStreamContent inputStream = new InputStreamContent(file.contentType, file.inputStream)

        sendCreateRequestAndExecute(fileMeta, inputStream)
    }

    @Transactional
    File create(String fileName, String mimeType, BufferedInputStream bufferedInputStream, String folderName = null) {
        File fileMeta = getFileMeta(fileName, folderName)
        InputStreamContent inputStream = new InputStreamContent(mimeType, bufferedInputStream)

        sendCreateRequestAndExecute(fileMeta, inputStream)
    }

    private File getFileMeta(String fileName, String folderName) {
        String folderId = !folderName || folderName == 'root' ? 'root' : getOrCreateFolder(folderName).id
        new File(name: fileName, parents: Collections.singletonList(folderId))
    }

    private File sendCreateRequestAndExecute(File fileMeta, InputStreamContent inputStream) {
        def request = drive.files().create(fileMeta, inputStream)
        request.getMediaHttpUploader().setProgressListener(new CustomProgressListener())
        request.execute()
    }

    void delete(String id) {
        drive.files().delete(id).execute()
    }
}