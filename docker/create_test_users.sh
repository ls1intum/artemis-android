#!/bin/bash

serverUrl=$1

jwtRegex="jwt=((\w|\d|\.|-)*)"

responseHeaders=$(
  curl -X POST http://"$serverUrl"/api/core/public/authenticate \
    -H "Content-Type: application/json" \
    -d '{"username":"artemis_admin","password":"artemis_admin","rememberMe":true}' \
    -i
)


[[ $responseHeaders =~ $jwtRegex ]]
adminJwt=${BASH_REMATCH[1]}

# "internal" has to be set: the server only hashes and stores the supplied password for internal
# users, so without it the account is created with a null password and every later login is a 401.
for i in 1 2 3
do
  curl -X POST http://"$serverUrl"/api/account/admin/users \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=${adminJwt};" \
  -d '{"authorities":["ROLE_USER"],"login":"aa0'${i}'aaa","email":"test_user'${i}'@example.com","firstName":"Test","lastName":"User'${i}'","guidedTourSettings":[],"groups":["default"],"internal":true,"password":"test_user_'${i}'_password"}'
done
