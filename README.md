# FaceDetector
Face recognition Engine &amp;  Reverse Image Search
Choix des dossiers de personnes en se basant sur l'identification à partir d'une photographie de la personne concernée.

Searching for photos in a directory can be done in two different ways. With ImageSearch, the search is based on the general resemblance of the photo, even if it does not contain a face. On the other hand, with FaceRecognitionPhoto, the presence of a face in the photo is essential for recognition to be carried out.

The library currently used for facial recognition is openCV, with the current version being opencv-490.jar & opencv_java490.dll.



La recherche de photos dans un répertoire peut se faire de deux manières différentes. Avec ImageSearch, la recherche se base sur la ressemblance générale de la photo, même si elle ne contient pas de visage. En revanche, avec FaceRecognitionPhoto, la présence d'un visage dans la photo est indispensable pour que la reconnaissance soit effectuée.

La bibliothèque actuellement utilisée pour la reconnaissance faciale est openCV, avec la version en cours étant la opencv-490.jar & opencv_java490.dll.

# Détection faciale - Fonctionnement de l'application

Lors du démarrage de l'application, celle-ci effectue les actions suivantes :

## 1. Lecture de l'image source
- L'application lit l'image située dans le fichier :  
  `files/photos/search/photoSRC.png`.

## 2. Recherche de correspondance
- L'image source est analysée pour identifier la photo la plus similaire parmi les fichiers présents dans le répertoire :  
  `files/photos/aquired`.
- Une fois une correspondance trouvée, la photo correspondante est affichée sur l'interface utilisateur.

## 3. Capture via la caméra
- Si une caméra est connectée et en fonctionnement :
  - Une photo est acquise en temps réel.
  - Cette photo est enregistrée sous le nom :  
    `files/photos/search/photoSRC.png`.
  - Elle devient l'image source pour une nouvelle recherche dans le répertoire des photos.

## 4. Enregistrement initial d'une photo
- Lors de l'enregistrement initial de la photo d’une personne :
  - La photo est sauvegardée avec un nom composé d’un préfixe `photo_` suivi d’un suffixe représentant l’identifiant unique (ID) de la personne dans la base de données.
