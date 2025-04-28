cd SensorCommonPOJOs
mvn clean install
cd ..
cd SensorNode
mvn clean install
cd ..
cp SensorNode/target/SensorNode-0.0.1-SNAPSHOT.jar ../working_directory/SensorNode-0.0.1-SNAPSHOT.jar