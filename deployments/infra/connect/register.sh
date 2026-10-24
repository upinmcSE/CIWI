#!/bin/sh
set -eu

SRC="/etc/connector/connector.json"
RENDERED="/tmp/connector.rendered.json"

echo "[register] waiting for Kafka Connect at ${CONNECT_URL} ..."
until curl -fsS "${CONNECT_URL}/" >/dev/null 2>&1; do
  sleep 2
done
echo "[register] connect is up"

# envsubst isn't in curlimages/curl; do the substitution with sed. Only the
# handful of placeholders we control are expanded — no shell-metachar risk.
sed \
  -e "s|\${POSTGRES_HOST}|${POSTGRES_HOST}|g" \
  -e "s|\${POSTGRES_PORT}|${POSTGRES_PORT}|g" \
  -e "s|\${POSTGRES_DB}|${POSTGRES_DB}|g" \
  -e "s|\${CDC_USER}|${CDC_USER}|g" \
  -e "s|\${CDC_PASSWORD}|${CDC_PASSWORD}|g" \
  "${SRC}" > "${RENDERED}"

echo "[register] posting connector"
HTTP_CODE=$(curl -sS -o /tmp/resp.json -w '%{http_code}' \
  -X POST \
  -H 'Content-Type: application/json' \
  --data @"${RENDERED}" \
  "${CONNECT_URL}/connectors")

echo "[register] HTTP ${HTTP_CODE}"
cat /tmp/resp.json || true
echo

case "${HTTP_CODE}" in
  2*) echo "[register] created"; exit 0 ;;
  409) echo "[register] already exists — ok"; exit 0 ;;
  *)  echo "[register] failed"; exit 1 ;;
esac