package net.toiditimtoi.epubcompressor.controller;

import net.toiditimtoi.epubcompressor.data.CompressionRequest;
import net.toiditimtoi.epubcompressor.service.EpubCompressionService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class IndexController {

    private final EpubCompressionService compressionService;

    public IndexController(EpubCompressionService compressionService) {
        this.compressionService = compressionService;
    }

    @GetMapping("")
    public String index() {
        return "index";
    }

    @PostMapping("/epub/compress")
    public ResponseEntity<FileSystemResource> compressFile(CompressionRequest compressionRequest) {

        var fsResource = compressionService.compress(compressionRequest);  // Redirect to home page after processing
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compressed_" + compressionRequest.file().getOriginalFilename());
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(fsResource);
    }
}
