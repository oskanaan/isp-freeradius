#!/bin/sh

#apt update
#apt install nano
cd /etc/freeradius/mods-enabled
ln -s ../mods-available/sql sql

sed -i -e 's/-\s*sql\s*$/sql/g' /etc/freeradius/sites-available/default
sed -i -e 's/#\s*sql\s*$/sql/g' /etc/freeradius/sites-available/default
sed -i -e 's/-\s*sql\s*$/sql/g' /etc/freeradius/sites-available/inner-tunnel
sed -i -e 's/#\s*sql\s*$/sql/g' /etc/freeradius/sites-available/inner-tunnel

sed -i -e 's/dialect = \"sqlite\"/dialect = \"postgresql\"/g' /etc/freeradius/mods-available/sql
sed -i -e 's/driver = \"rlm_sql_null\"/driver = \"rlm_sql_\$\{dialect\}\"/g' /etc/freeradius/mods-available/sql

sed -i -e "s/#\s*server = \"localhost\"/       server = \"$RADIUS_DB_HOST\"/g" /etc/freeradius/mods-available/sql
sed -i -e 's/#\s*port = 3306/       port = 5432/g' /etc/freeradius/mods-available/sql
sed -i -e "s/#\s*login = \"radius\"/       login = \"$RADIUS_DB_USER\"/g" /etc/freeradius/mods-available/sql
sed -i -e "s/#\s*password = \"radpass\"/       password = \"$RADIUS_DB_PASS\"/g" /etc/freeradius/mods-available/sql

sed -i -e 's/ca_file \= \"\/etc\/ssl\/certs\/my_ca.crt\"/#ca_file = \"\/etc\/ssl\/certs\/my_ca.crt\"/g' /etc/freeradius/mods-available/sql
sed -i -e 's/ca_path \= \"\/etc\/ssl\/certs\/\"/#ca_path \= \"\/etc\/ssl\/certs\/\"/g' /etc/freeradius/mods-available/sql
sed -i -e 's/tls_required \= yes/tls_required \= no/g' /etc/freeradius/mods-available/sql
sed -i -e 's/certificate_file \= \"\/etc\/ssl\/certs\/private\/client.crt\"/#certificate_file \= \"\/etc\/ssl\/certs\/private\/client.crt\"/g' /etc/freeradius/mods-available/sql
sed -i -e 's/private_key_file \= \"\/etc\/ssl\/certs\/private\/client.key\"/#private_key_file \= \"\/etc\/ssl\/certs\/private\/client.key\"/g' /etc/freeradius/mods-available/sql

set -e

host="$1"
shift
cmd="$@"
echo 'PGPASSWORD='$RADIUS_DB_PASS' psql -h '$RADIUS_DB_HOST' -U '$RADIUS_DB_USER''
until PGPASSWORD="$RADIUS_DB_PASS" psql -h "$RADIUS_DB_HOST" -U "$RADIUS_DB_USER" -c '\q'; do
  >&2 echo "PostgreSQL is unavailable - sleeping"
  sleep 1
done

>&2 echo "PostgreSQL is up - executing command"
exec $cmd
