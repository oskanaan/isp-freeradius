FROM debian:bullseye-slim
RUN apt-get update && apt-get install -y freeradius-utils
COPY continuous_check.sh /continuous_check.sh
RUN chmod +x /continuous_check.sh
ENTRYPOINT ["/continuous_check.sh"]