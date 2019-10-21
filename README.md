# ambien
It gives you rest...

## Building
`mvn clean package`

## Running
`target/ambien <options>`

## Usage:
```
version: 0.1.2
Usage: ambien -apolloBundle <creds.zip> -user <username> -pw <password> -kt <keyspaceName.tableName> -o <outputDir> [options]
OPTIONS:
  -dc <dataCenter>               Data center to connect to [dc1]
  -kt <keyspace.table>           Keyspace and Table to use, can be a comma-separated list [required]
  -o <outputDir>                 Directory to write to (must be empty) [required]
  -configFile <filename>         File with configuration options [none]
  -user <username>               Cassandra username [none]
  -pw <password>                 Password for user [none]
  -httpPort <httpPort>           Port for HTTP REST endpoint [8222]
  -endpointRoot <root>           REST endpoint to create (use '$keyspace' for keyspace name and '$table' for table name) [api/$keyspace/$table]
  -packageName <pkg>             Package name [hessian.ambien]
  -apolloBundle <filename>        Apollo credentials zip file [none]

```

You must supply the Apollo credentials zip file, username, password, at least one
keyspace.table, and an output directory (which should be empty).

This will produce a directory of source code in the supplied directory.
After running Ambien, change directory to the output directory and run:

`mvn clean package`

And then start the service with:

`java -jar target/package-0.0.1-SNAPSHOT.jar`

## Current API calls
There is an index.html page which lists all the generated REST endpoints:
``` 
http://hostname:8222/
```
Print Hello World:
```
http://hostname:8222/api/hello
```
Select all rows (GET):
```
http://hostname:8222/api/all
```
Select some rows (GET and POST):
``` 
http://hostname:8222/api/some?some={some}
```
Select by partition keys (GET and POST):
```
http://hostname:8222/api/<partitionKey1>_<partitionKey2>_..._<partitionKeyN>/?partitionKey1={partitionKey1}&partitionKey2={partitionKey2}&...&partitionKeyN={partitionKeyN}
```
Select by partition keys and clustering key(s) (GET and POST):
```
http://hostname:8222/api/<pkey1>_<ccol1>?pkey={pkey1}&ccol1={ccol1}
http://hostname:8222/api/<pkey1>_<ccol1>_<ccol2>?pkey1={pkey1}&ccol1={ccol1}&ccol2={ccol2}
...
```
Select by partition key and inequality on clustering key(s) (GET and POST):
```
http://hostname:8222/api/<pkey1>_<ccol1>_lt?pkey1={pkey1}&ccol1={ccol1}
http://hostname:8222/api/<pkey1>_<ccol1>_gt?pkey1={pkey1}&ccol1={ccol1}
http://hostname:8222/api/<pkey1>_<ccol1>_lte?pkey1={pkey1}&ccol1={ccol1}
http://hostname:8222/api/<pkey1>_<ccol1>_gte?pkey1={pkey1}&ccol1={ccol1}

http://hostname:8222/api/<pkey1>_<ccol1>_<ccol2>_lt?pkey1={pkey1}&ccol1={ccol1}&ccol2={ccol2}
http://hostname:8222/api/<pkey1>_<ccol1>_<ccol2>_gt?pkey1={pkey1}&ccol1={ccol1}&ccol2={ccol2}
http://hostname:8222/api/<pkey1>_<ccol1>_<ccol2>_lte?pkey1={pkey1}&ccol1={ccol1}&ccol2={ccol2}
http://hostname:8222/api/<pkey1>_<ccol1>_<ccol2>_gte?pkey1={pkey1}&ccol1={ccol1}&ccol2={ccol2}
...
```

## Spring Actuator
You can also access various metrics from the Actuator endpoints:
``` 
http://hostname:8222/actuator
```
There is a DSE HealthIndicator included, named AmbienHealthCheck:
``` 
http://hostname:8222/actuator/health
```

