# Flush player makefile

CC = cc

all: FlushPlayer.jar

FlushPlayer.jar: FlushPlayer.java FlushCassette.java
	javac -target 1.8 -d . FlushPlayer.java FlushCassette.java
	cp *.cassette graffitv
	cp flush.au graffitv
	jar cf FlushPlayer.jar graffitv

clean:
	/bin/rm -fr graffitv
	/bin/rm FlushPlayer.jar


