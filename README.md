# FaceDetector

## Description
**Face Recognition Engine & Reverse Image Search**  
L'application permet d'identifier et de gérer des dossiers de personnes en se basant sur l'identification à partir d'une photographie de la personne concernée.

## Modes de Recherche
### 1. ImageSearch
- Basé sur la ressemblance générale de la photo.  
- Fonctionne même si l'image ne contient pas de visage.  
- Utile pour une recherche plus large et moins spécifique.

### 2. FaceRecognitionPhoto
- Nécessite la présence d'un visage dans la photo pour effectuer une reconnaissance.  
- Utilisé pour des recherches précises et centrées sur l'identification faciale.

## Technologies Utilisées
- La bibliothèque utilisée pour la reconnaissance faciale est **OpenCV**.  
- Version actuelle :  
  - `opencv-490.jar`  
  - `opencv_java490.dll`

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
