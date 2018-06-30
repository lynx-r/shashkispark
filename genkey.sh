openssl genrsa -out rootCA.key 2048
openssl req -x509 -new -key rootCA.key -days 10000 -out rootCA.crt
openssl genrsa -out shashki.online.key 2048
openssl req -new -key shashki.online.key -out shashki.online.csr
openssl x509 -req -in shashki.online.csr -CA rootCA.crt -CAkey rootCA.key -CAcreateserial -out shashki.online.crt -days 5000
