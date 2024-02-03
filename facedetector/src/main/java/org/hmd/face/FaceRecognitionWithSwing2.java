package org.hmd.face;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaceRecognitionWithSwing2 {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private static JFrame frame;
	private static JPanel videoPanel;
	private static JTextArea resultTextArea;
	static Mat queryImage = null;
	String photoSrc = null;

	String paterne = "_(\\d+)\\.";
	String phpScriptPath = "http://localhost:8083/tickets";// "path/to/your/php/script.php";

	// Liste des images à comparer
	String directoryPath = "files" + File.separator + "photos" + File.separator + "aquired" + File.separator;

	JPanel observer = new JPanel(new GridLayout(4, 1));

	// Add a JProgressBar
	JProgressBar progressBar = new JProgressBar(0, 100); // Specify the range (0-100)

	// Create a JButton for browsing images
	JButton browseButton = new JButton("Browse");

	public FaceRecognitionWithSwing2() {
		// TODO Auto-generated constructor stub
		// Set up Swing components
		setupUI();

		// Initialize video capture
		VideoCapture capture = new VideoCapture(0);

		String photoSrc = "files" + File.separator + "photos" + File.separator + "search" + File.separator
				+ "photoSRC.png";

//		// Charger l'image de requête
//		Mat queryImage = Imgcodecs.imread(photoSrc);

		// Check if the camera is opened successfully
		if (!capture.isOpened()) {
			showError("Error: Unable to open the camera.");
			// return;

			queryImage = handleBrowseButtonClick();
			if (queryImage == null) {
				queryImage = imageFromDisc(photoSrc);
			}

			// Display the current frame in the video panel

			updateVideoPanel(queryImage, queryImage, null, 0);
			// Perform face recognition
			performFaceRecognition(queryImage);

		} else {
			while (true) {
				// Capture a frame from the camera
				queryImage = imageVideoCapture(capture);

				// Display the current frame in the video panel

				updateVideoPanel(queryImage, queryImage, null, 0);
				// Perform face recognition
				performFaceRecognition(queryImage);
			}
		}
	}

	public static void main(String[] args) {

		new FaceRecognitionWithSwing2();
	}

	private void setupUI() {
		// Create and configure the main JFrame
		frame = new JFrame("Face Recognition");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);

		// Create a JPanel for displaying the camera feed
		videoPanel = new JPanel();
		frame.getContentPane().add(videoPanel);

		// Create a JTextArea for displaying recognition results
		resultTextArea = new JTextArea();
		resultTextArea.setEditable(false);
		resultTextArea.setLineWrap(true);
		resultTextArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(resultTextArea);
		scrollPane.setPreferredSize(new Dimension(300, 600));

		progressBar.setStringPainted(true); // Display percentage
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Handle the browse button click event
				queryImage = handleBrowseButtonClick();
				try {
					progressBar.setValue(0);
					progressBar.repaint();
					resultTextArea.setText("");
					updateVideoPanel(queryImage, queryImage, null, 0);
				} finally {
					performFaceRecognition(queryImage);
				}

			}
		});

		// Add the browse button to the content pane
		observer.add(browseButton);
		observer.add(progressBar);
		observer.add(scrollPane);

		frame.getContentPane().add(observer);
		// Create a JSplitPane with videoPanel on the left and scrollPane on the right
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, videoPanel, observer);
		splitPane.setDividerLocation(600); // Adjust the initial divider location

		// Add the JSplitPane to the content pane
		frame.getContentPane().add(splitPane);
		// frame.getContentPane().add(progressBar);
		// Show the JFrame
		frame.setVisible(true);
		// Set up video panel with placeholder images
		Mat dummyQueryImage = Imgcodecs.imread("files/photos/search/photoSRC.png");
		Mat dummyExaminedImage = Imgcodecs.imread("files/photos/search/photoSRC.png");
		updateVideoPanel(dummyQueryImage, dummyExaminedImage, null, 0);

	}

	private static Mat handleBrowseButtonClick() {
		// Create a file chooser dialog
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select an Image File");

		// Show the file chooser dialog
		int result = fileChooser.showOpenDialog(frame);

		// Check if the user selected a file
		if (result == JFileChooser.APPROVE_OPTION) {
			// Get the selected file
			File selectedFile = fileChooser.getSelectedFile();

			// Load and display the selected image
			Mat selectedImage = Imgcodecs.imread(selectedFile.getAbsolutePath());

			// Additional logic based on the selected image can be added here
			return selectedImage;
		}
		return null;
	}

	private static void showError(String errorMessage) {
		JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void updateVideoPanel(Mat queryImage, Mat currentExaminedImage, Mat faceRO, int progress) {

		JLabel queryLabel = new JLabel();
		// Check if the Mat is empty
		if (queryImage == null || queryImage.empty()) {
			return;
		} else {
			BufferedImage queryBufferedImage = matToBufferedImage(queryImage, 180);
			ImageIcon queryIcon = new ImageIcon(queryBufferedImage);
			queryLabel.setIcon(queryIcon);
		}

		JLabel examinedLabel = new JLabel();
		if (currentExaminedImage == null || currentExaminedImage.empty()) {

		} else {
			BufferedImage examinedBufferedImage = matToBufferedImage(currentExaminedImage, 180);
			ImageIcon examinedIcon = new ImageIcon(examinedBufferedImage);
			examinedLabel.setIcon(examinedIcon);
		}

		JLabel faceROLabel = new JLabel();

		if (faceRO != null) {
			BufferedImage faceROBufferedImage = matToBufferedImage(faceRO, 180);
			ImageIcon faceROIcon = new ImageIcon(faceROBufferedImage);
			faceROLabel.setIcon(faceROIcon);
		}

		videoPanel.removeAll();

		// Add the right panel (photo to search)
		JPanel rightPanel = new JPanel();
		rightPanel.add(queryLabel);
		videoPanel.add(rightPanel);

		// Add a separator (optional, adjust as needed)
		videoPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		// Add the left panel (current frame being examined)
		JPanel leftPanel = new JPanel();
		leftPanel.add(examinedLabel);
		videoPanel.add(leftPanel);

		JPanel faceROPanel = new JPanel();
		faceROPanel.add(faceROLabel);
		videoPanel.add(faceROPanel);

		// Update the progress bar value
		progressBar.setValue(progress);

		// Revalidate and repaint the videoPanel
		videoPanel.revalidate();
		videoPanel.repaint();
	}

	private void performFaceRecognition(Mat queryImage) {
		// ... (existing code for face recognition)

		// Check if the Mat is empty
		if (queryImage == null || queryImage.empty()) {
			return;
		}
//      double seuilDeSimilarite = -1.0;
		double seuilDeSimilarite = 0.8; // Example threshold value, adjust as needed

		// Charger le classificateur en cascade pour la détection des visages
		CascadeClassifier faceCascade = new CascadeClassifier(
				"files/data/haarcascades/haarcascade_frontalface_default.xml");

		// Détection des visages dans l'image de requête
		MatOfRect faceDetections = new MatOfRect();
		faceCascade.detectMultiScale(queryImage, faceDetections);

		// Liste pour stocker les caractéristiques des visages détectés dans l'image de
		// requête
		List<Mat> queryFaceFeatures = new ArrayList<>();
		List<String> foundedFaceFeatures = new ArrayList<>();

		// Extraction des caractéristiques des visages détectés
		for (Rect rect : faceDetections.toArray()) {
			Mat faceROI = new Mat(queryImage, rect);
			Mat hist = computeHistogram(faceROI);
			queryFaceFeatures.add(hist);
		}

		if (queryFaceFeatures != null && queryFaceFeatures.size() != 0) {

		} else {
			System.out.println("Pas de visage dans la photo");
			System.out.println("Arret d traitement ");
			return;
		}
		// Spécifier le répertoire contenant les images
		File directory = new File(directoryPath);
		// Vérifier si le répertoire existe
		if (!directory.exists() || !directory.isDirectory()) {
			System.err.println("Le répertoire spécifié n'existe pas ou n'est pas un répertoire.");
			return;
		}

		// Charger l'ensemble d'images à partir du répertoire
		List<Mat> imageSet = new ArrayList<>();
		List<String> imagePathList = new ArrayList<>();
		String bestMatchFileName = null;
		int bestMatchIndex = -1;

		File[] files = directory.listFiles();

		List<String> extens = new ArrayList<String>();
		extens.add(".png");
		extens.add(".jpg");

		if (files != null) {
			for (File file : files) {

				if (file.isFile()) {
					for (String ext : extens) {
						if (file.getName().toLowerCase().endsWith(ext)) {
							Mat image = Imgcodecs.imread(file.getAbsolutePath());
							if (!image.empty()) {
								imageSet.add(image);
								imagePathList.add(file.getAbsolutePath());
							}
						}
					}

				}
			}
		}

		// Charger les images de la liste (remplacez avec votre propre code pour charger
		// les images)

		// Comparaison des caractéristiques avec les images de la liste
		for (int i = 0; i < imageSet.size(); i++) {
			Mat currentExaminedImage = imageSet.get(i);
//  		}
//          for (Mat image : imageSet) {

			// Calculate progress percentage
			int progress = (i + 1) * 100 / imageSet.size();

			// Détection des visages dans l'image de la liste
			MatOfRect facesInList = new MatOfRect();
			faceCascade.detectMultiScale(currentExaminedImage, facesInList);

			// Comparaison des caractéristiques des visages détectés
			for (Rect rect : facesInList.toArray()) {
				Mat faceROI = new Mat(currentExaminedImage, rect);
				Mat hist = computeHistogram(faceROI);

				// Comparer les histogrammes
				double similarity = Imgproc.compareHist(hist, queryFaceFeatures.get(0), Imgproc.CV_COMP_CORREL);
		 		System.out.println("Similarity with image in the list: " + similarity);

				updateVideoPanel(queryImage, currentExaminedImage, faceROI, progress);

				// Ajoutez votre logique pour gérer la correspondance
				if (similarity > seuilDeSimilarite) {
					

				
					// choisir le meilleur
					// seuilDeSimilarite = similarity;
					System.out.println("Image in the list matched!");
					
//					int id = extracteId(stringPath);
//					// Call PHP application with the extracted ID
//					callPhpApplication(id);
					
					// Faites quelque chose avec l'image correspondante
					bestMatchIndex = i;
				}

				for (String stringPath : foundedFaceFeatures) {
					System.out.println(stringPath);

				}
				
				
				if (bestMatchIndex != -1) {

					// Extract the file name from the full path
					bestMatchFileName = new File(imagePathList.get(bestMatchIndex)).getPath();
					if ( foundedFaceFeatures.contains(/*new File(*/imagePathList.get(bestMatchIndex)/*)*/)) {
						foundedFaceFeatures.add(/* new File( */imagePathList.get(bestMatchIndex)/* ) */);
						 
					} 
					
//					if (foundedFaceFeatures.size()==0) {
//						foundedFaceFeatures.add(/* new File( */imagePathList.get(bestMatchIndex)/* ) */);
//					}
					System.out.println(foundedFaceFeatures);
					
					Mat founded = imageSet.get(bestMatchIndex);
					updateVideoPanel(queryImage, founded, null, progress);
					
				} else {					
					updateVideoPanel(queryImage, null, null, progress);
				}

				
			}
		}

		//System.out.println(bestMatchFileName);
		// Display the best match result in the JTextArea
		if (bestMatchFileName != null) {
			resultTextArea.append("Best Match: " + bestMatchFileName + "\n");
		}
	}

	private int extracteId(String fileName) {
		// Pattern to extract the ID from the image name
		Pattern idPattern = Pattern.compile(paterne);
		Matcher matcher = idPattern.matcher(fileName);
		if (matcher.find()) {
			int id = Integer.parseInt(matcher.group(1));
			return id;
		}
		return 0;
	}
	/**
	 * 
	 * @param id
	 */
	private void callPhpApplication(int id) {
		// Code to call your PHP application with the extracted ID
		// You can use Java's ProcessBuilder or any other suitable method
		// For example:
		try {

			ProcessBuilder processBuilder = new ProcessBuilder("php", phpScriptPath, String.valueOf(id));
			processBuilder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

//	private void extracteId(Mat queryImage) {
//		// ... (existing code)
//
//		// Liste des images à comparer
//		String directoryPath = "files" + File.separator + "photos" + File.separator + "aquired" + File.separator;
//		// Spécifier le répertoire contenant les images
//		File directory = new File(directoryPath);
//		// Vérifier si le répertoire existe
//		if (!directory.exists() || !directory.isDirectory()) {
//			System.err.println("Le répertoire spécifié n'existe pas ou n'est pas un répertoire.");
//			return;
//		}
//
//		// Pattern to extract the ID from the image name
//		Pattern idPattern = Pattern.compile(paterne);
//
//		// Charger l'ensemble d'images à partir du répertoire
//		List<Mat> imageSet = new ArrayList<>();
//		List<String> imagePathList = new ArrayList<>();
//		String bestMatchFileName = null;
//		int bestMatchIndex = -1;
//
//		File[] files = directory.listFiles();
//
//		List<String> extens = new ArrayList<String>();
//		extens.add(".png");
//		extens.add(".jpg");
//
//		if (files != null) {
//			for (File file : files) {
//
//				if (file.isFile()) {
//					for (String ext : extens) {
//						if (file.getName().toLowerCase().endsWith(ext)) {
//							Mat image = Imgcodecs.imread(file.getAbsolutePath());
//							if (!image.empty()) {
//								imageSet.add(image);
//								imagePathList.add(file.getAbsolutePath());
//
//								// Extract ID from the image name using regex
//								String fileName = file.getName();
//								Matcher matcher = idPattern.matcher(fileName);
//								if (matcher.find()) {
//									int id = Integer.parseInt(matcher.group(1));
//									// Call PHP application with the extracted ID
//									callPhpApplication(id);
//								}
//							}
//						}
//					}
//
//				}
//			}
//		}
//
//		// ... (existing code)
//	}


	private static BufferedImage matToBufferedImage(Mat mat) {
		// Check if the Mat is empty
		if (mat.empty()) {
			return null;
		}

		// Determine the image type (grayscale or color)
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (mat.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}

		// Create a BufferedImage with the same dimensions and type as the Mat
		BufferedImage bufferedImage = new BufferedImage(mat.cols(), mat.rows(), type);

		// Get the byte array of the Mat data
		byte[] data = new byte[mat.channels() * mat.cols() * mat.rows()];
		mat.get(0, 0, data);

		// Set the data buffer of the BufferedImage
		if (type == BufferedImage.TYPE_BYTE_GRAY) {
			// For grayscale images, use a single-byte data buffer
			bufferedImage.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
		} else {
			// For color images, use a three-byte data buffer
			DataBufferByte dataBuffer = (DataBufferByte) bufferedImage.getRaster().getDataBuffer();
			byte[] imgData = dataBuffer.getData();
			System.arraycopy(data, 0, imgData, 0, data.length);
		}

		// Set the data buffer of the BufferedImage
		if (type == BufferedImage.TYPE_BYTE_GRAY) {
			// For grayscale images, use a single-byte data buffer
			bufferedImage.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
		} else {
			// For color images, use a three-byte data buffer
//            ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).setData(data);

			// For color images, use a three-byte data buffer
			DataBufferByte dataBuffer = (DataBufferByte) bufferedImage.getRaster().getDataBuffer();
			byte[] imgData = dataBuffer.getData();
			System.arraycopy(data, 0, imgData, 0, data.length);

		}

		return bufferedImage;
	}

	// Fonction pour calculer l'histogramme d'une image en niveaux de gris
	private static Mat computeHistogram(Mat image) {
		Mat hist = new Mat();
		Mat grayImage = new Mat();
		Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
		List<Mat> grayList1 = new ArrayList<Mat>();
		grayList1.add(grayImage);
		Imgproc.calcHist(grayList1, new MatOfInt(0), new Mat(), hist, new MatOfInt(256), new MatOfFloat(0, 256));
		Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX, -1, new Mat());
		return hist;
	}

	private static Mat imageVideoCapture(VideoCapture capture) {

		// Vérifier si la caméra est ouverte
		if (!capture.isOpened()) {
			System.out.println("Erreur: Impossible d'ouvrir la caméra.");
			return null;
		}

		Mat frame = new Mat();

		// Capturer une image depuis la caméra
		capture.read(frame);
		// Vérifier si l'image a été chargée avec succès
		if (frame.empty()) {
			System.out.println("Erreur: Impossible de charger l'image.");
			return null;
		}

		return frame;

	}

	public static Mat imageFromDisc(String inImagePath) {
		// Chemin vers l'image pré-enregistrée frame

		// Charger l'image à partir du disque
		Mat frame = Imgcodecs.imread(inImagePath);

		// Vérifier si l'image a été chargée avec succès
		if (frame.empty()) {
			System.out.println("Erreur: Impossible de charger l'image.");
			return null;
		}

		return frame;
	}

	private static BufferedImage matToBufferedImage(Mat mat, int targetSize) {
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

}
