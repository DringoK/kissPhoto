package dringo.kissPhoto;

import com.sun.jna.Library;
import com.sun.jna.Native;

// We declare the printf function we need and the library containing it (msvcrt)...
public interface CRuntimeLibrary extends Library {

  CRuntimeLibrary INSTANCE =
    (CRuntimeLibrary) Native.loadLibrary("msvcrt", CRuntimeLibrary.class);

  void printf(String format, Object... args);
}
