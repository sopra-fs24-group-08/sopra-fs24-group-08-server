package ch.uzh.ifi.hase.soprafs24.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CloudStorageService {

    private static final String BUCKET_NAME = "sopra-fs24-group-08-server-bucket";

    private final Storage storage;

    public CloudStorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public void uploadObject(String objectName, byte[] content) throws IOException {
        BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, content);
    }

    public byte[] downloadObject(String objectName) {
        Blob blob = storage.get(BlobId.of(BUCKET_NAME, objectName));
        return blob.getContent();
    }
}
