FROM java:7
MAINTAINER Teo Klestrup Röijezon <teo@nullable.se>
ADD target/universal/stage /app
CMD /app/bin/playmatcher -Ddb.default.url=jdbc:postgresql://$DB_1_PORT_5432_TCP_ADDR:$DB_1_PORT_5432_TCP_PORT/postgres -DapplyEvolutions.default=true -DapplyDownEvolutions.default=true -Dhttp.port=80
EXPOSE 80
