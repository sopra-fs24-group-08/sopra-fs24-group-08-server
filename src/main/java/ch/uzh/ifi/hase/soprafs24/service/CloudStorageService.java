package ch.uzh.ifi.hase.soprafs24.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Acl; // Import Acl
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CloudStorageService {

    private static final String BUCKET_NAME = "sopra-fs24-group-08-server-bucket";

    private final Storage storage;

    public CloudStorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public String uploadObject(String objectName, byte[] content) throws IOException {
        BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        Blob blob = storage.create(blobInfo, content);

        // Make the blob publicly accessible
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        return String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, objectName);
    }

    public byte[] downloadObject(String objectName) {
        Blob blob = storage.get(BlobId.of(BUCKET_NAME, objectName));
        return blob.getContent();
    }
}
