package net.toiditimtoi.epubcompressor.imagecompressor;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class ImageCompressor {

    public static void compressImage(String inputImagePath, String outputImagePath, float compressionQuality) throws IOException {
        // Read the image
        File inputFile = new File(inputImagePath);
        BufferedImage image = ImageIO.read(inputFile);

        // Get a ImageWriter for jpeg format.
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) throw new IllegalStateException("No writers found");

        ImageWriter writer = writers.next();

        // Create the ImageWriteParam to compress the image.
        ImageWriteParam param = writer.getDefaultWriteParam();

        // Set the compression quality
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(compressionQuality);

        // The output will be a ByteArrayOutputStream (in memory)
        try (OutputStream out = new FileOutputStream(outputImagePath);
             ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {

            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);

            ios.flush(); // important!
        } finally {
            writer.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            compressImage("gitignorethis/Page-429_2.png", "gitignorethis/page-429_2_compressed.png", 0.5f); // 0.5f is 50% quality
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
