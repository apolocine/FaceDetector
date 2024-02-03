package org.hmd.face.emp;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.CvType;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfFloat;
import org.opencv.core.CvType;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;



//<dependencies>
//<dependency>
//    <groupId>org.openpnp</groupId>
//    <artifactId>opencv</artifactId>
//    <version>4.5.3-1</version> <!-- Assurez-vous de spécifier la version appropriée -->
//</dependency>
//</dependencies>



public class FacialRecognition {
	public static void main(String[] args) {
		// Charger la bibliothèque OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		// Initialiser la caméra
		VideoCapture capture = new VideoCapture(0);
		
//		Mat frameV = imageVideoCapture(capture );  
		
		
String inImagePath = "files/photos/aquired/photo02.png";

		Mat frameP = imageFromDisc(inImagePath);
		
		String outImagePath = "files/photos/analyzed/analyzed_photo02.jpg";
		
		 String outImagePath_=  analyseImage(  frameP,  outImagePath);
		
		 System.out.println(outImagePath);

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

	
	
	public static String  analyseImage(Mat frame,String outImagePath) {
		
		// Vous pouvez maintenant appliquer les étapes d'analyse faciale sur cette
		// image.
		
		 // Charger le classificateur Haar pour la détection de visages (vous devez télécharger ce fichier XML)
        String haarCascadePath = "files/data/haarcascades/haarcascade_frontalface_default.xml";
        CascadeClassifier faceCascade = new CascadeClassifier(haarCascadePath);

        String eyesCascadePath = "files/data/haarcascades/haarcascade_eye.xml";
        CascadeClassifier eyesCascade = new CascadeClassifier(eyesCascadePath);
        
        String smileCascadePath = "files/data/haarcascades/haarcascade_smile.xml";
        CascadeClassifier smileCascade = new CascadeClassifier(smileCascadePath);
        
        
        // Convertir l'image en niveaux de gris (la détection de visages fonctionne généralement mieux sur des images en niveaux de gris)
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

        
   /*      
        // Appliquer un seuillage pour détecter les contours (ajustez selon vos besoins)
        Imgproc.threshold(grayFrame, grayFrame, 100, 255, Imgproc.THRESH_BINARY);

        // Rechercher les contours dans l'image
        Mat hierarchy = new Mat();
        MatOfPoint contours = new MatOfPoint();
        Imgproc.findContours(grayFrame, contours.toList(), hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Dessiner les contours sur l'image originale
        Imgproc.drawContours(frame, contours.toList(), -1, new Scalar(0, 0, 255), 2);
  
           */  
        
        // Détecter les visages dans l'image
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(grayFrame, faces);

        // Dessiner des rectangles autour des visages détectés
        Rect[] facesArray = faces.toArray();
        for (Rect face : facesArray) {
            Imgproc.rectangle(frame, new Point(face.x, face.y), new Point(face.x + face.width, face.y + face.height), new Scalar(0, 255, 154), 2);
        
        
            Imgproc.rectangle(frame, new Point(face.x, face.y), new Point(face.x + face.width, face.y + face.height), new Scalar(154, 255, 0), 2);

            // Région d'intérêt (ROI) pour la détection des yeux dans la région du visage
            Mat faceROI = grayFrame.submat(face);

            // Détecter les yeux dans la région du visage
            MatOfRect eyes = new MatOfRect();
            eyesCascade.detectMultiScale(faceROI, eyes);

            // Dessiner des rectangles autour des yeux détectés
            Rect[] eyesArray = eyes.toArray();
            for (Rect eye : eyesArray) {
                Imgproc.rectangle(frame, new Point(face.x + eye.x, face.y + eye.y), new Point(face.x + eye.x + eye.width, face.y + eye.y + eye.height), new Scalar(255, 0, 0), 2);
            }

            // Calculer la distance entre les yeux
            if (eyesArray.length == 2) {
                Point leftEye = new Point(face.x + eyesArray[0].x + eyesArray[0].width / 2, face.y + eyesArray[0].y + eyesArray[0].height / 2);
                Point rightEye = new Point(face.x + eyesArray[1].x + eyesArray[1].width / 2, face.y + eyesArray[1].y + eyesArray[1].height / 2);

                double distanceBetweenEyes = Math.sqrt(Math.pow(rightEye.x - leftEye.x, 2) + Math.pow(rightEye.y - leftEye.y, 2));
                System.out.println("Distance entre les yeux : " + distanceBetweenEyes + " pixels");
            }

            // Détecter les sourires dans la région du visage
            MatOfRect smiles = new MatOfRect();
            smileCascade.detectMultiScale(faceROI, smiles);

            // Dessiner des rectangles autour des sourires détectés
            Rect[] smilesArray = smiles.toArray();
            for (Rect smile : smilesArray) {
                Imgproc.rectangle(frame, new Point(face.x + smile.x, face.y + smile.y), new Point(face.x + smile.x + smile.width, face.y + smile.y + smile.height), new Scalar(0, 0, 255), 2);
            }
            
            
            
          

            
            
            // Dessiner des rectangles autour des lèvres (vous devez ajuster le classificateur Haar pour la détection des lèvres ou utiliser une méthode différente)
            // Exemple (non spécifique) : cascadeClassifier.detectMultiScale(faceROI, lips);
            // Dessiner des rectangles autour des lèvres détectées
            // for (Rect lip : lipsArray) {
            //     Imgproc.rectangle(frame, new Point(face.x + lip.x, face.y + lip.y), new Point(face.x + lip.x + lip.width, face.y + lip.y + lip.height), new Scalar(0, 0, 255), 2);
            // }

            // Calculer la distance entre les lèvres (ajustez selon la méthode de détection des lèvres)
            // if (lipsArray.length == 2) {
            //     Point leftLip = new Point(face.x + lipsArray[0].x + lipsArray[0].width / 2, face.y + lipsArray[0].y + lipsArray[0].height / 2);
            //     Point rightLip = new Point(face.x + lipsArray[1].x + lipsArray[1].width / 2, face.y + lipsArray[1].y + lipsArray[1].height / 2);
            //
            //     double distanceBetweenLips = Math.sqrt(Math.pow(rightLip.x - leftLip.x, 2) + Math.pow(rightLip.y - leftLip.y, 2));
            //     System.out.println("Distance entre les lèvres : " + distanceBetweenLips + " pixels");
            // }
        
        }
        
        
        
		// Ajoutez le code de reconnaissance faciale et d'enregistrement dans la base de
		// données ici.

		// Par exemple, enregistrez l'image analysée sur le disque
		try {
			Imgcodecs.imwrite(outImagePath, frame);
			return outImagePath;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
}
