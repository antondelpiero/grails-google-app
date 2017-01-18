package grails.plugin.google

import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import groovy.util.logging.Slf4j

import static com.google.api.client.googleapis.media.MediaHttpUploader.UploadState.*

@Slf4j
class CustomProgressListener implements MediaHttpUploaderProgressListener {

    @Override
    void progressChanged(MediaHttpUploader uploader) {
        switch (uploader.getUploadState()) {
            case INITIATION_STARTED:
                log.info "Initiation has been started!"
                break
            case INITIATION_COMPLETE:
                log.info "Initiation is complete!"
                break
            case MEDIA_IN_PROGRESS:
                log.info "Upload progress: ${uploader.numBytesUploaded} bytes uploaded"
                break
            case MEDIA_COMPLETE:
                log.info "Upload is complete!"
                break
            case NOT_STARTED:
                log.info "Upload Not Started!"
                break
        }
    }
}
