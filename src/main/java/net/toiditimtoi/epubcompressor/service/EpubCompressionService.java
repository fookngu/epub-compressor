package net.toiditimtoi.epubcompressor.service;

import net.toiditimtoi.epubcompressor.data.CompressionRequest;
import net.toiditimtoi.epubcompressor.io.EpubIO;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

@Service
public class EpubCompressionService {

    private final String TMP_FOLDER_PREFIX = "epub-extract";

    public FileSystemResource compress(CompressionRequest compressionRequest) {
        var originalFileName = compressionRequest.file().getOriginalFilename();
        try {
            // a temporary folder to put the epub file into, extract the epub, put the compressed image file and produce the compress epub file
            var tmpMasterFolder = Files.createTempDirectory(TMP_FOLDER_PREFIX);
            // save the file
            assert originalFileName != null;
            var fsEpubFile = tmpMasterFolder.resolve(originalFileName);
            compressionRequest.file().transferTo(fsEpubFile);
            var compressedFile = EpubIO.compressEpubFile(fsEpubFile, compressionRequest.compressionRatio());
            return new FileSystemResource(compressedFile);

        } catch (IOException e) {
            throw new RuntimeException("Cannot work with file system");
        }
    }
}
