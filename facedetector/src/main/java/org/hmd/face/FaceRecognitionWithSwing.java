package org.hmd.face;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class FaceRecognitionWithSwing {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private static JFrame frame;
	private static JPanel videoPanel;
	private static JTextArea resultTextArea;
	JEditorPane resultWebPanel; // = new JEditorPane("text/html", "")New addition

	JLabel qrCodeLabel = new JLabel();// "QRCode"

	static Mat queryImage = null;
	String paterne = Config.getPaterneExtracteID();
	String phpScriptPath = Config.getPhpScriptPath();
	String baseUrl = Config.getBaseAmiaUrl();
	// Ajouter le JTextField pour la saisie de l'ID
	private JTextField idTextField = new JTextField(10);
	// Ajouter le JButton pour prendre la photo
	private JButton captureButton = new JButton("Capture Photo");
	private JButton searchButton = new JButton("Search");
	// Liste des images à comparer
	String photoSrcPath = Config.getSearchPhotoPath();// "files" + File.separator + "photos" + File.separator + "search"
														// +
														// File.separator + "photoSRC.png";

	// Paterne saving name ;
	String directoryPath = Config.getPhotosDirectoryPath(); //
	String prefix = "ph_";
	String postfix = ".png";

	JPanel observer = new JPanel(new GridLayout(0, 1));

	// Add a JProgressBar
	JProgressBar progressBar = new JProgressBar(0, 100); // Specify the range (0-100)

	// Create a JButton for browsing images
	JButton browseButton = new JButton("Browse");

	private JCheckBox faceCheckBox;
	private JCheckBox eyesCheckBox;
	private JCheckBox lipsCheckBox;

	private CascadeClassifier faceCascade;
	private CascadeClassifier eyesCascade;
	private CascadeClassifier lipsCascade;

	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				int initialId = Integer.parseInt(args[0]);
				SwingUtilities.invokeLater(() -> new FaceRecognitionWithSwing(initialId));
			} catch (NumberFormatException e) {
				System.err.println("Erreur: L'argument doit être un entier valide.");
			}
		} else {
			SwingUtilities.invokeLater(() -> new FaceRecognitionWithSwing(0));
		}
	}

	public FaceRecognitionWithSwing(int initialId) {
		// TODO Auto-generated constructor stub
		// Set up Swing components
		setupUI();

		idTextField.setText(String.valueOf(initialId)); // Initializer avec la valeur passée en argument

		if (initialId != 0) {

			// Initialize video capture
			VideoCapture capture = new VideoCapture(0);
			while (true) {

				// Capture a frame from the camera
				queryImage = imageVideoCapture(capture);

				// Display the current frame in the video panel
				updateVideoPanel(queryImage, queryImage, null, 0);

				// Perform face recognition
				performFaceRecognition(queryImage);

				// Ask user if they want to quit
				int response = JOptionPane.showConfirmDialog(frame, "Voulez-vous quitter le programme ?",
						"Confirmation", JOptionPane.YES_NO_OPTION);

				if (response == JOptionPane.YES_OPTION) {
					// Quit the program
					break;
				}

			}

		}

	}

	private void setupUI() {
		// Create and configure the main JFrame
		frame = new JFrame("Face Recognition");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);

		faceCheckBox = new JCheckBox("Face");
		eyesCheckBox = new JCheckBox("Eyes");
		lipsCheckBox = new JCheckBox("Lips");
		faceCheckBox.setSelected(true);
		eyesCheckBox.setSelected(false);
		lipsCheckBox.setSelected(false);
		// Add the checkboxes to the observer panel
		observer.add(faceCheckBox);
		observer.add(eyesCheckBox);
		observer.add(lipsCheckBox);

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

		// Ajouter le JTextField pour la saisie de l'ID
		observer.add(idTextField);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

		// Ajouter le JButton pour prendre la photo
		captureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				queryImage = handleCaptureButtonClick();

				// Display the current frame in the video panel
				updateVideoPanel(queryImage, queryImage, null, 0);

			}
		});
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleSearcButtonClick();
			}
		});
		buttonPanel.add(searchButton);
		buttonPanel.add(captureButton);

		observer.add(buttonPanel);

		JScrollPane qrScrollPane = new JScrollPane(qrCodeLabel);
		qrScrollPane.setPreferredSize(new Dimension(300, 200));

		// Add the browse button to the content pane
		observer.add(qrScrollPane);
		observer.add(progressBar);
		// observer.add(browseButton);
		// observer.add(scrollPane);

		observer.setPreferredSize(new Dimension(200, 400));

		JScrollPane observerScrollPane = new JScrollPane(observer);
		observerScrollPane.setPreferredSize(new Dimension(300, 200));

		frame.getContentPane().add(observerScrollPane);
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

	/**
	 * 
	 * @return
	 */
	private Mat captureImage() {
		VideoCapture capture = new VideoCapture(0);
		Mat capturedImage = new Mat();

		if (capture.isOpened()) {
			capture.read(capturedImage);
			capture.release();
			return capturedImage;
		} else {
			JOptionPane.showMessageDialog(frame, "Erreur: Impossible d'ouvrir la caméra.", "Erreur",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * 
	 * @return
	 */
	protected Mat handleSearcButtonClick() {
		try {
			progressBar.setValue(0);

			// Capturer une image avec VideoCapture
			Mat capturedImage = captureImage();

			if (capturedImage != null) {
				synchronized (capturedImage) {
					// show on Panels affichage
					updateVideoPanel(capturedImage, capturedImage, null, 0);
					// Enregistrer l'image avec le nom ph_"id".png
					saveImage(capturedImage, photoSrcPath);
					// // Perform face recognition
					performFaceRecognition(capturedImage);
				}

//				JOptionPane.showMessageDialog(frame, "Photo capturée et enregistrée avec succès.", "Succès",
//						JOptionPane.INFORMATION_MESSAGE);

				return capturedImage;
			} else {
				JOptionPane.showMessageDialog(frame, "Erreur lors de la capture de la photo.", "Erreur",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(frame, "Veuillez entrer un ID numérique valide.", "Erreur",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;

	}

	/**
	 * @return
	 * 
	 */
	private Mat handleCaptureButtonClick() {
		// Obtenir l'ID à partir du JTextField
		String idText = idTextField.getText();
		if (idText.isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Veuillez entrer un ID valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		int id = Integer.parseInt(idText);
		Mat capturedImage = null;
		if (id != 0) {

			String filePath = directoryPath + prefix + id + postfix;
			capturedImage = handleCaptureButtonClick(filePath);
			updateWebPanel(filePath, id, null);

			idTextField.setText("0");
		} else {
			JOptionPane.showMessageDialog(frame, "Veuillez entrer un ID valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
		}

		return capturedImage;
	}

	/**
	 * 
	 * @param filename
	 * @return
	 */
	private Mat handleCaptureButtonClick(String filePath) {

		try {

			// Capturer une image avec VideoCapture
			Mat capturedImage = captureImage();

			if (capturedImage != null) {
				// Enregistrer l'image avec le nom ph_"id".png
				saveImage(capturedImage, filePath);

				String message = "Photo capturée et enregistrée avec succès.";
				System.out.println(message);

//				JOptionPane.showMessageDialog(frame,message , "Succès",
//						JOptionPane.INFORMATION_MESSAGE);

				return capturedImage;
			} else {
				JOptionPane.showMessageDialog(frame, "Erreur lors de la capture de la photo.", "Erreur",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(frame, "Veuillez entrer un ID numérique valide.", "Erreur",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
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

		// Create a JEditorPane for displaying web content
		resultWebPanel = new JEditorPane("text/html", "");
		resultWebPanel.setEditable(false);
		JScrollPane webScrollPane = new JScrollPane(resultWebPanel);
		webScrollPane.setPreferredSize(new Dimension(300, 300));

		// Add the web panel to the content pane
		videoPanel.add(webScrollPane);

		// Revalidate and repaint the videoPanel
		videoPanel.revalidate();
		videoPanel.repaint();

	}

	private void updateWebPanel(String content) {
		resultWebPanel.setText(content);
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

	private void callPhpApplication(String baseUrl, int id) {

		// Construct the URL with the ID as a query parameter
		String stringURL = baseUrl + "?" + id;
		// Open the default web browser with the specified URL
		openWebBrowser(stringURL);

//		 	try {
//				openHTTPConexion(stringURL);
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ProtocolException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

	}

	private void openHTTPConexion(String stringURL) throws MalformedURLException, IOException, ProtocolException {
		// Construct the URL with the ID as a query parameter
		URL url = new URL(stringURL);

		// Open a connection to the URL
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// Set the request method (GET, POST, etc.)
		connection.setRequestMethod("GET");

		// Get the response code
		int responseCode = connection.getResponseCode();
		System.out.println("HTTP Response Code: " + responseCode);

		// Read the response from the server
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder resultContentBuilder = new StringBuilder();

		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			resultContentBuilder.append(line).append("\n");
		}
		// Get the final result content
		String resultContent = resultContentBuilder.toString();
		// Update the web panel with the result content

		/**
		 * @TOUNCOMMENT
		 */
		updateWebPanel("HTTP Response Code: " + responseCode + "\n" + resultContent);

		// Close the connection
		connection.disconnect();
	}

	/**
	 * 
	 * @param url
	 */
	private void openWebBrowser(String url) {
		// Check if the Desktop class is supported (available on Windows, Linux, and
		// macOS)
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();

			try {
				// Open the default web browser with the specified URL
				desktop.browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			// Desktop not supported, handle it accordingly (e.g., display an error message)
			System.out.println("Desktop is not supported. Unable to open the web browser.");
		}
	}

	/*
	 * 
	 */
	private void updateWebPanel(String filePath, int id, ImageIcon qrImage) {
		// Construire le contenu HTML avec l'image en cours de contrôle
		String htmlContent = "<html><body><h2>Result for ID: " + id + "</h2>";
		htmlContent += "<img src='file:" + filePath + "' width='200'/><br/>";
		htmlContent += "<p>Additional information or results can be displayed here.</p>";
//		String image  = ImageProsess.createImgTagWithImage(qrImage);
//		htmlContent +=	image;

		htmlContent += "</body></html>";

		// Mettre à jour le contenu du JEditorPane
		resultWebPanel.setText(htmlContent);
	}

	
	
	
	
	
	
	private void performFaceRecognition(Mat queryImage) {
	    // ... (existing code for face recognition)

	    // Check if the Mat is empty
	    if (queryImage == null || queryImage.empty()) {
	        return;
	    }

	    boolean detectFace = faceCheckBox.isSelected();
	    boolean detectEyes = eyesCheckBox.isSelected();
	    boolean detectLips = lipsCheckBox.isSelected();

	    double seuilDeSimilarite = 0.9; // Example threshold value, adjust as needed

	    // Charger le classificateur en cascade pour la détection des visages
	    faceCascade = new CascadeClassifier("files/data/haarcascades/haarcascade_frontalface_default.xml");
	    eyesCascade = new CascadeClassifier("files/data/haarcascades/haarcascade_eye.xml");
//	    lipsCascade = new CascadeClassifier("files/data/haarcascades/haarcascade_mcs_mouth.xml");
	    lipsCascade = new CascadeClassifier("files/data/haarcascades/haarcascade_smile.xml");

	    // Détection des visages dans l'image de requête
	    MatOfRect faceDetections = new MatOfRect();
	    if (detectFace) {
	        faceCascade.detectMultiScale(queryImage, faceDetections);
	    }

	    // Détection des yeux dans l'image de requête
	    MatOfRect eyesDetections = new MatOfRect();
	    if (detectEyes) {
	        eyesCascade.detectMultiScale(queryImage, eyesDetections);
	    }

	    // Détection des levres dans l'image de requête
	    MatOfRect lipsDetections = new MatOfRect();
	    if (detectLips) {
	        lipsCascade.detectMultiScale(queryImage, lipsDetections);
	    }

	    // Liste pour stocker les caractéristiques des visages détectés dans l'image de
	    // requête
	    List<Mat> queryFaceFeatures = new ArrayList<>();
	    List<String /* File */> foundedFaceFeatures = new ArrayList<>();

	    // Extraction des caractéristiques des visages détectés
	    for (Rect rect : faceDetections.toArray()) {
	        Mat faceROI = new Mat(queryImage, rect);
	        Mat hist = computeHistogram(faceROI);
	        queryFaceFeatures.add(hist);
	    }

	    if (queryFaceFeatures == null || queryFaceFeatures.size() == 0) {
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

	    // Comparaison des caractéristiques avec les images de la liste
	    for (int i = 0; i < imageSet.size(); i++) {
	        Mat currentExaminedImage = imageSet.get(i);

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

	            MatOfRect eyesInList = new MatOfRect();
	            MatOfRect lipsInList = new MatOfRect();

	            // Détection des yeux dans l'image de la liste
	            if (detectEyes) {
	                eyesCascade.detectMultiScale(currentExaminedImage, eyesInList);
	            }

	            // Détection des levres dans l'image de la liste
	            if (detectLips) {
	                lipsCascade.detectMultiScale(currentExaminedImage, lipsInList);
	            }

	          //  updateVideoPanel(queryImage, currentExaminedImage, faceROI, eyesInList, lipsInList, progress);
	            updateVideoPanel(queryImage, currentExaminedImage, null, progress);
	            // Ajoutez votre logique pour gérer la correspondance
	            if (similarity > seuilDeSimilarite) {
	                System.out.println("Similarity with image in the list best -> : " + similarity);
	                System.out.println("Image in the list matched!");
	                // Faites quelque chose avec l'image correspondante
	                bestMatchIndex = i;
	            }
	        }

	        if (bestMatchIndex != -1) {
	            if (!foundedFaceFeatures.contains(imagePathList.get(bestMatchIndex))) {
	                foundedFaceFeatures.add(imagePathList.get(bestMatchIndex));
	            }

	            // Extract the file name from the full path
	            bestMatchFileName = new File(imagePathList.get(bestMatchIndex)).getPath();
	            Mat founded = imageSet.get(bestMatchIndex);
	            updateVideoPanel(queryImage, founded, null, progress);

	            int id = extracteId(imagePathList.get(bestMatchIndex));
	        } else {
	            updateWebPanel("", -1, null); // Pas de correspondance
	            updateVideoPanel(queryImage, null, null, progress);
	        }
	    }

	    if (foundedFaceFeatures.size() != 0) {
	        int extractedId = extracteId(foundedFaceFeatures.get(0));
	        // Call PHP application with the extracted ID
	        callPhpApplication(baseUrl, extractedId);

	        String link = baseUrl + "?id=" + extractedId; // Remplacez cela par votre URL
	        int qrCodeWidth = 100;
	        int qrCodeHeight = 100;

	        ImageIcon imagQRCode = generateQRCode(link, qrCodeWidth, qrCodeHeight);
	        qrCodeLabel.removeAll();
	        qrCodeLabel.setIcon(imagQRCode);

	        String filePath = directoryPath + prefix + extractedId + postfix;
	        updateWebPanel(filePath, extractedId, imagQRCode);
	    }

	    if (bestMatchFileName != null) {
	        resultTextArea.append("Best Match: " + bestMatchFileName + "\n");
	    }
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	private void performFaceRecognition___(Mat queryImage) {
		// ... (existing code for face recognition)

		// Check if the Mat is empty
		if (queryImage == null || queryImage.empty()) {
			return;
		}

		boolean detectFace = faceCheckBox.isSelected();
		boolean detectEyes = eyesCheckBox.isSelected();
		boolean detectLips = lipsCheckBox.isSelected();

//      double seuilDeSimilarite = -1.0;
		double seuilDeSimilarite = 0.9; // Example threshold value, adjust as needed

		// Charger le classificateur en cascade pour la détection des visages
		faceCascade = new CascadeClassifier("files/data/haarcascades/haarcascade_frontalface_default.xml");
		eyesCascade = new CascadeClassifier("files/data/haarcascades/haarcascade_eye.xml");
		lipsCascade = new CascadeClassifier("files/data/haarcascades/haarcascade_mcs_mouth.xml");

		// Détection des visages dans l'image de requête
		MatOfRect faceDetections = new MatOfRect();
		if (detectFace) {
			faceCascade.detectMultiScale(queryImage, faceDetections);
		}

		 // Détection des yeux dans l'image de requête
	    MatOfRect eyesDetections = new MatOfRect();

	    if (detectEyes) {
	        eyesCascade.detectMultiScale(queryImage, eyesDetections);
	    }

	    // Détection des levres dans l'image de requête
	    MatOfRect lipsDetections = new MatOfRect();

	    if (detectLips) {
	        lipsCascade.detectMultiScale(queryImage, lipsDetections);
	    }
	    
	    
	    
	    
		// Liste pour stocker les caractéristiques des visages détectés dans l'image de
		// requête
		List<Mat> queryFaceFeatures = new ArrayList<>();
		List<String /* File */> foundedFaceFeatures = new ArrayList<>();

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
					System.out.println("Similarity with image in the list best -> : " + similarity);
					System.out.println("Image in the list matched!");
					// Faites quelque chose avec l'image correspondante
					bestMatchIndex = i;

				}

			}

			if (bestMatchIndex != -1) {
				if (!foundedFaceFeatures.contains(/* new File( */imagePathList.get(bestMatchIndex)/* ) */)) {
					foundedFaceFeatures.add(/* new File( */imagePathList.get(bestMatchIndex)/* ) */);

				}
				// foundedFaceFeatures.add(new File(imagePathList.get(bestMatchIndex)));
				// Extract the file name from the full path
				bestMatchFileName = new File(imagePathList.get(bestMatchIndex)).getPath();
				Mat founded = imageSet.get(bestMatchIndex);
				updateVideoPanel(queryImage, founded, null, progress);

				int id = extracteId(imagePathList.get(bestMatchIndex));

//				String filePath = directoryPath + "control_image_" + id + ".png";
//
//				// Enregistrer l'image en cours de contrôle
//				saveImage(queryImage, filePath);

			} else {

				updateWebPanel("", -1, null); // Pas de correspondance
//				Mat qrCodeImage = null;
//				Mat capturedImage = null;
//				updateResultWebPanel(capturedImage,   qrCodeImage,   -1);
				updateVideoPanel(queryImage, null, null, progress);
			}

		}

		System.out.println(foundedFaceFeatures);
		if (foundedFaceFeatures.size() != 0) {
			int extractedId = extracteId(foundedFaceFeatures.get(0));
			// Call PHP application with the extracted ID
			// callPhpApplication(id);
			callPhpApplication(baseUrl, extractedId);

//			"http://localhost:8083/tickets?id="
			String link = baseUrl + "?id=" + extractedId; // Remplacez cela par votre URL
			int qrCodeWidth = 100;
			int qrCodeHeight = 100;

			ImageIcon imagQRCode = generateQRCode(link, qrCodeWidth, qrCodeHeight);
			qrCodeLabel.removeAll();
			qrCodeLabel.setIcon(imagQRCode);
			// Mettre à jour le JEditorPane avec le résultat web

			String filePath = directoryPath + prefix + extractedId + postfix;

			updateWebPanel(filePath, extractedId, imagQRCode);
//			Mat imagMat = ImageIconToMatConverter.imageIconToMat(imagQRCode);
//			updateResultWebPanel(queryFaceFeatures.get(0),   imagMat,   extractedId);

		}

//  		System.out.println(bestMatchFileName);
		// Display the best match result in the JTextArea
		if (bestMatchFileName != null) {
			resultTextArea.append("Best Match: " + bestMatchFileName + "\n");
		}
	}

	private void saveImage(Mat image, String filePath) {
		File outputFile = new File(filePath);
		Imgcodecs.imwrite(outputFile.getAbsolutePath(), image);
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
			showError("Error: Unable to open the camera.");

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

	private ImageIcon generateQRCode(String data, int width, int height) {
		Map<EncodeHintType, Object> hints = new HashMap<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		QRCodeWriter writer = new QRCodeWriter();

		try {
			BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
			BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
			System.out.println("QR Code generated successfully.");

//		        String filePath = "files/exemple/QRCode.png";
//		        try {
//		            ImageIO.write(image, "png", new File(filePath));
//		        } catch (IOException e) {
//		            e.printStackTrace();
//		        }

			ImageIcon icon = new ImageIcon(image);
			return icon;
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void updateResultWebPanel_(Mat photoImage, Mat qrCodeImage, int id) {
		String photoBase64 = ImageProsess.encodeImageToBase64(photoImage);
		String qrCodeBase64 = ImageProsess.encodeImageToBase64(qrCodeImage);
		String htmlContent = ImageProsess.generateHTMLContent(photoBase64, qrCodeBase64, id);
		displayHTMLContent(htmlContent, resultWebPanel);
	}

	// Add a method to display HTML content in the resultWebPanel
	private static void displayHTMLContent(String htmlContent, JEditorPane resultWebPanel) {
		resultWebPanel.setContentType("text/html");
		resultWebPanel.setText(htmlContent);
	}

	private static void generateQRCode(String data, String filePath, int width, int height) {
		Map<EncodeHintType, Object> hintMap = new HashMap<>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix;

		try {
			bitMatrix = qrCodeWriter.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, width, height, hintMap);
		} catch (WriterException e) {
			e.printStackTrace();
			return;
		}

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.createGraphics();

		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		graphics.setColor(Color.BLACK);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (bitMatrix.get(i, j)) {
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}

		try {
			ImageIO.write(image, "png", new File(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("QR Code generated successfully.");
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

}
