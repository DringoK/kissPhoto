module de.kissphoto {
  exports de.kissphoto;
  opens de.kissphoto.model to javafx.base;   //for FileTable uses reflection on model/MediaFile

  requires javafx.controls;
  requires java.management;
  requires java.desktop;
  requires javafx.media;
  requires com.drew.metadata;

}