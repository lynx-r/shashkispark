Бекенд состоит из трех микросервисов: `articleservice`, `boardservice`, `securityservice`.
Для деплоя локально в папку /var/lib/tomcat8/webapps выполните команду:

```
gradle clean build deploylocal
```

В папке `dynamodb-local` описание таблиц DynamoDB.
