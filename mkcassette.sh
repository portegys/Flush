# Make a cassette
if [ $# -ne 3 ]
then
	echo 'Usage: $0 <image file> <cassette title> <cassette file>'
	exit 1
fi
java -jar FlushRecorder.jar -image $1 -size 300 -title "$2" -file $3 -scale_image -sound flush.au -display
exit 0
