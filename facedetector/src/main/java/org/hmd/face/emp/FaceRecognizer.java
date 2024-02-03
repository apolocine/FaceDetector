package org.hmd.face.emp;
import org.opencv.core.*;
import org.opencv.face.LBPHFaceRecognizer;
//import org.opencv.face.FaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class FaceRecognizer {
	
 
    
    private CascadeClassifier faceDetector;
    private LBPHFaceRecognizer faceRecognizer;

    public FaceRecognizer() {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
        
        faceDetector = new CascadeClassifier("files/data/haarcascades/haarcascade_frontalface_alt.xml");
        faceRecognizer = LBPHFaceRecognizer.create();
    }

    public void train(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        List<Mat> images = new ArrayList<>();
        Mat labels = new Mat(files.length, 1, CvType.CV_32SC1);
        int counter = 0;

        for (File file : files) {
            Mat image = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(image, faceDetections);

            for (Rect rect : faceDetections.toArray()) {
                Mat face = new Mat(image, rect);
                images.add(face);
                // Supposons que le nom de fichier contient l'ID de la personne (par exemple, "person1.jpg")
                labels.put(counter, 0, getLabelFromFile(file)); // getLabelFromFile est une méthode à implémenter
                counter++;
            }
        }

        faceRecognizer.train(images, labels);
        saveModel(); // Méthode pour sauvegarder le modèle entraîné
    }

    private void saveModel() {
        // Implémenter la logique pour sauvegarder le modèle entraîné
   
        String filename = "files\\config\\model.xml";
        saveModel(  filename);
    }

	private void saveModel(String filename) {
		 
		faceRecognizer.write(filename);
        System.out.println("Model saved to " + filename);
	}
    private int getLabelFromFile(File file) {
        // Implémenter la logique pour extraire l'étiquette (ID de la personne) du nom de fichier
        return Integer.parseInt(file.getName().split("\\.")[0].replaceAll("\\D+",""));
    }
    
    
    
    // Ajouter d'autres méthodes au besoin pour détecter, reconnaître et enregistrer des visages
    
    public static void main(String[] args) {
        FaceRecognizer recognizer = new FaceRecognizer();
        recognizer.train("files\\config\\");

        // Sauvegarder le modèle entraîné
        recognizer.saveModel("files\\config\\my_face_model.xml");
    }


}
