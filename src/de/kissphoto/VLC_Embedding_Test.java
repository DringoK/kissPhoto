package de.kissphoto;

/**
 * Application that integrates VLC player into Java FX Application
 * found on
 * https://community.oracle.com/thread/2436712
 * <p>
 * Source on GitHub
 * https://github.com/caprica/vlcj.git
 * <p>
 * Documentation
 * http://caprica.github.io/vlcj/javadoc/2.2.0/index.html?uk/co/caprica/vlcj/component/DirectMediaPlayerComponent.html
 * <p>
 * /
 * public class VLC_Embedding_Test extends Application{
 * private static final int WIDTH = 1920;
 * private static final int HEIGHT = 1080;
 * <p>
 * public static void main(final String[] args) {
 * Application.launch(args);
 * }
 * <p>
 * private DirectMediaPlayerComponent mp;
 *
 * @Override public void start(Stage primaryStage) throws Exception {
 * NativeLibrary.addSearchPath("libvlc", "c:/program files (x86)/videolan/vlc");
 * <p>
 * BorderPane borderPane = new BorderPane();
 * final Canvas canvas = new Canvas(WIDTH, HEIGHT);
 * borderPane.setCenter(canvas);
 * System.out.println(">>> " + canvas.getGraphicsContext2D().getPixelWriter().getPixelFormat());
 * Scene scene = new Scene(borderPane);
 * final PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
 * final WritablePixelFormat<ByteBuffer> byteBgraInstance = PixelFormat.getByteBgraPreInstance();
 * <p>
 * mp = new DirectMediaPlayerComponent("RV32", WIDTH, HEIGHT, WIDTH*4) {
 * private long totalTime;
 * private long totalFrames;
 * private long tooLateFrames;
 * @Override public void display(Memory nativeBuffer) {
 * long startTime = System.currentTimeMillis();
 * ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
 * pixelWriter.setPixels(0, 0, WIDTH, HEIGHT, byteBgraInstance, byteBuffer, WIDTH*4);
 * long renderTime = System.currentTimeMillis() - startTime;
 * totalTime += renderTime;
 * totalFrames++;
 * if(renderTime > 20) {
 * tooLateFrames++;
 * }
 * <p>
 * System.out.printf("Frames: %4d   Avg.time: %4.1f ms   Frames>20ms: %d   (Max)FPS: %3.0f fps\n", totalFrames, (double)totalTime / totalFrames, tooLateFrames, 1000.0 / ((double)totalTime / totalFrames));
 * if(totalFrames > 1500) {
 * System.exit(0);
 * }
 * }
 * };
 * <p>
 * mp.getMediaPlayer().playMedia("L:\\Movies\\HD\\2012 [2009, Action Adventure Drama SF Thriller, 1080p].mkv");
 * <p>
 * primaryStage.setScene(scene);
 * primaryStage.show();
 * }
 * }
 */