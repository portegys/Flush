# Flush
Flush images away. Prototype demo for the GraffiTV project (see GraffiTV.pdf).

Build:<br>
```
build.sh/build.bat
```

Run flush as app:<br>
```
    java -jar FlushPlayer.jar
        [-loadCassette <file name> (cassette to load)]
        [-cassetteChoices <comma-separated list of cassette names>]
```

Run flush as applet:
```
appletviewer flush.html # applet parameters: "load" and "choice"
```

Record flush cassette:<br>
```
mkcassette.sh
```

```
    java -jar FlushRecorder.jar
        -image <image file|URL>
        [-file <cassette file> (else cassette written to stdout)]
        [-title <cassette title>]
        [-sound <sound file|URL>]
        [-scale_image (scale to cassette size)]
        [-size <cassette ("toilet") size (pixels)>]
        [-hole_size <"toilet hole" size (pixels)>]
        [-fragment_size <image fragment size (pixels)>]
        [-rotation_increment <rotation increment (degrees)>]
        [-flush_delay <image sequence delay while flushing (ms)>]
        [-recharge_delay <image sequence delay while recharging (ms)>]
        [-num_spiral <number of spiraling "swirls">]
        [-min_spiral_scale <minimum spiral scale (0.00:1.00, .01 increments)>]
        [-max_spiral_scale <maximum spiral scale (0.00:1.00, .01 increments)>]
        [-spiral_converge <rate at which fragments converge on spirals (0.0:1.0)>]
        [-random_seed <random number seed>]
        [-display (animation)]
```

Note: re-build after recording a cassette.
