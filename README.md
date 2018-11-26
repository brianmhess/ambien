# ambien
It gives you rest...

## Building
`mvn clean package`

## Running
`java -jar target/ambien-0.1-SNAPSHOT-jar-with-dependencies.jar <options>`

## Usage:
```
version: 0.0.1
Usage: ambien -host <hostname> -k <keyspaceName> -t <tableName> -o <outputDir> [options]
OPTIONS:
  -host <hostname>               Contact point for DSE [required]
  -k <keyspacename>              Keyspace to use [required]
  -t <tablename>                 Table to use [required]
  -o <outputDir>                 Directory to write to (must be empty) [required]
  -configFile <filename>         File with configuration options [none]
  -port <portNumber>             CQL Port Number [9042]
  -user <username>               Cassandra username [none]
  -pw <password>                 Password for user [none]
  -ssl-truststore-path <path>    Path to SSL truststore [none]
  -ssl-truststore-pw <pwd>       Password for SSL truststore [none]
  -ssl-keystore-path <path>      Path to SSL keystore [none]
  -ssl-keystore-pw <pwd>         Password for SSL keystore [none]

```