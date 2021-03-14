![kissPhotoLogo](resources/images/KissPhotoIconLarge.png)

#kissPhoto Features
MIT License, Copyright (c)2021 kissPhoto

file renaming, renumbering, photo and video-clip sorting - but keep it simple stupid
* Rename files like in a wordprocesser's table: move around with the cursor, search and replace + mass rename, renumbering and sorting
* photo management and photo show without any additional files or database
* self containing: all information is in the picture files, filenames or directory names. So your editing will be effective in all platforms and programs also without kissPhoto
* auto numbering: file order can be changed while file numbering is maintained
* file date maintaining
* mass renaming for filenames, EXIF-Info and time-stamps
* rotate jpges lossless
* The viewer treats movie clips like moving photos (like in Harry Potter's newspapers ;-)
* zoom in/out pictures and videoclips
  

* runnable on all PC platforms which support JavaFX
* optional seamless vlc support: play virtually all playable files if vlc is installed on the system additionally


# User Manual

[click here to open User Manual](docs/manual.md)

# Setting up Developer Environment: Libraries used in kissPhoto

kissPhoto was programmed in Java with JavaFX and IntelliJ.
  
I am using Bell-Soft's "Full JDK" version of Open-JDK which already includes Open-JFX (JavaFX):

https://bell-sw.com/

No maven and no gradle is necessary because dependencies are declared directly in IntelliJ (File-Project Structure).
The .iml file with these settings has been checked in into Git.
The libraries which I have used are supposed to be located just beside the kissPhoto folder, so that the settings in Project Structure will find them.

The advantage from not using maven/gradle is to having the control over what exactly is loaded from internet and what not and when.
This makes it more transparent smaller (the maven cache does not grow and grow) and a lot faster

I am aware that not using maven/gradle has the disadvantage that all libraries have to be checked out manually from git.
Therefore I describe in the following what needs to be imported:

For each module:
* Import from Github (push green Code-Button in Github. For this follow the link under the headline of the module, see below)      
* In IntelliJ: Git-Clone...: paste location from <green button> above.
* Store in Directory beside kissPhoto
  * it will be opened as a new IntelliJ-Project
  * close the project

If all modules are loaded to the harddisk
* load kissPhoto again
* Remove from version control for kissPhoto via File-Settings...-VersionControl


## Adobe XMP-Core
https://github.com/drewnoakes/adobe-xmp-core

Check in ProjectStructure
* Module should have been imported
* no dependencies should be listed

**Used from MetaData-Extractor**

## MetaData-Extractor

https://github.com/drewnoakes/metadata-extractor

Check in ProjectStructure
* Module should have been imported
* dependency on Module adobe-xmp-core should be listed

**Used for reading and interpreting a pretty complete set of EXIF-attributes**

## JNA
https://github.com/java-native-access/jna

Java Native Access

Check in ProjectStructure
* Library "jna" with classes should have been added
  * jna\dist\jna-platform.jar
  * jna\dist\jna.jar

**Used from VLCJ-Natives and VLCJ**

## Caprica VLCJ-Natives

https://github.com/caprica/vlcj-natives

Check in ProjectStructure
* Module should have been imported
* dependency on Library JNA should be listed

**Used from VLCJ**

## Caprica VLCJ

https://github.com/caprica/vlcj

Check in ProjectStructure
* Module should have been imported
* dependencies should be listed
  * on Module VLCJ-natives
  * on Library JNA

**Used from VLCJ-javafx and from kissPhoto as the adapter to libvlc.dll of Video-LAN's vlc player**

## Caprica VLCJ-javafx

https://github.com/caprica/vlcj-javafx

Check in ProjectStructure
* Module should have been imported
* dependency to Module VLCJ should be listed

**Used from kissPhoto for copying pixels from vlc to an JavaFX ImageView**

(if VLC is installed it is used for Playback, if not then JavaFX is used for Playback, with very limited codec support)

## Caprica VLCJ-File-Filters

https://github.com/caprica/vlcj-file-filters

Check in ProjectStructure
* Module should have been imported
* no dependencies should be listed

**Used from kissPhoto for compatibility test of a media file by it's file extension**

## MediaUtil

http://mediachest.sourceforge.net/mediautil

Passive Project  
To include the fixes it has been included in kissPhoto\src and the Fixes from sourceforge.net/mediautil have bee "copied over"

**Used for rotating jpgs + simple writing into exif fields**

## kissPhoto
Check in ProjectStructure
* Module should have been imported
* dependencies should be listed
  * module metadata-extractor
  * library jna
  * module vlcj
  * module vlcj-javafx
  * module vlcj-file-filters

