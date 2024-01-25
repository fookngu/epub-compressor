package net.toiditimtoi.epubcompressor;

import net.toiditimtoi.epubcompressor.imagecompressor.EpubExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
class EpubCompressorApplicationTests {

	@Test
	void contextLoads() {
	}

	public static void main(String[] args) {
		try {
			Path tempDir = EpubExtractor.extractEpub("gitignorethis/DK - A Short History of the Vietnam War-DK Publishing (2021).epub");
			System.out.println("Extracted to: " + tempDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
