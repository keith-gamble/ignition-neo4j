java -jar ../module-signer.jar \
    -keystore=../../keystore.jks \
	-keystore-pwd=Keg@4426 \
	-alias="apple development" \
	-alias-pwd=Keg@4426 \
	-chain=../../apple_development.p7b \
	-module-in=./neo4j-driver-build/target/Neo4J-Driver-unsigned.modl \
	-module-out=./Neo4J-Driver.modl