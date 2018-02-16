Cryptocheck

Application that retrieves prices from CryptoCompare public API
Due to limitation of URL parameters, requests are sent to the API in batches

Crypto to be retrieved is configurable in supportedCoins.json

Usage:

Publish build to local docker repository
sbt docker:publishLocal

Start environment locally bound to port 9001
docker images
Specify cryptocheck image version in docker-compose.yml
docker-compose up