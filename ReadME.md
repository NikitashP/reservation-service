## Reservation Service

- reservation APIs need public key

### creating reservation

#### request
```text
curl -X POST \
  http://localhost:8081/create \
  -H 'Content-Type: application/json' \
  -H 'X-API-Key: abcdef123456' \
  -H 'cache-control: no-cache' \
  -d '{
	"hotelId":"b3163e2b-d5e6-44bb-adbd-fee3ed74565d",
	"customerId":"0f93221c-adb5-4705-8033-22abe248e590"
}'
```
#### response
```text
9859c174-7f52-462c-9035-26a86b085815
```
###  reservation details

#### request
```text
curl -X GET \
  http://localhost:8081/reservation/9859c174-7f52-462c-9035-26a86b085815 \
  -H 'Content-Type: application/json' \
  -H 'X-API-Key: abcdef123456' \
  -d '{
	"hotelId":"b3163e2b-d5e6-44bb-adbd-fee3ed74565d",
	"customerId":"0f93221c-adb5-4705-8033-22abe248e590"
}'
```
#### response
```text
{
    "id": "9859c174-7f52-462c-9035-26a86b085815",
    "hotelId": "b3163e2b-d5e6-44bb-adbd-fee3ed74565d",
    "customerId": "0f93221c-adb5-4705-8033-22abe248e590",
    "status": "PENDING_APPROVAL"
}
```

### all change to reservation

#### request
```text
curl -X GET \
  http://localhost:8081/events/9859c174-7f52-462c-9035-26a86b085815 \
  -H 'Content-Type: application/json' \
  -H 'X-API-Key: abcdef123456' \
  -d '{
	"hotelId":"b3163e2b-d5e6-44bb-adbd-fee3ed74565d",
	"customerId":"0f93221c-adb5-4705-8033-22abe248e590"
}'
```
#### response
```text
[
    {
        "id": "9859c174-7f52-462c-9035-26a86b085815",
        "customerId": "0f93221c-adb5-4705-8033-22abe248e590",
        "hotelId": "b3163e2b-d5e6-44bb-adbd-fee3ed74565d",
        "status": "INITIATED"
    }
]
```