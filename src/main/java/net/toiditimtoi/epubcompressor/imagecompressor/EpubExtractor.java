package net.toiditimtoi.epubcompressor.imagecompressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EpubExtractor {

    public static Path extractEpub(String epubFilePath) throws IOException {
        // Create a temporary directory
        Path tempDir = Files.createTempDirectory("extractedEpub");

        // Open the EPUB file as a ZIP file
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(epubFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            byte[] buffer = new byte[1024];

            // Extract each entry in the ZIP file
            while (zipEntry != null) {
                File newFile = newFile(tempDir.toFile(), zipEntry);
                // Create all parent directories
                new File(newFile.getParent()).mkdirs();

                if (!zipEntry.isDirectory()) {
                    // Write file content
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }

        return tempDir;
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }


}
