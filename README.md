# Local DynamoDB

This project configured to use local DynamoDB. To run it execute this:

```
$ cd artcleservice
$ sls dynamodb start
$ cd boardservice
$ sls dynamodb start
```

```
$ gradle :articleservice:run
$ gradle :boardservice:run
$ gradle :securityservice:run
```
