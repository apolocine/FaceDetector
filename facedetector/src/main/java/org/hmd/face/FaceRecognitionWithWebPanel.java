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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceRecognitionWithWebPanel {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static JFrame frame;
    private static JPanel videoPanel;
    private static JTextArea resultTextArea;
    private static JEditorPane resultWebPanel;
    static Mat queryImage = null;
    String photoSrc = null;

    public static void main(String[] args) {
        // Set up Swing components
        setupUI();

        // Initialize video capture
        VideoCapture capture = new VideoCapture(0);
        String photoSrc = "files" + File.separator + "photos" + File.separator + "search" + File.separator
                + "photoSRC.png";

        if (!capture.isOpened()) {
            showError("Error: Unable to open the camera.");

            queryImage = handleBrowseButtonClick();
            if (queryImage == null) {
                queryImage = imageFromDisc(photoSrc);
            }

            updateVideoPanel(queryImage, queryImage, null);
            performFaceRecognition(queryImage);

        } else {
            while (true) {
                queryImage = imageVideoCapture(capture);
                updateVideoPanel(queryImage, queryImage, null);
                performFaceRecognition(queryImage);
            }
        }
    }

    private static void setupUI() {
        frame = new JFrame("Face Recognition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        videoPanel = new JPanel();
        frame.getContentPane().add(videoPanel);

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        scrollPane.setPreferredSize(new Dimension(300, 600));
        frame.getContentPane().add(scrollPane);

        // Create a JEditorPane for displaying HTML content
        resultWebPanel = new JEditorPane();
        resultWebPanel.setEditable(false);
        JScrollPane webScrollPane = new JScrollPane(resultWebPanel);
        webScrollPane.setPreferredSize(new Dimension(300, 600));
        frame.getContentPane().add(webScrollPane);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBrowseButtonClick();
            }
        });

        frame.getContentPane().add(browseButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, videoPanel, scrollPane);
        splitPane.setDividerLocation(600);
        frame.getContentPane().add(splitPane);

        frame.setVisible(true);

        Mat dummyQueryImage = Imgcodecs.imread("files/photos/search/photoSRC.png");
        Mat dummyExaminedImage = Imgcodecs.imread("files/photos/search/photoSRC.png");
        updateVideoPanel(dummyQueryImage, dummyExaminedImage, null);
    }

    private static Mat handleBrowseButtonClick() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Image File");

        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            Mat selectedImage = Imgcodecs.imread(selectedFile.getAbsolutePath());

            return selectedImage;
        }
        return null;
    }

    private static void showError(String errorMessage) {
        JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void updateVideoPanel(Mat queryImage, Mat currentExaminedImage, Mat faceRO) {
        BufferedImage queryBufferedImage = matToBufferedImage(queryImage, 180);
        BufferedImage examinedBufferedImage = matToBufferedImage(currentExaminedImage, 180);

        JLabel faceROLabel = new JLabel();

        if (faceRO != null) {
            BufferedImage faceROBufferedImage = matToBufferedImage(faceRO, 180);
            ImageIcon faceROIcon = new ImageIcon(faceROBufferedImage);
            faceROLabel = new JLabel(faceROIcon);
        }

        ImageIcon queryIcon = new ImageIcon(queryBufferedImage);
        ImageIcon examinedIcon = new ImageIcon(examinedBufferedImage);

        JLabel queryLabel = new JLabel(queryIcon);
        JLabel examinedLabel = new JLabel(examinedIcon);

        videoPanel.removeAll();

        JPanel rightPanel = new JPanel();
        rightPanel.add(queryLabel);
        videoPanel.add(rightPanel);

        videoPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        JPanel leftPanel = new JPanel();
        leftPanel.add(examinedLabel);
        videoPanel.add(leftPanel);

        JPanel faceROPanel = new JPanel();
        faceROPanel.add(faceROLabel);
        videoPanel.add(faceROPanel);

        videoPanel.revalidate();
        videoPanel.repaint();
    }

    private static void performFaceRecognition(Mat queryImage) {
        double seuilDeSimilarite = 0.8;

        CascadeClassifier faceCascade = new CascadeClassifier(
                "files/data/haarcascades/haarcascade_frontalface_default.xml");

        MatOfRect faceDetections = new MatOfRect();
        faceCascade.detectMultiScale(queryImage, faceDetections);

        List<Mat> queryFaceFeatures = new ArrayList<>();

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

        String directoryPath = "files" + File.separator + "photos" + File.separator + "aquired" + File.separator;
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Le répertoire spécifié n'existe pas ou n'est pas un répertoire.");
            return;
        }

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

        for (int i = 0; i < imageSet.size(); i++) {
            Mat currentExaminedImage = imageSet.get(i);

            MatOfRect facesInList = new MatOfRect();
            faceCascade.detectMultiScale(currentExaminedImage, facesInList);

            for (Rect rect : facesInList.toArray()) {
                Mat faceROI = new Mat(currentExaminedImage, rect);
                Mat hist = computeHistogram(faceROI);

                double similarity = Imgproc.compareHist(hist, queryFaceFeatures.get(0), Imgproc.CV_COMP_CORREL);
                System.out.println("Similarity with image in the list: " + similarity);

                updateVideoPanel(queryImage, currentExaminedImage, faceROI);

                if (similarity > seuilDeSimilarite) {
                    System.out.println("Image in the list matched!");
                    bestMatchIndex = i;
                }

                if (bestMatchIndex != -1) {
                    bestMatchFileName = new File(imagePathList.get(bestMatchIndex)).getPath();
                    Mat founded = imageSet.get(bestMatchIndex);

                    // Display the best match result in the JTextArea
                    resultTextArea.append("Best Match: " + bestMatchFileName + "\n");

                    // Append the result to the JEditorPane (web panel)
                    appendResultToWebPanel("Best Match: " + bestMatchFileName + "<br>");
                }
            }
        }
    }

    private static void appendResultToWebPanel(String result) {
        String currentContent = resultWebPanel.getText();
        resultWebPanel.setText(currentContent + result);
        resultWebPanel.setCaretPosition(resultWebPanel.getDocument().getLength());
    }

    private static BufferedImage matToBufferedImage(Mat mat, int targetSize) {
        if (mat.empty()) {
            return null;
        }

        Mat resizedMat = new Mat();
        Imgproc.resize(mat, resizedMat, new Size(targetSize, targetSize));

        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (resizedMat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        BufferedImage bufferedImage = new BufferedImage(targetSize, targetSize, type);
        byte[] data = new byte[resizedMat.channels() * resizedMat.cols() * resizedMat.rows()];
        resizedMat.get(0, 0, data);

        if (type == BufferedImage.TYPE_BYTE_GRAY) {
            bufferedImage.getRaster().setDataElements(0, 0, resizedMat.cols(), resizedMat.rows(), data);
        } else {
            DataBufferByte dataBuffer = (DataBufferByte) bufferedImage.getRaster().getDataBuffer();
            byte[] imgData = dataBuffer.getData();
            System.arraycopy(data, 0, imgData, 0, data.length);
        }

        return bufferedImage;
    }

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
        if (!capture.isOpened()) {
            System.out.println("Erreur: Impossible d'ouvrir la caméra.");
            return null;
        }

        Mat frame = new Mat();
        capture.read(frame);

        if (frame.empty()) {
            System.out.println("Erreur: Impossible de charger l'image.");
            return null;
        }

        return frame;
    }

    public static Mat imageFromDisc(String inImagePath) {
        Mat frame = Imgcodecs.imread(inImagePath);

        if (frame.empty()) {
            System.out.println("Erreur: Impossible de charger l'image.");
            return null;
        }

        return frame;
    }
}
