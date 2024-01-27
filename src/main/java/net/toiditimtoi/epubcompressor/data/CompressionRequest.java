package net.toiditimtoi.epubcompressor.data;

import org.springframework.web.multipart.MultipartFile;

public record CompressionRequest(MultipartFile file, int compressionRatio) { }
