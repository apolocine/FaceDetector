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
