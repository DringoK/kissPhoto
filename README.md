![kissPhotoLogo](resources/images/KissPhotoSplash.jpg)

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


## Adobe XMP-Core

https://github.com/drewnoakes/adobe-xmp-core

Import from Github (push green Code-Button)      
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto

Check in ProjectStructure
* add..Import Module as IntelliJ-Module to Project

**Used from MetaData-Extractor**

## MetaData-Extractor

https://github.com/drewnoakes/metadata-extractor

Import from Github (push green Code-Button)  
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto  

Check in ProjectStructure
* add..Import Module as IntelliJ-Module to Project
* Mark Samples and Tests as "not Sources" and "not Tests" (unklick the two buttons) in ProjectStructure while Modules/meta-data-extractor is selected
* add Dependency to module MetaData-Extractor in module MetaDataExtractor (cancel Maven-Import at the end)

**Used for reading and interpreting a pretty complete set of EXIF-attributes**

## JNA

Java Native Access

https://github.com/java-native-access/jna

Import from Github (push green Code-Button)  
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto

In ProjectStructure
* add as Library "jna" with clases
  * jna\dist\jna-platform.jar
  * jna\dist\jna.jar

**Used from VLCJ-Natives**

## Caprica VLCJ-Natives

https://github.com/caprica/vlcj-natives

Import from Github (push green Code-Button)  
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto  
Rename module-info.java into module-info.java.old as kissPhoto does not use modules

Check in ProjectStructure
* add..Import Module as IntelliJ-Module to Project
* add Dependency to Library JNA in module VLCJ

**Used from VLCJ**

## Caprica VLCJ

https://github.com/caprica/vlcj

Import from Github (push green Code-Button)  
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto
Rename module-info.java into module-info.java.old as kissPhoto does not use modules

In ProjectStructure
* add..Import Module as IntelliJ-Module to Project
* add Dependency to Module VLCJ-natives in module VLCJ
* add Dependency to Library JNA in module VLCJ

**Used from VLCJ-javafx and from kissPhoto as the adapter to libvlc.dll of Video-LAN's vlc player**

## Caprica VLCJ-javafx

https://github.com/caprica/vlcj-javafx

Import from Github (push green Code-Button)  
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto
Rename module-info.java into module-info.java.old as kissPhoto does not use modules

Check in ProjectStructure
* add..Import Module as IntelliJ-Module to Project
* add Dependency to Module VLCJ in VLCJ-javafx

**Used from kissPhoto for copying pixels from vlc to an JavaFX ImageView**
(if VLC is installed it is used for Playback, if not then JavaFX is used for Playback, with very limited codec support)

## Caprica VLCJ-File-Filters

https://github.com/caprica/vlcj-file-filters

Import from Github (push green Code-Button)  
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto
Rename module-info.java into module-info.java.old as kissPhoto does not use modules

In ProjectStructure
* add..Import Module as IntelliJ-Module to Project

**Used from kissPhoto for compatibility test of a media file by it's file extension**

## MediaUtil

http://mediachest.sourceforge.net/mediautil

Passive Project  
To include the fixes it has been included in kissPhoto\src and the Fixes from sourceforge.net/mediautil have bee "copied over"

**Used for rotating jpgs + simple writing into exif fields**

## kissPhoto
No add all depencencies to the other modules
* module metadata-extractor
* library jna
* module vlcj
* module vlcj-javafx
* module vlcj-file-filters

