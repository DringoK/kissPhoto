#!/bin/bash
echo deb
rm ./*.deb
jpackage \
  --type deb \
  --input out/artifacts \
  --main-jar kissPhoto.jar \
  --app-version 00.22.901 \
  --name kissPhoto \
  --copyright '(c)2021 Dr. Ingo Kreuz' \
  --vendor 'Ingo Kreuz' \
  --description 'KissPhoto: file renaming, renumbering, photo and video-clip sorting - but keep it simple stupid' \
  --java-options '-splash:resources/images/KissPhotoSplash.jpg' \
  --linux-menu-group 'AudioVideo' \
  --icon 'resources/images/KissPhotoIconLarge.png' \
  --verbose \
  --file-associations Associations/avi.aso  --file-associations Associations/bmp.aso  --file-associations Associations/gif.aso \
  --file-associations Associations/jpeg.aso --file-associations Associations/jpg.aso  --file-associations Associations/mov.aso \
  --file-associations Associations/mp4.aso  --file-associations Associations/mpeg.aso --file-associations Associations/mpg.aso \
  --file-associations Associations/png.aso  --file-associations Associations/qt.aso
echo debian-installer generated
echo moving result to KissPhoto base directory
cp ./*.deb ..
cp out/artifacts/*.jar ..