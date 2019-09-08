/*
 *
 * FlushRecorder.java application: Record an animation of an image flushing down a toilet.
 *
 * Output: A FlushCassette file for loading into a FlushPlayer applet.
 *
 * Usage:
 *
 * java FlushRecorder
 *      -image <image file|URL>
 *      [-file <cassette file> (else cassette written to stdout)]
 *      [-title <cassette title>]
 *      [-sound <sound file|URL>]
 *      [-scale_image (scale to cassette size)]
 *      [-size <cassette ("toilet") size (pixels)>]
 *      [-hole_size <"toilet hole" size (pixels)>]
 *      [-fragment_size <image fragment size (pixels)>]
 *      [-rotation_increment <rotation increment (degrees)>]
 *      [-flush_delay <image sequence delay while flushing (ms)>]
 *      [-recharge_delay <image sequence delay while recharging (ms)>]
 *      [-num_spiral <number of spiraling "swirls">]
 *      [-min_spiral_scale <minimum spiral scale (0.00:1.00, .01 increments)>]
 *      [-max_spiral_scale <maximum spiral scale (0.00:1.00, .01 increments)>]
 *      [-spiral_converge <rate at which fragments converge on spirals (0.0:1.0)>]
 *      [-random_seed <random number seed>]
 *      [-display (animation)]
 */

package graffitv;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

// Record FlushCassette file containing animation of flushed image.
public class FlushRecorder extends Applet implements Runnable
{
   // Default parameters.
   static final int    FLUSH_TIMER         = 100;
   static final int    RECHARGE_TIMER      = 3000;
   static final double HOLE_SCALE          = .1;
   static final double FRAGMENT_SIZE_SCALE = .03;
   static final int    ROTATION_INCREMENT  = 15;
   static final int    SMOOTH_WINDOW       = 5;
   static final int    NUM_SPIRAL          = 30;
   // 0.00 to 1.00, .01 increments:
   static final double MIN_SPIRAL_SCALE = .50;
   static final double MAX_SPIRAL_SCALE = .90;
   static final double SPIRAL_CONVERGE  = .25;

   // Parameters.
   private String     imageName;
   private Image      image;
   private String     cassetteFile;
   private String     cassetteTitle;
   private String     soundFile;
   private AudioClip  sound;
   private boolean    scaleImage        = false;
   private int        size              = -1;
   private int        holeSize          = -1;
   private int        fragmentSize      = -1;
   private int        rotationIncrement = ROTATION_INCREMENT;
   private int        flushTimer        = FLUSH_TIMER;
   private int        rechargeTimer     = RECHARGE_TIMER;
   private int        numSpiral         = NUM_SPIRAL;
   private double     minSpiral         = MIN_SPIRAL_SCALE;
   private double     maxSpiral         = MAX_SPIRAL_SCALE;
   private double     spiralConverge    = SPIRAL_CONVERGE;
   private Random     random;
   public FlushCanvas canvas;

   // Image fragment.
   private class ImageFragment
   {
      Image image;
      int   size;
      Point location;
      int   spiral;
      int   index;

      public ImageFragment(Image image, int size, Point location)
      {
         this.image    = image;
         this.size     = size;
         this.location = location;
         this.spiral   = this.index = -1;
      }
   }

   // Globals.
   private FlushCassette    cassette;
   private BufferedImage    cassetteImage;
   private Graphics2D       cassetteGraphics;
   private Ellipse2D.Double cassetteClip, fragmentClip;
   private Vector           fragments;
   private AffineTransform  xform;
   private Thread           thread;
   private int              radius;
   private int              holeRadius;
   private int              fragmentRadius;
   private int              fragmentStagger;
   private double           scales[];
   private Vector           spirals[];

