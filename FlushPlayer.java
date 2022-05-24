/*
 *
 * FlushPlayer.java applet: Play an animation of an image flushing down a toilet.
 *
 * Input: A FlushCassette file created by a FlushRecorder program.
 *
 * Usage:
 *
 * <applet code="FlushPlayer.class" width=w height=h>
 * [<param name=load value="<FlushCassette file|URL>">]
 * [<param name=choice<n> value="<FlushCassette file|URL>"> (repeat)]
 * </applet>
 *
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

// Main applet.
public class FlushPlayer extends Applet
{
   // Applet information.
   public String getAppletInfo()
   {
      return("FlushPlayer - Version 1.0, April 2001 (portegys@corecomm.net)");
   }


   // Globals.
   AppletContext             context;
   private FlushViewer       viewer;
   private FlushLoader       loader;
   private FlushControls     controls;
   private FlushCassette     cassette;
   private Panel             masterPanel;
   private String            loadFilename;
   private ArrayList<String> choiceList;

   // Initialize.
   public void init()
   {
      Dimension d;

      // Get environment.
      try
      {
         context = getAppletContext();
      }
      catch (Exception e)
      {
         context = null;
      }

      // Load blank cassette.
      cassette = new FlushCassette();

      // Add components.
      d      = getSize();
      d      = new Dimension(d.width, (int)((double)d.height * .60));
      viewer = new FlushViewer(d);
      add(viewer);
      masterPanel = new Panel();
      masterPanel.setLayout(new GridLayout(4, 1));
      loader   = new FlushLoader();
      controls = new FlushControls();
      add(masterPanel);
   }


   // Start.
   public void start()
   {
      viewer.start();
   }


   // Stop.
   public void stop()
   {
      loader.stop();
      viewer.stop();
   }


   // Flush viewer.
   class FlushViewer extends Canvas implements Runnable
   {
      private Dimension size;
      private Thread    thread;

      // State.
      private final int IDLE     = 0;
      private final int FLUSHING = 1;
      private int       state    = IDLE;

      private final int DEFAULT_TIMER = 500;

      // Constructor.
      public FlushViewer(Dimension d)
      {
         size = d;
         setBounds(0, 0, size.width, size.height);
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


      // Flush.
      public void flush()
      {
         if (cassette.first() != null)
         {
            state = FLUSHING;
            cassette.play();
         }
      }


      // Paint.
      public void paint(Graphics g)
      {
         FlushCassette.Frame f;

         if ((f = cassette.current()) != null)
         {
            g.drawImage(f.image, 0, 0, this);
         }
         else
         {
            g.setColor(Color.white);
            g.fillRect(0, 0, size.width, size.height);
         }
      }


      // Run.
      public void run()
      {
         Thread   me;
         Graphics g;

         if ((me = Thread.currentThread()) != thread) { return; }

         // Action loop.
         while (thread == me)
         {
            if ((g = getGraphics()) != null)
            {
               paint(g);
            }

            try
            {
               if (state == IDLE)
               {
                  Thread.sleep(DEFAULT_TIMER);
               }
               else
               {
                  Thread.sleep(cassette.current().show);

                  // Get next animation frame.
                  if (cassette.next() == null)
                  {
                     cassette.first();
                     cassette.stop();
                     state = IDLE;
                  }
               }
            }
            catch (InterruptedException e) { break; }
         }
         thread = null;
      }
   }      // End FlushViewer class.

   // Flush loader.
   class FlushLoader implements Runnable
   {
      private Label  title;
      private String file;
      private Thread thread;

      // Constructor.
      public FlushLoader()
      {
         title = new Label();
         title.setAlignment(Label.CENTER);
         title.setText(cassette.title());
         masterPanel.add(title);
      }


      // Start.
      public void start(String cassetteFile)
      {
         // Stop ongoing load.
         stop();

         // Create new thread
         thread = new Thread(this);
         thread.setPriority(Thread.MIN_PRIORITY);

         // Start loading.
         file = cassetteFile;
         thread.start();
      }


      // Stop.
      public synchronized void stop()
      {
         if (thread != null)
         {
            if (thread.isAlive()) { thread.stop(); }
            thread = null;
         }
      }


      // Load cassette.
      public void run()
      {
         FlushCassette c;

         if (Thread.currentThread() != thread) { return; }

         // "Eject" cassette.
         c        = new FlushCassette();
         cassette = c;
         title.setText(cassette.title());

         if (file == null)
         {
            stop();
            return;
         }
         // Load cassette.
         title.setText("Loading...");
         try
         {
            c = new FlushCassette(file, context);
         }
         catch (IOException e) {
            title.setText("Load failed: " + e.getMessage());
            stop();
         }

         // Replace playing cassette.
         cassette = c;
         title.setText(cassette.title());

         // Stop load thread.
         stop();
      }
   }

   // Flush controls.
   class FlushControls
   {
      private           Panel[] panels;
      private TextField text;
      private Choice    choice;
      private Button    button;

      // Constructor.
      public FlushControls()
      {
         int    i;
         String s;

         panels    = new Panel[3];
         panels[0] = new Panel();
         choice    = new Choice();
         choice.addItemListener(new choiceItemListener());
         choice.add("Load:");
         if (choiceList == null)
         {
            try
            {
               for (i = 0; ; i++)
               {
                  if ((s = getParameter("choice" + i)) == null) { break; }
                  choice.add(s);
               }
            }
            catch (Exception e) {}
         }
         else
         {
            for (String c : choiceList)
            {
               choice.add(c);
            }
         }
         panels[0].add(choice);
         panels[1] = new Panel();
         if (loadFilename == null)
         {
            try
            {
               loadFilename = getParameter("load");
            }
            catch (Exception e)
            {
               loadFilename = null;
            }
         }
         text = new TextField(loadFilename, 20);
         text.addActionListener(new textActionListener());
         panels[1].add(text);
         panels[2] = new Panel();
         button    = new Button("Flush");
         button.addActionListener(new buttonActionListener());
         panels[2].add(button);
         for (i = 0; i < 3; i++) { masterPanel.add(panels[i]); }

         // Load initial cassette.
         loader.start(loadFilename);
      }


      // Choice listener.
      class choiceItemListener implements ItemListener
      {
         public void itemStateChanged(ItemEvent evt)
         {
            String s;

            s = choice.getItem(choice.getSelectedIndex());
            if (!s.equals("Load:"))
            {
               choice.select(0);
               text.setText(s);

               // Load new cassette.
               loader.start(s);
            }
         }
      }

      // Text listener.
      class textActionListener implements ActionListener
      {
         public void actionPerformed(ActionEvent evt)
         {
            // Load new cassette.
            loader.start(text.getText());
         }
      }

      // Button listener.
      class buttonActionListener implements ActionListener
      {
         public void actionPerformed(ActionEvent evt)
         {
            viewer.flush();
         }
      }
   }

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java graffitv.FlushPlayer\n" +
      "        [-loadCassette <file name> (cassette to load)]\n" +
      "        [-cassetteChoices <comma-separated list of cassette names>]";

   // Main.
   @SuppressWarnings("deprecation")
   public static void main(String[] args)
   {
      // Create player.
      FlushPlayer player = new FlushPlayer();

      // Get options.
      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-loadCassette"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid loadCassette option");
               System.err.println(Usage);
               System.exit(1);
            }
            player.loadFilename = args[i];
            continue;
         }
         if (args[i].equals("-cassetteChoices"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid cassetteChoices option");
               System.err.println(Usage);
               System.exit(1);
            }
            player.choiceList = new ArrayList<String>();
            String[] choices  = args[i].split(",");
            for (int j = 0; j < choices.length; j++)
            {
               player.choiceList.add(choices[j]);
            }
            continue;
         }
         if (args[i].equals("-help"))
         {
            System.out.println(Usage);
            System.exit(0);
         }
         System.err.println("Invalid option: " + args[i]);
         System.err.println(Usage);
         System.exit(1);
      }

      // Create frame.
      JFrame frame = new JFrame();

      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setTitle("Flush it!");
      frame.setBounds(0, 0, 300, 499);
      frame.setLayout(new GridLayout(1, 1));
      frame.add(player);
      frame.setVisible(true);

      // Run applet.
      player.init();
      player.start();
      frame.resize(new Dimension(300, 500));
   }
}
