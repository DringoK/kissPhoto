package de.kissphoto.view.statusBarHelpers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;

/**
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * Status-Bar - Showing a caption and some progress in %(esp. for saving)
 * <p/>
 *
 * @author Dr. Ingo Kreuz
 * @date: 2017-10-24
 * @modified:
 */

public class ProgressBarPanel extends BorderPane {
  private Label caption = new Label();
  private ProgressBar progressBar = new ProgressBar(0);
  final static double inset = 4;

  public ProgressBarPanel() {
    BorderPane.setAlignment(caption, Pos.CENTER_LEFT);
    BorderPane.setAlignment(progressBar, Pos.CENTER);

    caption.setPadding(new Insets(inset, inset, inset, inset));
    progressBar.setPadding(new Insets(inset, inset, inset, inset));

    setLeft(caption);
    setCenter(progressBar);
  }

  /**
   * register a listner on a Double Property
   *
   * @param progress (DoubleProperty 0..1)
   */
  public void registerProgressDoubleObservable(SimpleDoubleProperty progress) {
    progress.addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        progressBar.setProgress((double) newValue);
      }
    });
  }

  /**
   * set the progress bar value directly
   *
   * @param percent (double between 0..1)
   */
  public void setProgress(double percent) {
    progressBar.setProgress(percent);
  }

  public void setCaption(String newCaption) {
    caption.setText(newCaption);
  }

}
