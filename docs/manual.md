![kissPhotoLogo](../resources/images/KissPhotoIconLarge.png)

# Kissphoto Manual

## Basic Idea

keep it simple:
- just files (no database)
- parse filenames for renumbering and mass renaming
- cursor through lines in edit mode like in plain text
  
###No Data Loss
- "saving" will just rename the files
- deletion just results in moving the file into the subdirectory "deleted"
- rotation of jpgs is lossless and keeps the file date

      
## Install kissPhoto
https://github.com/DringoK/kissPhoto/releases

Start the installer
- kissPhoto.msi for Windows
- kissPhoto.deb for Debian based Linux (e.g. Ubuntu)

Start kissPhoto.
Open the menu Extra/language and select your preferred language.

## Use kissPhoto
  
### Getting Started
- open a file with menu file/open...
- or drag a file to the left section of the application (the file table)

kissPhoto will open the complete directory of the file and selects the file

Use the main menu or context menu to start a function of kissPhoto.

### Showing Media
If the currently selected file is a media file (sound, picture or video) it is shown immediately
- use +/- or ctrl-Mouse Wheel to zoom in/out
- use F5 for full screen, use tab to push the full screen to the next screen
- use context menu for further options

### Editing
Double Click an entry in the file table or press F2 to start inplace editing.

If multiple files are selected and you start editing you will get to the mass renaming dialog.

####Numbering
- use Ctrl-N for numbering all files
- find more numbering options in the edit menu  (just selected files, step size, starting with, ...)
- because kissPhoto parses the filenames renumbering does not change the other parts of the filenames :-)
                                             
###Save your changes
Your files will not be changed unless you save your changes.

This will result just in renaming the files.

Note: Rotation of JPEG files will be lossless, but here the files need to be rewritten.

