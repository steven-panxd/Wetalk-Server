# Wetalk-Server
A simple chat application on Java. (Server side)

# Dependencies
#### All required jar files are in libs folder
- [Kryonet](https://github.com/EsotericSoftware/kryonet "Kryonet") (Java TCP/UDP server/client library)
- [Gson](https://github.com/google/gson "Gson") (Json serializer)
- [Java-JWT](https://github.com/auth0/java-jwt "Java-JWT") (Jason Web Token generator)
- [SQLite](https://www.sqlite.org/index.html "SQLite") and [SQLite JDBC](https://github.com/xerial/sqlite-jdbc "SQLite JDBC") (Database)
- [Redis](https://redis.io/ "Redis") and [Jedis](https://github.com/redis/jedis "Jedis") (Cache) // Please install Redis before launching the application

# Sample data format
### Sample request format (from client to server)
```json
{
	"request": "<login>",
	"data": {
		"username": "username",
		"password": "password"
	}
}
```

### Sample response format (from server to client)
```json
{
	"response": "<login>",
	"status": "<succeed>",
	"data": {
		"accessToken": "xxxxxxxxxxxxxxxxxxxxxxxxx",
		"id": "1"
	}
}
```
