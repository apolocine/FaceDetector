package org.hmd.face.emp;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ImageSearch2 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static double compareImages(Mat image1, Mat image2) {
        // Convertir les images en niveaux de gris
        Mat gray1 = new Mat();
        Mat gray2 = new Mat();
        Imgproc.cvtColor(image1, gray1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(image2, gray2, Imgproc.COLOR_BGR2GRAY);

        // Calculer les histogrammes des images
        Mat hist1 = new Mat();
        Mat hist2 = new Mat();
        
    	List<Mat> grayList1 = new ArrayList<Mat>();
		grayList1.add(gray1);
		List<Mat> grayList2 = new ArrayList<Mat>();
		grayList2.add(gray2);

		Imgproc.calcHist(grayList1, new MatOfInt(0), new Mat(), hist1, new MatOfInt(256), new MatOfFloat(0, 256));
		Imgproc.calcHist(grayList2, new MatOfInt(0), new Mat(), hist2, new MatOfInt(256), new MatOfFloat(0, 256));

        
        // Normaliser les histogrammes
        Core.normalize(hist1, hist1, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.normalize(hist2, hist2, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        // Calculer la corrélation des histogrammes
        return Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL);
    }

    public static String findBestMatch(Mat queryImage, List<Mat> imageSet) {
        String bestMatch = null;
        double maxSimilarity = -1.0;

        for (Mat image : imageSet) {
            double similarity = compareImages(queryImage, image);
            System.out.println("Similarity with " + image + ": " + similarity);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = image.toString();
            }
        }

        return bestMatch;
    }

    public static void main(String[] args) {
        // Charger la bibliothèque OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Charger l'image de requête
        Mat queryImage = Imgcodecs.imread("path/to/query/image.jpg");

        // Charger l'ensemble d'images
        List<Mat> imageSet = new ArrayList<>();
        imageSet.add(Imgcodecs.imread("path/to/image1.jpg"));
        imageSet.add(Imgcodecs.imread("path/to/image2.jpg"));
        imageSet.add(Imgcodecs.imread("path/to/image3.jpg"));

        // Recherche de la meilleure correspondance
        String bestMatch = findBestMatch(queryImage, imageSet);
        System.out.println("Best match: " + bestMatch);
    }
}
