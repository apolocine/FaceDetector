package org.hmd.face;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageProsess {

	public static void main(String[] args) {
		// Load an image from resources
		ImageIcon imageIcon = loadImageIcon("/path/to/your/image.png");

		// Create HTML content with the embedded image
		String htmlContent = createHTMLWithImage(imageIcon);

		// Display HTML content in a JOptionPane
		JOptionPane.showMessageDialog(null, new JLabel(htmlContent), "HTML with ImageIcon", JOptionPane.PLAIN_MESSAGE);
	}

	private static ImageIcon loadImageIcon(String imagePath) {
		// Load image from resources
		InputStream inputStream = ImageProsess.class.getResourceAsStream(imagePath);
		if (inputStream != null) {
			try {
				byte[] imageData = readImageData(inputStream);
				return new ImageIcon(imageData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String createHTMLWithImage(ImageIcon imageIcon) {
		if (imageIcon != null) {

			// Create HTML content with embedded image
			return "<html><body>" + createImgTagWithImage(imageIcon) + "</body></html>";
		} else {
			return "<html><body><p>Image not found!</p></body></html>";
		}
	}

	public static String createImgTagWithImage(ImageIcon imageIcon) {
		if (imageIcon != null) {
			// Convert ImageIcon to byte array
			byte[] imageData = imageIconToByteArray(imageIcon);

			// Encode byte array to base64
			String base64Image = Base64.getEncoder().encodeToString(imageData);

			// Create HTML content with embedded image
			return "<img src='data:image/png;base64," + base64Image + "' width='200'/>";
		} else {
			return "<p>Image not found!</p>";
		}
	}

	private static byte[] imageIconToByteArray(ImageIcon imageIcon) {
		try {
			// Extract Image from ImageIcon
			Image image = imageIcon.getImage();

			// Convert Image to byte array
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			javax.imageio.ImageIO.write(imageToBufferedImage(image), "png", byteArrayOutputStream);

			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	private static byte[] readImageData(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		return outputStream.toByteArray();
	}

	private static java.awt.image.BufferedImage imageToBufferedImage(Image image) {
		// Ensure image is fully loaded
		image = new ImageIcon(image).getImage();

		// Create BufferedImage
		java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(image.getWidth(null),
				image.getHeight(null), java.awt.image.BufferedImage.TYPE_INT_ARGB);

		// Draw image onto BufferedImage
		java.awt.Graphics g = bufferedImage.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bufferedImage;
	}
	
	
	public static String encodeImageToBase64(Mat image) {
		if(image == null) return "";
	    BufferedImage bufferedImage = matToBufferedImage(image, 60);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
	        ImageIO.write(bufferedImage, "png", baos);
	        byte[] imageData = baos.toByteArray();
	        return Base64.getEncoder().encodeToString(imageData);
	    } catch (IOException e) {
	        e.printStackTrace();
	        return "";
	    }
	}
	
	
	
	
	public static BufferedImage matToBufferedImage(Mat mat, int targetSize) {
		// Check if the Mat is empty
		if (mat == null || mat.empty()) {
			return null;
		}

		// Resize the Mat to the target size
		Mat resizedMat = new Mat();
		Imgproc.resize(mat, resizedMat, new Size(targetSize, targetSize));

		// Determine the image type (grayscale or color)
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (resizedMat.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}

		// Create a BufferedImage with the resized Mat dimensions and type
		BufferedImage bufferedImage = new BufferedImage(targetSize, targetSize, type);

		// Get the byte array of the resized Mat data
		byte[] data = new byte[resizedMat.channels() * resizedMat.cols() * resizedMat.rows()];
		resizedMat.get(0, 0, data);

		// Set the data buffer of the BufferedImage
		if (type == BufferedImage.TYPE_BYTE_GRAY) {
			// For grayscale images, use a single-byte data buffer
			bufferedImage.getRaster().setDataElements(0, 0, resizedMat.cols(), resizedMat.rows(), data);
		} else {
			// For color images, use a three-byte data buffer
			DataBufferByte dataBuffer = (DataBufferByte) bufferedImage.getRaster().getDataBuffer();
			byte[] imgData = dataBuffer.getData();
			System.arraycopy(data, 0, imgData, 0, data.length);
		}

		return bufferedImage;
	}
	
	
	public static String generateHTMLContent(String imagePath, String qrCodeImagePath, int id) {
	    StringBuilder htmlContent = new StringBuilder();

	    // Add HTML tags and content
	    htmlContent.append("<html>");
	    htmlContent.append("<body>");

	    // Add an image tag for the photo
	    htmlContent.append("<img src='data:image/png;base64,").append(imagePath).append("' width='300' height='300'>");

	    // Add a line break
	    htmlContent.append("<br>");

	    // Add an image tag for the QR code
	    htmlContent.append("<img src='data:image/png;base64,").append(qrCodeImagePath).append("' width='300' height='300'>");

	    // Add additional HTML content or styling as needed

	    // Close HTML tags
	    htmlContent.append("</body>");
	    htmlContent.append("</html>");

	    return htmlContent.toString();
	}

	
	

	
}