   // Get arguments and set parameters.
   public void getargs(String args[])
   {
      int    i;
      String s;

      // Set parameters from arguments.
      for (i = 0; i < args.length; i++)
      {
         s = args[i];

         if (s.equals("-?") || s.equals("-h"))
         {
            usage();
            System.exit(0);
         }

         if (s.equals("-image") && (i < args.length - 1))
         {
            i++;
            imageName = args[i];
         }
         else if (s.equals("-file") && (i < args.length - 1))
         {
            i++;
            cassetteFile = args[i];
         }
         else if (s.equals("-title") && (i < args.length - 1))
         {
            i++;
            cassetteTitle = args[i];
         }
         else if (s.equals("-sound") && (i < args.length - 1))
         {
            i++;
            soundFile = args[i];
         }
         else if (s.equals("-scale_image"))
         {
            scaleImage = true;
         }
         else if (s.equals("-size") && (i < args.length - 1))
         {
            i++;
            try
            {
               size = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid size");
               System.exit(1);
            }
            if (size <= 0)
            {
               System.err.println("Invalid size");
               System.exit(1);
            }
         }
         else if (s.equals("-hole_size") && (i < args.length - 1))
         {
            i++;
            try
            {
               holeSize = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid hole_size");
               System.exit(1);
            }
            if (holeSize <= 0)
            {
               System.err.println("Invalid hole_size");
               System.exit(1);
            }
         }
         else if (s.equals("-fragment_size") && (i < args.length - 1))
         {
            i++;
            try
            {
               fragmentSize = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid fragment_size");
               System.exit(1);
            }
            if (fragmentSize <= 0)
            {
               System.err.println("Invalid fragment_size");
               System.exit(1);
            }
         }
         else if (s.equals("-rotation_increment") && (i < args.length - 1))
         {
            i++;
            try
            {
               rotationIncrement = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid rotation_increment");
               System.exit(1);
            }
            if ((rotationIncrement < 0) || (rotationIncrement > 360))
            {
               System.err.println("Invalid rotation_increment");
               System.exit(1);
            }
         }
         else if (s.equals("-flush_delay") && (i < args.length - 1))
         {
            i++;
            try
            {
               flushTimer = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid flush_delay");
               System.exit(1);
            }
            if (flushTimer < 0)
            {
               System.err.println("Invalid flush_delay");
               System.exit(1);
            }
         }
         else if (s.equals("-recharge_delay") && (i < args.length - 1))
         {
            i++;
            try
            {
               rechargeTimer = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid recharge_delay");
               System.exit(1);
            }
            if (rechargeTimer < 0)
            {
               System.err.println("Invalid recharge_delay");
               System.exit(1);
            }
         }
         else if (s.equals("-num_spiral") && (i < args.length - 1))
         {
            i++;
            try
            {
               numSpiral = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid num_spiral");
               System.exit(1);
            }
            if (numSpiral < 1)
            {
               System.err.println("Invalid num_spiral");
               System.exit(1);
            }
         }
         else if (s.equals("-min_spiral") && (i < args.length - 1))
         {
            i++;
            try
            {
               minSpiral = Double.parseDouble(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid min_spiral");
               System.exit(1);
            }
            if ((minSpiral < 0.0) || (minSpiral > 1.0))
            {
               System.err.println("Invalid min_spiral");
               System.exit(1);
            }
            if (((int)(minSpiral * 1000.0) % 10) != 0)
            {
               System.err.println("Invalid min_spiral");
               System.exit(1);
            }
         }
         else if (s.equals("-max_spiral") && (i < args.length - 1))
         {
            i++;
            try
            {
               maxSpiral = Double.parseDouble(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid max_spiral");
               System.exit(1);
            }
            if ((maxSpiral < 0.0) || (maxSpiral > .99))
            {
               System.err.println("Invalid max_spiral");
               System.exit(1);
            }
            if (((int)(maxSpiral * 1000.0) % 10) != 0)
            {
               System.err.println("Invalid max_spiral");
               System.exit(1);
            }
         }
         else if (s.equals("-spiral_converge") && (i < args.length - 1))
         {
            i++;
            try
            {
               spiralConverge = Double.parseDouble(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid spiral_converge");
               System.exit(1);
            }
            if ((spiralConverge < 0.0) || (spiralConverge > 1.0))
            {
               System.err.println("Invalid spiral_converge");
               System.exit(1);
            }
         }
         else if (s.equals("-random_seed") && (i < args.length - 1))
         {
            i++;
            try
            {
               random = new Random(Integer.parseInt(args[i]));
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid random_seed");
               System.exit(1);
            }
         }
         else if (s.equals("-display"))
         {
            canvas = new FlushCanvas();
         }
         else
         {
            usage();
            System.exit(1);
         }
      }

      // Check for missing or inconsistent settings.
      if (imageName == null)
      {
         usage();
         System.exit(1);
      }
      if (minSpiral > maxSpiral)
      {
         System.err.println("min_spiral must be <= max_spiral");
         System.exit(1);
      }
   }


   // Initialize.
   public void init()
   {
      URL          url;
      Toolkit      toolkit;
      MediaTracker tracker;
      PixelGrabber grabber;

      int[] pixels;
      MemoryImageSource memory;
      int               i, j, k, s, x, y, cx, cy;
      double            a, d, d2;
      Point2D.Double    p1, p2;
      Point             p;
      ImageFragment     f;

      // Get image to be flushed.
      toolkit = Toolkit.getDefaultToolkit();
      try
      {
         url   = new URL(imageName);
         image = toolkit.createImage(url);
      }
      catch (MalformedURLException e) {
         image = toolkit.createImage(imageName);
      }
      if (image == null)
      {
         System.err.println("Cannot create image " + imageName);
         System.exit(1);
      }
      tracker = new MediaTracker(this);
      tracker.addImage(image, 0);
      try
      {
         tracker.waitForAll();
      }
      catch (InterruptedException e) {
         System.err.println("Image loading interrupted");
         System.exit(1);
      }
      if ((image.getWidth(this) <= 0) || (image.getHeight(this) <= 0))
      {
         System.err.println("Invalid image " + imageName);
         System.exit(1);
      }

      // Get flush sound.
      if (soundFile != null)
      {
         try
         {
            try { url = new URL(soundFile); }
            catch (MalformedURLException e) {
               url = new URL("file:" + soundFile);
            }
            sound = newAudioClip(url);
         }
         catch (Exception e) {
            System.err.println("Cannot load sound file " + soundFile + ": " + e.getMessage());
            System.exit(1);
         }
      }

      // Set working variables.
      if (size == -1)
      {
         size = Math.min(image.getWidth(this), image.getHeight(this));
      }
      radius = size / 2;
      if (holeSize == -1)
      {
         holeSize = (int)((double)size * HOLE_SCALE);
      }
      else if ((holeSize <= 0) || (holeSize > size))
      {
         System.err.println("Invalid hole_size value");
         System.exit(1);
      }
      holeRadius = holeSize / 2;
      if (fragmentSize == -1)
      {
         fragmentSize = (int)((double)size * FRAGMENT_SIZE_SCALE);
      }
      else if ((fragmentSize <= 0) || (fragmentSize > size))
      {
         System.err.println("Invalid fragment_size value");
         System.exit(1);
      }
      fragmentRadius   = fragmentSize / 2;
      fragmentStagger  = (int)(Math.sqrt((double)(fragmentSize * fragmentSize) / 2.0));
      cassetteImage    = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
      cassetteGraphics = cassetteImage.createGraphics();
      cassetteClip     = new Ellipse2D.Double((double)((size / 2) - radius),
                                              (double)((size / 2) - radius),
                                              (double)(radius * 2), (double)(radius * 2));
      fragmentClip = new Ellipse2D.Double(0.0, 0.0, (double)fragmentSize, (double)fragmentSize);
      xform        = new AffineTransform();
      if (random == null) { random = new Random(new Date().getTime()); }

      // Create cassette.
      try
      {
         cassette = new FlushCassette(cassetteFile, cassetteTitle, soundFile, size);
      }
      catch (IOException e) {
         System.err.println("Cannot create cassette: " + e.getMessage());
         System.exit(1);
      }

      // Load cassette image.
      x = image.getWidth(this);
      y = image.getHeight(this);
      if (scaleImage)
      {
         // Center and scale to fit.
         if (x > y)
         {
            d = (double)size / (double)x;
            i = (int)((double)(y / 2) * d);
            cassetteGraphics.drawImage(image, 0, (size / 2) - i, size - 1,
                                       (size / 2) + i, 0, 0, x - 1, y - 1, Color.white, this);
         }
         else
         {
            d = (double)size / (double)y;
            i = (int)((double)(x / 2) * d);
            cassetteGraphics.drawImage(image, (size / 2) - i, 0, (size / 2) + i,
                                       size - 1, 0, 0, x - 1, y - 1, Color.white, this);
         }
      }
      else
      {
         // Center and clip to fit.
         cassetteGraphics.drawImage(image, radius - (x / 2), radius - (y / 2), x, y, Color.white, this);
      }

      // Create random spirals.
      // Fragments will follow the nearest spiral to the center.
      scales  = new double[numSpiral];
      spirals = new Vector[numSpiral];
      cx      = size / 2;
      cy      = size / 2;
      p1      = new Point2D.Double();
      p2      = new Point2D.Double();
      for (s = 0; s < numSpiral; s++)
      {
         scales[s]  = randscale();
         spirals[s] = new Vector();
         a          = Math.toRadians((double)random.nextInt(360));
         xform.setToRotation(a);
         p1.setLocation(0.0, (double)radius);
         xform.transform(p1, p2);
         x = (int)p2.getX() + cx;
         y = (int)p2.getY() + cy;
         a = Math.toRadians((double)rotationIncrement);
         while (true)
         {
            spirals[s].addElement(new Point(x, y));
            p1.setLocation((double)(x - cx), (double)(y - cy));
            xform.setToRotation(a);
            xform.scale(scales[s], scales[s]);
            xform.transform(p1, p2);
            x = (int)p2.getX() + cx;
            y = (int)p2.getY() + cy;
            if ((x < 0) || (x >= size) || (y < 0) || (y >= size)) { break; }
            d = edist((double)x, (double)y, (double)cx, (double)cy);
            if ((d <= (double)holeRadius) || (d >= (double)radius)) { break; }
         }
      }

      // Create image fragments.
      fragments = new Vector();
      pixels    = new int[size * size];
      grabber   = new PixelGrabber(cassetteImage, 0, 0, size, size, pixels, 0, size);
      try {
         grabber.grabPixels();
      }
      catch (InterruptedException e) {}
      j = size - fragmentSize;
      for (x = fragmentSize / 2; x < j; x += fragmentStagger)
      {
         for (y = fragmentSize / 2; y < j; y += fragmentStagger)
         {
            d = edist((double)x, (double)y, (double)cx, (double)cy);
            if (d >= (double)radius) { continue; }
            i      = (y * size) + x;
            memory = new MemoryImageSource(fragmentSize, fragmentSize, pixels, i, size);
            p      = new Point(x, y);
            f      = new ImageFragment(createImage(memory), fragmentSize, p);
            fragments.addElement(f);

            // Assign spiral to fragment.
            d2 = size * 2.0;
            for (s = 0; s < numSpiral; s++)
            {
               for (i = 0, k = spirals[s].size(); i < k; i++)
               {
                  p = (Point)spirals[s].elementAt(i);

                  d = edist(f.location.getX(), f.location.getY(), p.getX(), p.getY());
                  if ((f.spiral == -1) || (d < d2))
                  {
                     f.spiral = s;
                     f.index  = i;
                     d2       = d;
                  }
               }
            }
         }
      }

      // Size canvas.
      if (canvas != null) { canvas.setSize(size, size); }
   }


   // Generate random scale to simulate turbulence.
   private double randscale()
   {
      int i = (int)((maxSpiral - minSpiral) * 100.0) + 1;

      return(((double)random.nextInt(i) / 100.0) + minSpiral);
   }


   // Start.
   public void start()
   {
      if (thread == null)
      {
         thread = new Thread(this);
         thread.setPriority(Thread.MIN_PRIORITY);
         thread.start();
      }
   }


   // Stop.
   public synchronized void stop()
   {
      thread = null;
   }


   // Run.
   public void run()
   {
      Thread   me;
      Graphics g;

      if ((me = Thread.currentThread()) != thread) { return; }

      // Flush loop.
      while (thread == me)
      {
         display();

         // Add frame to cassette.
         cassette.add(cassette.newFrame((Image)cassetteImage,
                                        new Dimension(size, size), flushTimer));

         if (flush())
         {
            display();

            // Add final frame.
            cassette.add(cassette.newFrame((Image)cassetteImage,
                                           new Dimension(size, size), rechargeTimer));
            cassette.close();

            System.exit(0);
         }

         try
         {
            Thread.sleep(flushTimer);
         }
         catch (InterruptedException e) { break; }
      }
      thread = null;
   }


   // Display animation
   private void display()
   {
      Graphics g;

      if ((canvas != null) && ((g = canvas.getGraphics()) != null))
      {
         canvas.image = cassetteImage;
         canvas.paint(g);
      }
   }


   // Flush - returns true when image completely flushed.
   // Incrementally spiral swirls of image fragments into the
   // image center using a combined rotation and scale reduction.
   private boolean flush()
   {
      int           i, j, s, x, y, x2, y2, cx, cy;
      ImageFragment f;
      double        a, d;
      Point         p;

      Point2D.Double p1, p2;
      Vector         v;
      Graphics       g;
      boolean        flushed;

      // Spiral fragments.
      flushed = true;
      a       = Math.toRadians((double)rotationIncrement);
      p1      = new Point2D.Double();
      p2      = new Point2D.Double();
      cx      = size / 2;
      cy      = size / 2;
      v       = new Vector();
      for (i = 0, j = fragments.size(); i < j; i++)
      {
         f = (ImageFragment)fragments.elementAt(i);
         x = f.location.x;
         y = f.location.y;
         p1.setLocation((double)(x - cx), (double)(y - cy));
         xform.setToRotation(a);
         d = scales[f.spiral];
         xform.scale(d, d);
         xform.transform(p1, p2);
         x2 = (int)p2.getX() + cx;
         y2 = (int)p2.getY() + cy;
         f.index++;
         if (f.index < spirals[f.spiral].size())
         {
            p   = (Point)spirals[f.spiral].elementAt(f.index);
            x2 += (double)(p.x - x2) * spiralConverge;
            y2 += (double)(p.y - y2) * spiralConverge;
         }
         if ((x2 >= 0) && (x2 < size) && (y2 >= 0) && (y2 < size))
         {
            d = edist((double)x2, (double)y2, (double)cx, (double)cy);
            if ((d > (double)holeRadius) && (d < (double)radius))
            {
               f.location.x = x2;
               f.location.y = y2;
               v.addElement(f);
               flushed = false;
            }
         }
      }
      fragments = v;

      // Create new image spiral by spiral.
      cassetteGraphics.dispose();
      cassetteImage    = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
      cassetteGraphics = cassetteImage.createGraphics();
      cassetteGraphics.setClip(cassetteClip);
      cassetteGraphics.setColor(Color.white);
      cassetteGraphics.fillRect(0, 0, size, size);
      j = fragments.size();
      for (s = 0; s < numSpiral; s++)
      {
         for (i = 0; i < j; i++)
         {
            f = (ImageFragment)fragments.elementAt(i);
            if (f.spiral != s) { continue; }
            x = f.location.x;
            y = f.location.y;
            cassetteGraphics.setClip(cassetteClip);
            fragmentClip.setFrame((double)(x - (f.size / 2)),
                                  (double)(y - (f.size / 2)),
                                  (double)fragmentSize, (double)fragmentSize);
            cassetteGraphics.clip(fragmentClip);
            cassetteGraphics.drawImage(f.image, x - (f.size / 2),
                                       y - (f.size / 2), this);
         }
      }
      cassetteGraphics.setClip(cassetteClip);
      cassetteGraphics.setColor(Color.black);
      cassetteGraphics.fillOval(cx - holeRadius, cy - holeRadius, holeRadius * 2, holeRadius * 2);

      return(flushed);
   }


   // Euclidean distance
   public double edist(double x1, double y1, double x2, double y2)
   {
      double d, t;

      t  = x1 - x2;
      t *= t;
      d  = t;
      t  = y1 - y2;
      t *= t;
      d += t;
      return(Math.sqrt(d));
   }


   // Usage message.
   public static void usage()
   {
      System.err.println("java FlushRecorder");
      System.err.println("\t-image <image file|URL>");
      System.err.println("\t[-file <cassette file> (else cassette written to stdout)]");
      System.err.println("\t[-title <cassette title>]");
      System.err.println("\t[-sound <sound file|URL>]");
      System.err.println("\t[-scale_image (scale to cassette size)]");
      System.err.println("\t[-size <cassette (\"toilet\") size (pixels)>]");
      System.err.println("\t[-hole_size <\"toilet hole\" size (pixels)>]");
      System.err.println("\t[-fragment_size <image fragment size (pixels)>]");
      System.err.println("\t[-rotation_increment <rotation increment (degrees)>]");
      System.err.println("\t[-flush_delay <image sequence delay while flushing (ms)>]");
      System.err.println("\t[-recharge_delay <image sequence delay while recharging (ms)>]");
      System.err.println("\t[-num_spiral <number of spiraling \"swirls\">]");
      System.err.println("\t[-min_spiral_scale <minimum spiral scale (0.00:1.00, .01 increments)>]");
      System.err.println("\t[-max_spiral_scale <maximum spiral scale (0.00:1.00, .01 increments)>]");
      System.err.println("\t[-spiral_converge <rate at which fragments converge on spirals (0.0:1.0)>]");
      System.err.println("\t[-random_seed <random number seed>]");
      System.err.println("\t[-display (animation)]");
   }


   // Display canvas.
   class FlushCanvas extends Canvas
   {
      BufferedImage image;

      public void paint(Graphics g)
      {
         if ((image != null) && (g != null))
         {
            g.drawImage((Image)image, 0, 0, this);
         }
      }
   }

   // Main.
   public static void main(String args[])
   {
      FlushRecorder recorder = new FlushRecorder();

      recorder.getargs(args);
      recorder.init();
      if (recorder.canvas != null)
      {
         JFrame frame = new JFrame("Flush Recorder");
         frame.addWindowListener(new WindowAdapter()
                                 {
                                    public void windowClosing(WindowEvent e) { System.exit(1); }
                                 }
                                 );
         frame.getContentPane().add("Center", recorder.canvas);
         frame.pack();
         frame.setSize(new Dimension(recorder.size, recorder.size));
         frame.show();
      }
      recorder.start();
   }
}
