package net.toiditimtoi.epubcompressor.io;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EpubIOTest {

    public static String SOURCE_EPUB_FILE = "/Users/phucngu/draft/vnwar.epub";
    private Path extractedFolder = null;

    @Test
    @Order(1)
    void extractEpub() throws IOException {
        var outputPath = EpubIO.extractEpub(SOURCE_EPUB_FILE);
        extractedFolder = outputPath;
        System.out.println(outputPath);
        assertTrue(Files.exists(outputPath));
    }

    @Test
    @Order(2)
    public void deleteFolderRecursively() throws IOException {
        if (this.extractedFolder != null){
            EpubIO.deleteRecursively(this.extractedFolder);
        }
        assert extractedFolder != null;
        assertTrue(Files.notExists(extractedFolder));
    }

    @Test
    @Order(3)
    public void extractAndCompressImage() throws IOException {
        var output = EpubIO.compressEpubFile(Path.of(SOURCE_EPUB_FILE), 30);
        System.out.println(output);
    }
}