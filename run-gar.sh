#jmc --> -XX:+UnlockCommercialFeatures -XX:+FlightRecorder

java -Xmx16m -cp pinger-global-aggregator/target/pinger-global-aggregator-1.0-jar-with-dependencies.jar us.woop.pinger.GAR &
PID=$!
echo $PID > target/gar.pid
fg
rm target/gar.pid 
