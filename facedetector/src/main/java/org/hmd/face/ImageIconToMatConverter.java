package org.hmd.face;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageIconToMatConverter {

    public static Mat imageIconToMat(ImageIcon imageIcon) {
        Image image = imageIcon.getImage();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);

        // Create a graphics context
        Graphics g = bufferedImage.createGraphics();

        // Draw the image on the buffered image
        g.drawImage(image, 0, 0, null);

        // Dispose of the graphics context to free up resources
        g.dispose();

        // Convert the buffered image to a Mat
        return bufferedImageToMat(bufferedImage);
    }

    private static Mat bufferedImageToMat(BufferedImage bufferedImage) {
        byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    public static void main(String[] args) {
        // Example usage
        ImageIcon imageIcon = new ImageIcon("path/to/your/image.png");
        Mat matImage = imageIconToMat(imageIcon);

        // Now you have the OpenCV Mat representation of the ImageIcon
    }
}
