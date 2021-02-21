package dringo.kissPhoto;

// Now we call the printf function...
public class JnaTest {
  public static void main(String args[]) {
    CRuntimeLibrary.INSTANCE.printf("Hello World from JNA !");
  }
}