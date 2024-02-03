package org.hmd.face.emp;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetector {
    public void detectFace(String filename) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Charger l'image
        Mat image = Imgcodecs.imread(filename);

        // Instancier le classificateur de visage
        CascadeClassifier faceDetector = new CascadeClassifier();
        faceDetector.load("files/data/haarcascades/haarcascade_frontalface_alt.xml");

        // Détecter les visages
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);

        // Dessiner un rectangle autour de chaque visage
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y),
             new Point(rect.x + rect.width, rect.y + rect.height),
              new Scalar(0, 255, 0));
        }

        // Sauvegarder l'image résultante
        String outputFilename = "files/photos/analyzed/imprimante033.png";
        System.out.println(String.format("Writing %s", outputFilename));
        Imgcodecs.imwrite(outputFilename, image);
    }

    public static void main(String[] args) {
    
    	
        new FaceDetector().detectFace("files/photos/aquired/imprimante02.png");
    }
}
