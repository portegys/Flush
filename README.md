# Flush
Flush images away.
<p>
<br>Build: build.sh/build.bat
<p>
<br>Run player as app: java -jar FlushPlayer.jar
<br>Usage:
<br>    java graffitv.FlushPlayer
<br>        [-loadCassette <file name> (cassette to load)]
<br>        [-cassetteChoices <comma-separated list of cassette names>]
<br>Run as applet: appletviewer flush.html (applet parameters: "load" and "choice"
<p>
<br>Run cassette recorder:
<br>mkcassette.sh
<br>or 
<br>java -jar FlushRecorder.jar
<br>Usage:
<br>    java graffitv.FlushPlayer
<br>        -image <image file|URL>
<br>        [-file <cassette file> (else cassette written to stdout)]
<br>        [-title <cassette title>]
<br>        [-sound <sound file|URL>]
<br>        [-scale_image (scale to cassette size)]
<br>        [-size <cassette ("toilet") size (pixels)>]
<br>        [-hole_size <"toilet hole" size (pixels)>]
<br>        [-fragment_size <image fragment size (pixels)>]
<br>        [-rotation_increment <rotation increment (degrees)>]
<br>        [-flush_delay <image sequence delay while flushing (ms)>]
<br>        [-recharge_delay <image sequence delay while recharging (ms)>]
<br>        [-num_spiral <number of spiraling "swirls">]
<br>        [-min_spiral_scale <minimum spiral scale (0.00:1.00, .01 increments)>]
<br>        [-max_spiral_scale <maximum spiral scale (0.00:1.00, .01 increments)>]
<br>        [-spiral_converge <rate at which fragments converge on spirals (0.0:1.0)>]
<br>        [-random_seed <random number seed>]
<br>        [-display (animation)]
<br>Note: re-build after recording a cassette.
