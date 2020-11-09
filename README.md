![kissPhotoLogo](resources/images/KissPhotoSplash.jpg)

# Installation: Used Libraries

## MediaUtil

http://mediachest.sourceforge.net/mediautil/   
Passive Project  
To include the fixes it has been included in kissPhoto\src and the Fixes from sourceforge.net/mediautil have bee "copied over"
  
**Used for rotating jpgs + simple writing into exif**

## MetaData-Extractor

https://github.com/drewnoakes/metadata-extractor  
Import from Github (push green Code-Button)  
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto  

In ProjectStructure
* add..Import Module as IntelliJ-Module to Project
* Mark Samples and Tests as "not Sources" and "not Tests" (unklick the two buttons) in ProjectStructure while Modules/meta-data-extractor is selected
* add Dependency to Module MetaData-Extractor in kissPhoto

**Used for displaying full EXIF-attributes**

## Adobe XMP-Core

https://github.com/drewnoakes/adobe-xmp-core  
Import from Github (push green Code-Button)      
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto  

In ProjectStructure
* add..Import Module as IntelliJ-Module to Project
* add Dependency to Module MetaData-Extractor in MetaDataExtractor (cancel Maven-Import at the end)

**Used from MetaData-Extractor**

## Caprica VLCJ-Project

https://github.com/caprica/vlcj
Import from Github (push green Code-Button)  
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto  

In ProjectStructure
* add..Import Module as IntelliJ-Module to Project
* add Dependency to Module VLCJ in kissPhoto

**Used from kissPhoto for using VLC as the Video Viewer if installed :-)**

## Caprica VLCJ-Natives

https://github.com/caprica/vlcj-natives.git
Import from Github (push green Code-Button)  
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto  

In ProjectStructure
* add..Import Module as IntelliJ-Module to Project
* add Dependency to Module VLCJ-Natives in VLCJ

**Used from VLCJ**

## JNA-Platform
Java Native Access

https://github.com/java-native-access/jna.git
Import from Github (push green Code-Button)  
In IntelliJ: VCS-Get from Version Control: paste location from <green button> above.  
Store in Directory beside kissPhoto  

In ProjectStructure
* add..Import Module as IntelliJ-Module to Project
* add Dependency to Module VLCJ-Natives in VLCJ

**Used from VLCJ**