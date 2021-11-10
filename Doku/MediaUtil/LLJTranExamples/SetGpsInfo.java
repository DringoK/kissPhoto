import java.io.*;

import java.util.*;
import java.text.*;
//import java.awt.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import javax.imageio.*;
import javax.imageio.stream.*;

import mediautil.gen.*;
import mediautil.gen.directio.*;
import mediautil.image.*;
import mediautil.image.jpeg.*;


public class SetGpsInfo {

    /* Changes Latitude to N 45 deg 35 min 25 sec, Longitude to
     * E 87 deg 40 min 30 sec and Altitute to 100m below sea level. Creates tags
     * if absent. Usage java SetGpsInfo <infile> <outfile> */
    public static void main(String[] args) throws Exception {

        InputStream fip = new BufferedInputStream(new FileInputStream(args[0])); // No need to buffer
        LLJTran llj = new LLJTran(fip);
        try {
            llj.read(LLJTran.READ_INFO, true);
        } catch (LLJTranException e) {
            e.printStackTrace();
        }

        AbstractImageInfo imageInfo = llj.getImageInfo();

        if(! (imageInfo instanceof Exif))
        {
            System.out.println("Sorry Image Does not have Exif. Exitting." + imageInfo);
            System.exit(1);
        }

        Exif exif = (Exif) imageInfo;
        IFD mainIfd = exif.getIFDs()[0];
        IFD gpsIfd = mainIfd.getIFD(Exif.GPSINFO);

        if(gpsIfd == null)
        {
            System.out.println("Gps IFD not found adding..");
            gpsIfd = new IFD(Exif.GPSINFO, Exif.LONG);
            mainIfd.addIFD(gpsIfd);
        }

        /* Set some values directly to gps IFD */
        Entry e;

        // Set Latitude
        e = new Entry(Exif.ASCII);
        e.setValue(0, 'N');
        gpsIfd.setEntry(new Integer(Exif.GPSLatitudeRef), 0, e);
        e = new Entry(Exif.RATIONAL);
        e.setValue(0, new Rational(45, 1));
        e.setValue(1, new Rational(35, 1));
        e.setValue(2, new Rational(25, 1));
        gpsIfd.setEntry(new Integer(Exif.GPSLatitude), 0, e);
        e = new Entry(Exif.BYTE);

        // Set Longitude
        e = new Entry(Exif.ASCII);
        e.setValue(0, 'E');
        gpsIfd.setEntry(new Integer(Exif.GPSLongitudeRef), 0, e);
        e = new Entry(Exif.RATIONAL);
        e.setValue(0, new Rational(87, 1));
        e.setValue(1, new Rational(40, 1));
        e.setValue(2, new Rational(30, 1));
        gpsIfd.setEntry(new Integer(Exif.GPSLongitude), 0, e);
        e = new Entry(Exif.BYTE);

        e.setValue(0, new Integer(1)); // This picture is taken underwater :-)
                                       // Use 0 if it is taken above sea
                                       // level
        gpsIfd.setEntry(new Integer(Exif.GPSAltitudeRef), 0, e);
        e = new Entry(Exif.RATIONAL);
        e.setValue(0, new Rational(100, 1));
        gpsIfd.setEntry(new Integer(Exif.GPSAltitude), 0, e);

        llj.refreshAppx(); // Recreate Marker Data for changes done

        OutputStream out = new BufferedOutputStream(new FileOutputStream(args[1]));

        // Transfer remaining of image to output with new header.
        llj.xferInfo(null, out, LLJTran.REPLACE, LLJTran.REPLACE);

        fip.close();
        out.close();

        llj.freeMemory();
    }
}
