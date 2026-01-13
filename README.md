# IMPORTANT:
Before starting up the docker with ```docker compose up --build -d``` make sure to change ```./backend/sql/script_pg.sql```. 
And if you update the script mid-coding then read the instructions below.

# In case you changed the content or the DB doesn't update:
In case you changed the content script_pg.sql, you'll have to do this:
```bash
docker compose down

docker compose down -v

docker-compose up --build -d
```

-v will delete the volumes

--build for everytime you change a Java code