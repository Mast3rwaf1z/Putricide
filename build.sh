mkdir -v files
if test -f "files/config.conf"; then
    echo "file 'files/config.conf' already exists"
else
    echo "initializing file 'files/config.conf'"
    echo "token=" >> files/config.conf
fi
echo "compiling program..."
rm *.jar
mvn clean compile package -q -f nutbot
mv *.jar nutbot.jar
java -jar nutbot.jar $(curl -s -u $(bash config.sh clientId):$(bash config.sh clientSecret) -d grant_type=client_credentials https://oauth.battle.net/token)