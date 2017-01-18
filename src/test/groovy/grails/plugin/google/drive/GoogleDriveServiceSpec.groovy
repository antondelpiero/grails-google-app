package grails.plugin.google.drive

import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import grails.test.mixin.TestFor
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(GoogleDriveService)
class GoogleDriveServiceSpec extends Specification {

    def mockDriveFiles, mockCreate

    def setup() {
        def googleDriveInitService = Mock(GoogleDriveInitService)
        googleDriveInitService.init() >> Mock(Drive)

        mockDriveFiles = Mock(Drive.Files)
        mockCreate = GroovyMock(Drive.Files.Create)

        service.googleDriveInitService = googleDriveInitService
        service.init()
    }

    void "init should delegate call to GoogleDriveInitService"() {
        when:
        service.init()

        then:
        1 * service.googleDriveInitService.init()
    }

    void "get should return google drive file type Get"() {
        given:
        def mockGet = Mock(Drive.Files.Get)
        mockDriveFiles.get("1") >> mockGet

        when:
        def result = service.get("1")

        then:
        1 * service.drive.files() >> mockDriveFiles
        result == mockGet
    }

    @Unroll
    // this also test createFolder()
    void "getOrCreateFolder should always return result -> fileAvailable: #fileAvailable"() {
        given: "mocked collaborator when file is found"
        def mockFile = GroovyMock(File)
        mockFile.name >> name

        def mockFileList = GroovyMock(FileList)
        mockFileList.getFiles() >> [mockFile]
        def mockList = Mock(Drive.Files.List)
        mockList.setQ(service.FOLDERS_QUERY) >> mockList
        mockList.execute() >> mockFileList

        mockDriveFiles.list() >> mockList

        and: "mocked collaborator when file is not found"
        mockDriveFiles.create(_ as File) >> mockCreate

        when:
        def result = service.getOrCreateFolder("test")

        then:
        (fileAvailable ? 1 : 2) * service.drive.files() >> mockDriveFiles
        (fileAvailable ? 0 : 1) * mockCreate.execute() >> mockFile
        result.name == name

        where:
        fileAvailable | name
        true          | "test"
        false         | "other"
    }

    @Unroll
    void "create with MultipartFile should upload file to google drive root folder when folder name -> #folderName"() {
        given:
        def file = Mock(MultipartFile)
        file.originalFilename >> "test.pdf"
        file.contentType >> "application/pdf"
        file.inputStream >> new ByteArrayInputStream("test".bytes)

        def mockHttpUploader = GroovyMock(MediaHttpUploader)
        service.drive.files() >> mockDriveFiles

        when:
        service.create(file)

        then:
        1 * mockDriveFiles.create(_, _) >> { fileMeta, inputStream ->
            assert fileMeta.name == "test.pdf"
            assert fileMeta.parents == ["root"]
            mockCreate
        }
        1 * mockCreate.getMediaHttpUploader() >> mockHttpUploader
        1 * mockHttpUploader.setProgressListener(_)
        1 * mockCreate.execute()

        where:
        folderName << [null, 'root']
    }

    @Unroll
    void "create with InputStream should upload file to google drive root folder when folder name -> #folderName"() {
        given:
        def mockHttpUploader = GroovyMock(MediaHttpUploader)
        service.drive.files() >> mockDriveFiles

        when:
        service.create("test.pdf", "application/pdf", Mock(BufferedInputStream))

        then:
        1 * mockDriveFiles.create(_, _) >> { fileMeta, inputStream ->
            assert fileMeta.name == "test.pdf"
            assert fileMeta.parents == ["root"]
            mockCreate
        }
        1 * mockCreate.getMediaHttpUploader() >> mockHttpUploader
        1 * mockHttpUploader.setProgressListener(_)
        1 * mockCreate.execute()

        where:
        folderName << [null, 'root']
    }

    void "delete should delegate call to google drive"() {
        given:
        service.drive.files() >> mockDriveFiles

        when:
        service.delete("1")

        then:
        1 * mockDriveFiles.delete("1") >> Mock(Drive.Files.Delete)
    }
}
