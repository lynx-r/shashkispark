rm -rf dump
BUCKET=dynamodb-shashkiwiki-backups
KEY=`aws s3 ls $BUCKET/backups --recursive | sort | tail -n 1 | awk '{print $4}'`
aws s3 cp s3://$BUCKET/$KEY ./dump.zip
unzip dump.zip
./dynamodump.py --host localhost --port 8765 -m restore -r local -s "*" -a zip 
rm -rf shashki_online
BUCKET=dynamodb-shashkiwiki-backups
KEY=`aws s3 ls $BUCKET/config --recursive | sort | tail -n 1 | awk '{print $4}'`
aws s3 cp s3://$BUCKET/$KEY ./shashki_online.zip
unzip shashki_online.zip
cp -r ./shashki_online/* /opt/dynamodb_local/shashki_online
