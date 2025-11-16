:: need for kubernates
openssl pkcs12 -export -in ca.crt -inkey ca.key -out keystore.p12 -name springbootkey
kubectl create secret generic smart-ssl-keystore --from-file=keystore.p12 -n smart-dev