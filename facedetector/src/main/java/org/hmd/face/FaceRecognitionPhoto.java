package org.hmd.face;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Retrouve la photo correspandante dans une liste à celle à celle aquise par la camera.
 * Methose analyse du visage
 */
public class FaceRecognitionPhoto {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Charger l'image de requête
 		VideoCapture capture = new VideoCapture(0);
 	 	Mat queryImage = imageVideoCapture(capture );  

		String photoSrc = "files" + File.separator + "photos" + File.separator + "search" + File.separator
				+ "photoSRC.png";
//		// Charger l'image de requête
//		Mat queryImage = Imgcodecs.imread(photoSrc);
//		Mat queryImage = imageFromDisc(photoSrc);
        
        
        
        
//    double seuilDeSimilarite = -1.0;
    double seuilDeSimilarite = 0.8;  // Example threshold value, adjust as needed

        // Charger le classificateur en cascade pour la détection des visages
        CascadeClassifier faceCascade = new CascadeClassifier("files/data/haarcascades/haarcascade_frontalface_default.xml");

        // Détection des visages dans l'image de requête
        MatOfRect faceDetections = new MatOfRect();
        faceCascade.detectMultiScale(queryImage, faceDetections);

        // Liste pour stocker les caractéristiques des visages détectés dans l'image de requête
        List<Mat> queryFaceFeatures = new ArrayList<>();

        // Extraction des caractéristiques des visages détectés
        for (Rect rect : faceDetections.toArray()) {
            Mat faceROI = new Mat(queryImage, rect);
            Mat hist = computeHistogram(faceROI);
            queryFaceFeatures.add(hist);
        }

        
        if(queryFaceFeatures!=null && queryFaceFeatures.size()!=0) {
        	 
        }else {
        	System.out.println("Pas de visage dans la photo");
        	System.out.println("Arret d traitement ");
        	return;
        }
        // Liste des images à comparer
    	String directoryPath = "files" + File.separator + "photos" + File.separator + "aquired" + File.separator;
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
		
		
        
        // Charger les images de la liste (remplacez avec votre propre code pour charger les images)

        // Comparaison des caractéristiques avec les images de la liste
		for (int i = 0; i < imageSet.size(); i++) {
			Mat image = imageSet.get(i);
//		}
//        for (Mat image : imageSet) {
            // Détection des visages dans l'image de la liste
            MatOfRect facesInList = new MatOfRect();
            faceCascade.detectMultiScale(image, facesInList);

            // Comparaison des caractéristiques des visages détectés
            for (Rect rect : facesInList.toArray()) {
                Mat faceROI = new Mat(image, rect);
                Mat hist = computeHistogram(faceROI);

                // Comparer les histogrammes
                double similarity = Imgproc.compareHist(hist, queryFaceFeatures.get(0), Imgproc.CV_COMP_CORREL);
                System.out.println("Similarity with image in the list: " + similarity);

            
				// Ajoutez votre logique pour gérer la correspondance
                if (similarity > seuilDeSimilarite) {
                	
                	//choisir le meilleur 
                	//seuilDeSimilarite = similarity;
                    System.out.println("Image in the list matched!");
                    // Faites quelque chose avec l'image correspondante
                    bestMatchIndex = i ;
                     
                }
                
                if (bestMatchIndex != -1) {
        			// Extract the file name from the full path
        			bestMatchFileName = new File(imagePathList.get(bestMatchIndex)).getPath();
        			System.out.println(bestMatchFileName);
        			// Save the best match to the output directory

//        	            String outputPath = outputDirectory + File.separator + "best_match.jpg";
//        	            Imgcodecs.imwrite(outputPath, imageSet.get(bestMatchIndex));

        		}
            }
        }
		
		
//		System.out.println(bestMatchFileName);
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

	
	
}
