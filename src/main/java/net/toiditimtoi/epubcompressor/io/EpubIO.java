package net.toiditimtoi.epubcompressor.io;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class EpubIO {

    public static final String PATH_TO_CAESIUM = "/Users/phucngu/workspace/caesium-clt/target/release/caesiumclt";
    private static final Path epubImagesFolder = Path.of("OEBPS", "images");

    public static Path extractEpub(String epubFilePath) throws IOException {
        // Create a temporary folder to extract the contents
        Path tempFolder = Files.createTempDirectory("epub-extract");

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Path.of(epubFilePath)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    // Extract each file entry to the temporary folder
                    Path entryPath = tempFolder.resolve(entry.getName());
                    Files.createDirectories(entryPath.getParent()); // Create parent directories if they don't exist
                    Files.copy(zipInputStream, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        return tempFolder;
    }

    /**
     * Deletes a file or directory along with all nested content.
     *
     * @param path the path to the file or directory to delete
     * @throws IOException if an I/O error occurs
     */
    public static void deleteRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                // Use try-with-resources to ensure the stream is closed properly
                try (Stream<Path> walk = Files.walk(path)) {
                    walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            System.err.println("Failed to delete " + p + ": " + e.getMessage());
                        }
                    });
                }
            } else {
                // If it's a file, delete it directly
                Files.delete(path);
            }
        } else {
            System.out.println("The path '" + path + "' does not exist.");
        }
    }

    public static Path prepareTargetFolder(Path epubExtractedContent) throws IOException {
        return Files.createTempDirectory(epubExtractedContent.getParent(), "image-epub");
    }

    public static Path compressImages(Path sourceFolder, Path outputFolder, int compressionRatio) {
        // prepare the command to start a caesium process
        var processBuilder = new ProcessBuilder(PATH_TO_CAESIUM, "-q", String.valueOf(compressionRatio), "-RS", "-o", outputFolder.toString(), sourceFolder.toString());

        // start the process
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ioException) {
            throw new RuntimeException("Cannot compress");
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            // read the output from the command
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("The epub file cannot be compressed, please contact the admin for more detail");
            }
        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
        }
        return outputFolder;
    }

//    public Path addCompressedImagesAndOriginalNonImageToEpubFile()

    public static Path compressEpubFile(Path sourceEpubFile, int compressionRatio) throws IOException {
        // extract the epub file to a temporary folder
        var extractedEpubFolder = extractEpub(sourceEpubFile.toString());

        // prepare a folder where Caesium will output the compressed images
        var imageOutputFolder = prepareTargetFolder(extractedEpubFolder);

        // call Caesium to do the image compression
        var sourceImageFolder = extractedEpubFolder.resolve(epubImagesFolder);
        var outputImg = compressImages(sourceImageFolder, imageOutputFolder, compressionRatio);
        // create the zip file
        var originalFileName = sourceEpubFile.getFileName().toString();
        var nameComponents = originalFileName.split("\\.");
        Path targetEpub = sourceEpubFile.resolveSibling(nameComponents[0] + "_compressed.epub");

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(targetEpub.toFile()))) {
            // copy the original non-image over
            try (var originalNonImageStream = Files.walk(extractedEpubFolder)) {
                originalNonImageStream.filter(path -> !extractedEpubFolder.relativize(path).startsWith(epubImagesFolder)).forEach(originalEntry -> {
                    // Get the relative path of the file or directory
                    Path relativePath = extractedEpubFolder.relativize(originalEntry);

                    try {
                        // Create a new entry for each file or directory
                        ZipEntry zipEntry = new ZipEntry(relativePath.toString());
                        if (Files.isDirectory(originalEntry)) {
                            zipEntry = new ZipEntry(zipEntry.getName() + "/");
                        }
                        zipOutputStream.putNextEntry(zipEntry);

                        // If it's a file, copy its content to the zip output stream
                        if (Files.isRegularFile(originalEntry)) {
                            Files.copy(originalEntry, zipOutputStream);
                        }

                        zipOutputStream.closeEntry();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                });
            }

            // copy the compressed image over
            try (var insideTheCompressedImgFolderStream = Files.walk(outputImg)) {
                insideTheCompressedImgFolderStream.forEach(imgPath -> {
                    Path relativePath = imageOutputFolder.relativize(imgPath);
                    Path relativeToTargetEpubPath = epubImagesFolder.resolve(relativePath);
                    try {
                        // create a new entry for each file or directory
                        ZipEntry zipEntry = new ZipEntry(relativeToTargetEpubPath.toString());
                        if (Files.isDirectory(imgPath)) {
                            zipEntry = new ZipEntry(zipEntry.getName() + "/");
                        }
                        zipOutputStream.putNextEntry(zipEntry);

                        if (Files.isRegularFile(imgPath)) {
                            Files.copy(imgPath, zipOutputStream);
                        }

                        zipOutputStream.closeEntry(); // Ensure this is called instead of zipOutputStream.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                });
            }

        }

        // clean up all the temp folder
        deleteRecursively(extractedEpubFolder);
        deleteRecursively(imageOutputFolder);
        return targetEpub ;
    }
}
