/*
 *
 * FlushCassette.java
 *
 * Animation of an image flushing down a toilet with sound effect.
 *
 */

package graffitv;

import java.applet.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;
import sun.audio.*;

public class FlushCassette extends Applet
{
   // Frame: image and show time.
   public class Frame
   {
      Image     image;
      Dimension size;
      int       show;

      // Constructor.
      public Frame(Image image, Dimension size, int show)
      {
         this.image = image;
         this.size  = size;
         this.show  = show;
      }
   }

   // Globals.
   private String cassetteFile;         // Cassette file.
   private String cassetteTitle;        // Cassette title.
   private String soundFile;            // Sound file.
   private int    size;                 // XY size.
   private int[]  pixels;               // Image pixels.

   // Mode.
   final int   RECORD = 0;
   final int   PLAY   = 1;
   private int mode;

   // Play mode.
   private Vector      animation;       // Frame sequence.
   private int         cursor;          // Sequence cursor.
   private AudioStream sound;           // Sound effect.

   // Record mode.
   private ObjectOutputStream out;      // Output stream.

   // Record mode constructor.
   public FlushCassette(String cassetteFile, String cassetteTitle,
                        String soundFile, int size) throws IOException
   {
      mode = RECORD;
      this.cassetteFile  = cassetteFile;
      this.cassetteTitle = cassetteTitle;
      this.soundFile     = soundFile;
      this.size          = size;

      try
      {
         if (cassetteFile != null)
         {
            out = new ObjectOutputStream(new GZIPOutputStream(
                                            new FileOutputStream(cassetteFile)));
         }
         else
         {
            out = new ObjectOutputStream(new GZIPOutputStream(
                                            new FileOutputStream(FileDescriptor.out)));
         }
         out.writeObject(cassetteTitle);
         out.writeObject(soundFile);
         out.writeInt(size);
         out.flush();
      }
      catch (Exception e) {
         throw new IOException(e.getMessage());
      }
      pixels = new int[size * size];
   }


   // Play mode constructor.
   public FlushCassette(String cassetteFile, AppletContext context) throws IOException
   {
      ObjectInputStream in;
      MemoryImageSource memory;
      URL               url;
      MediaTracker      tracker;
      Image             image;
      int               i, j;

      mode = PLAY;
      this.cassetteFile = cassetteFile;
      animation         = new Vector();
      cursor            = 0;

      // Read "header".
      context.showStatus("Loading cassette " + cassetteFile + "...");
      try
      {
         in            = new ObjectInputStream(new GZIPInputStream(getClass().getResourceAsStream(cassetteFile)));
         cassetteTitle = (String)in.readObject();
         soundFile     = (String)in.readObject();
         size          = in.readInt();
      }
      catch (Exception e) {
         throw new IOException(e.getMessage());
      }

      // Read frames.
      context.showStatus("Loading frames...");
      tracker = new MediaTracker(this);
      j       = 0;
      while (true)
      {
         i = 0;
         try
         {
            pixels = new int[size * size];
            for (i = 0; i < pixels.length; i++) { pixels[i] = in.readInt(); }
            memory = new MemoryImageSource(size, size, pixels, 0, size);
            image  = createImage(memory);
            tracker.addImage(image, 0);
            animation.addElement(new Frame(image, new Dimension(size, size), in.readInt()));
            context.showStatus("Frame " + j + " loaded");
            j++;
         }
         catch (EOFException e) {
            if (i != 0) { throw new IOException(e.getMessage()); }
            break;
         }
         catch (IOException e) {
            throw new IOException(e.getMessage());
         }
      }
      in.close();
      try
      {
         tracker.waitForAll();
      }
      catch (InterruptedException e) {
         throw new IOException(e.getMessage());
      }

      // Load sound.
      if (soundFile != null)
      {
         context.showStatus("Loading sound...");
         try
         {
            sound = new AudioStream(getClass().getResourceAsStream(soundFile));
         }
         catch (Exception e) {
            throw new IOException(e.getMessage());
         }
         AudioPlayer.player.start(sound);                       // Force load.
         AudioPlayer.player.stop(sound);
      }
      context.showStatus("Cassette " + cassetteFile + " loaded");
   }


   // Play mode constructor to "eject" cassette.
   public FlushCassette()
   {
      mode      = PLAY;
      animation = new Vector();
      sound     = null;
      cursor    = 0;
   }


   // Get title.
   public String title()
   {
      return(cassetteTitle);
   }


   // New Frame.
   public Frame newFrame(Image image, Dimension size, int show)
   {
      return(new Frame(image, size, show));
   }


   // Add frame to animation.
   public void add(Frame frame)
   {
      PixelGrabber grabber;
      int          i;

      if (mode == PLAY) { return; }

      grabber = new PixelGrabber(frame.image, 0, 0, size, size, pixels, 0, size);
      try {
         grabber.grabPixels();
      }
      catch (InterruptedException e) { return; }

      try {
         for (i = 0; i < pixels.length; i++) { out.writeInt(pixels[i]); }
         out.writeInt(frame.show);
      }
      catch (IOException e) {}
   }


   // Close animation.
   public void close()
   {
      if (mode == PLAY) { return; }
      try {
         out.close();
      }
      catch (IOException e) {}
   }


   // Get first frame.
   public Frame first()
   {
      if (mode == RECORD) { return(null); }
      cursor = 0;
      return(current());
   }


   // Next frame.
   public Frame next()
   {
      if (mode == RECORD) { return(null); }
      cursor++;
      return(current());
   }


   // Current frame.
   public Frame current()
   {
      if (mode == RECORD) { return(null); }
      try
      {
         Frame f = (Frame)(animation.elementAt(cursor));
         return(f);
      }
      catch (ArrayIndexOutOfBoundsException e) {
         return(null);
      }
   }


   // Play sound effect.
   public void play()
   {
      if (mode == RECORD) { return; }
      if (sound != null) { AudioPlayer.player.start(sound); }
   }


   // Stop sound effect.
   public void stop()
   {
      if (mode == RECORD) { return; }
      if (sound != null)
      {
         AudioPlayer.player.stop(sound);
         try
         {
            sound = new AudioStream(getClass().getResourceAsStream(soundFile));
         }
         catch (Exception e) {
         }
      }
   }
}
