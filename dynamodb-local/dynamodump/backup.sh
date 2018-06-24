BUCKET=dynamodb-shashkiwiki-backups
DATE=$(date +%x-%X)
./dynamodump.py --host localhost --port 8765 -m backup -r local -s SHASHKIWIKI* -a zip 
aws s3 cp dump.zip s3://$BUCKET/backups/dump-$DATE.zip
cp -r ../shashki_online .
zip -r shashki_online-$DATE.zip shashki_online
rm -rf shashki_online
aws s3 cp shashki_online-$DATE.zip s3://$BUCKET/config/
