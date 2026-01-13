To "reinitialize" the db in case you change the content script_pg.sql:
```bash
docker compose down

docker compose down -v

docker-compose up --build -d
```

-v will delete the volumes

--build for everytime you change a Java code