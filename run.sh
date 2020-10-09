export GOOGLE_APPLICATION_CREDENTIALS=$(find $(pwd)/keys -name "*.json")
echo $GOOGLE_APPLICATION_CREDENTIALS
sbt "~reStart"