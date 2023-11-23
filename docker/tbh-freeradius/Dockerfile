FROM freeradius/freeradius-server

RUN apt-get update && apt-get install -y postgresql-client

COPY init.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
