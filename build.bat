javac -d . *.java
copy *.cassette graffitv
jar cfm FlushPlayer.jar flushplayer.mf graffitv *.au
jar cfm FlushRecorder.jar flushrecorder.mf graffitv *.au


