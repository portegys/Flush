# Flush
Flush images away. Prototype demo for the GraffiTV project (see GraffiTV.pdf).
<p>
Build:<br>
<pre>
build.sh/build.bat
</pre>
<p>
Run flush as app:&lt;br&gt;
<pre>
    java -jar FlushPlayer.jar
        [-loadCassette &lt;file name&gt; (cassette to load)]
        [-cassetteChoices &lt;comma-separated list of cassette names&gt;]
</pre>
<br>
Run flush as applet:
<pre>
appletviewer flush.html # applet parameters: "load" and "choice"
</pre>
<p>
Record flush cassette:&lt;br&gt;
<pre>
mkcassette.sh
</pre>
<br>
<pre>
    java -jar FlushRecorder.jar
        -image &lt;image file|URL&gt;
        [-file &lt;cassette file&gt; (else cassette written to stdout)]
        [-title &lt;cassette title&gt;]
        [-sound &lt;sound file|URL&gt;]
        [-scale_image (scale to cassette size)]
        [-size &lt;cassette ("toilet") size (pixels)&gt;]
        [-hole_size &lt;"toilet hole" size (pixels)&gt;]
        [-fragment_size &lt;image fragment size (pixels)&gt;]
        [-rotation_increment &lt;rotation increment (degrees)&gt;]
        [-flush_delay &lt;image sequence delay while flushing (ms)&gt;]
        [-recharge_delay &lt;image sequence delay while recharging (ms)&gt;]
        [-num_spiral &lt;number of spiraling "swirls"&gt;]
        [-min_spiral_scale &lt;minimum spiral scale (0.00:1.00, .01 increments)&gt;]
        [-max_spiral_scale &lt;maximum spiral scale (0.00:1.00, .01 increments)&gt;]
        [-spiral_converge &lt;rate at which fragments converge on spirals (0.0:1.0)&gt;]
        [-random_seed &lt;random number seed&gt;]
        [-display (animation)]
</pre>
<br>
Note: re-build after recording a cassette.&lt;br&gt;
