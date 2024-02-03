package org.hmd.face.emp;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageSearch3 {
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

 
	public static Mat findBestMatch(Mat queryImage, List<Mat> imageSet) {
//		String bestMatch = null;
		Mat bestMatch = null;
		double maxSimilarity = -1.0;

		for (Mat image : imageSet) {
			double similarity = compareImages(queryImage, image);
			System.out.println("Similarity with " + image + ": " + similarity);

			if (similarity > maxSimilarity) {
				maxSimilarity = similarity;
				bestMatch = image;
			}
		} 
		return bestMatch;
	}

	public static void main(String[] args) {
		// Charger la bibliothèque OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		
//		VideoCapture capture = new VideoCapture(0);
//	 	Mat queryImage = imageVideoCapture(capture );  
		 
	
		String photoSrc = "files" + File.separator + "photos" + File.separator + "search" + File.separator
				+ "photoSRC.png";
//		// Charger l'image de requête
//		Mat queryImage = Imgcodecs.imread(photoSrc);
	Mat queryImage = imageFromDisc(photoSrc);
		
	
	
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

		
 
		String bestMatchPat = findBestMatchAndSave(queryImage, imageSet, imagePathList, directoryPath);
		System.out.println("Best match Path : " + bestMatchPat);

	}

	 
	   public static String findBestMatchAndSave(Mat queryImage, List<Mat> imageSet, List<String> imagePathList, String outputDirectory) {
	        String bestMatchFileName = null;
	        double maxSimilarity = -1.0;
	        int bestMatchIndex = -1;

	        for (int i = 0; i < imageSet.size(); i++) {
	            Mat image = imageSet.get(i);
	            double similarity = compareImages(queryImage, image);
	            System.out.println("Similarity with " + imagePathList.get(i) + ": " + similarity);

	            if (similarity > maxSimilarity) {
	                maxSimilarity = similarity;
	                bestMatchIndex = i;
	            }
	        }

	        if (bestMatchIndex != -1) {
	            // Extract the file name from the full path
	            bestMatchFileName = new File(imagePathList.get(bestMatchIndex)).getPath();

	            // Save the best match to the output directory
	            
//	            String outputPath = outputDirectory + File.separator + "best_match.jpg";
//	            Imgcodecs.imwrite(outputPath, imageSet.get(bestMatchIndex));
	           
	        }

	        return bestMatchFileName;
	    }

	   
	   
	   
	   
	   private static Mat imageVideoCapture(VideoCapture capture ) {

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
	   
	   
	   public static Mat  imageFromDisc(String inImagePath) {
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
	   
	   /*		// Recherche de la meilleure correspondance
		Mat bestMatch = findBestMatch(queryImage, imageSet);
		System.out.println("Best match: " + bestMatch); 
		
		// Save the best match to the output directory
		String outputPath = directoryPath + File.separator + "best_match.jpg";// new Mat(bestMatch) 
		
		Imgcodecs.imwrite(outputPath, bestMatch); 
		String fileName = new File(bestMatch.toString()).getName(); 
		
		System.out.println("FileName : " + fileName);
		
*/
	   
	   
//	 public static String findBestMatchAndSave(Mat queryImage, List<Mat> imageSet, String outputDirectory) {
//	        String bestMatch = null;
//	        double maxSimilarity = -1.0;
//
//	        for (Mat image : imageSet) {
//	            double similarity = compareImages(queryImage, image);
//	            System.out.println("Similarity with " + image + ": " + similarity);
//
//	            if (similarity > maxSimilarity) {
//	                maxSimilarity = similarity;
//	                bestMatch = image.toString();
//	            }
//	        }
//
//	        if (bestMatch != null) {
//	            // Extract the file name from the full path
//	            String fileName = new File(bestMatch).getName(); 
//	            // Save the best match to the output directory
//	            String outputPath = outputDirectory + File.separator + "best_match.jpg"; 
////	            Mat mat =  new Mat(bestMatch);  
////	            Imgcodecs.imwrite(outputPath, imageSet.get(imageSet.indexOf(mat))); 
//	            Imgcodecs.imwrite(outputPath, imageSet.get(imageSet.indexOf(queryImage))); 
//	            System.out.println("Best match saved to: " + outputPath); 
//	            return fileName;
//	        }
//
//	        return null;
//	    }
//	 
	 
	   
	   
//		String bestMatchPat = findBestMatchAndSave2(queryImage, imageSet, directoryPath);
//		System.out.println("Best match Path : " + bestMatchPat); 
	   
//	  public static String findBestMatchAndSave2(Mat queryImage, List<Mat> imageSet, String outputDirectory) {
//	        String bestMatchFileName = null;
//	        double maxSimilarity = -1.0;
//	        int bestMatchIndex = -1;
//
//	        for (int i = 0; i < imageSet.size(); i++) {
//	            Mat image = imageSet.get(i);
//	            double similarity = compareImages(queryImage, image);
//	            System.out.println("Similarity with " + image + ": " + similarity);
//
//	            if (similarity > maxSimilarity) {
//	                maxSimilarity = similarity;
//	                bestMatchIndex = i;
//	            }
//	        }
//
//	        if (bestMatchIndex != -1) {
//	            // Extract the file name from the full path
//	            bestMatchFileName = new File(imageSet.get(bestMatchIndex).toString()).getName();
//
//	            // Save the best match to the output directory
//	            String outputPath = outputDirectory + File.separator + "best_match.jpg";
//	            Imgcodecs.imwrite(outputPath, imageSet.get(bestMatchIndex));
//	            System.out.println("Best match saved to: " + outputPath);
//	        }
//
//	        return bestMatchFileName;
//	    }
}
