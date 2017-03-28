openssl enc -base64 -in password.txt -out password_base64.txt
openssl genrsa -out privkey.pem 2048
openssl req -key privkey.pem -new -x509 -days 365 -out certificate.crt
openssl pkcs12 -export -in certificate.crt -inkey privkey.pem -out certificate.p12 -passout file:password.txt