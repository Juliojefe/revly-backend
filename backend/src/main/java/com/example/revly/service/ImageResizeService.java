package com.example.revly.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

@Service
public class ImageResizeService {

    private static final long MIN_IMAGE_SIZE_BYTES = 150 * 1024;     // never go below ~150 KB
    private static final int MAX_IMAGE_WIDTH = 1920;
    private static final int MAX_IMAGE_HEIGHT = 1080;
    private static final float COMPRESSION_QUALITY = 0.85f;         // good balance for social media

    //  resizes images if needed
    public byte[] optimizeImage(MultipartFile file) throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            throw new IOException("Invalid image file");
        }
        BufferedImage resized = resizeImage(original, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(COMPRESSION_QUALITY);
        }
        writer.setOutput(ImageIO.createImageOutputStream(baos));
        writer.write(null, new IIOImage(resized, null, null), param);
        writer.dispose();
        byte[] result = baos.toByteArray();
        // Safety net: ensure image isn't too small
        if (result.length < MIN_IMAGE_SIZE_BYTES) {
            baos.reset();
            param.setCompressionQuality(0.92f);
            writer = ImageIO.getImageWritersByFormatName("jpg").next();
            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(resized, null, null), param);
            writer.dispose();
            result = baos.toByteArray();
        }
        return result;
    }

    private BufferedImage resizeImage(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        double ratio = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        if (ratio >= 1.0) {
            return original; // no resize needed
        }
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return resized;
    }
}
