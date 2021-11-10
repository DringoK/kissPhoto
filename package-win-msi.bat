@echo off
echo msi
jpackage --input out/artifacts --name kissPhoto --app-version 0.21.1110 ^
 --copyright "(c)2021 Dr. Ingo Kreuz" ^
 --vendor "Ingo Kreuz" ^
 --description "KissPhoto: file renaming, renumbering, photo and video-clip sorting - but keep it simple stupid" ^
 --main-jar kissPhoto.jar --java-options "-splash:resources/images/KissPhotoSplash.jpg" ^
 --win-shortcut --win-menu ^
 --win-menu-group "kissPhoto" --icon resources/images/KissPhotoIconLarge.ico ^
 --file-associations Associations/avi.aso ^
 --file-associations Associations/bmp.aso ^
 --file-associations Associations/gif.aso ^
 --file-associations Associations/jpeg.aso ^
 --file-associations Associations/jpg.aso ^
 --file-associations Associations/mov.aso ^
 --file-associations Associations/mp4.aso ^
 --file-associations Associations/mpeg.aso ^
 --file-associations Associations/mpg.aso ^
 --file-associations Associations/png.aso ^
 --file-associations Associations/qt.aso ^
 --type msi ^
 --verbose
echo msi generated
echo moving result to KissPhoto base directory
move *.msi ..
xcopy out\artifacts\*.jar ..  /Y
