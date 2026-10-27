# REST API - Σύστημα Αξιολόγησης

## 1. Αποστολή αιτήματος εικονικού επιβάτη (Προς Κεντρικό Σύστημα)
* **Endpoint:** `POST /api/central/dispatch`
* **JSON Request:**
```json
{
  "timestamp": "08:00:15",
  "passengerId": "P-101",
  "currentFloor": 0,
  "destinationFloor": 7
}
```

## 2. Λήψη δεδομένων τηλεμετρίας (Από Ανελκυστήρες)
* **Endpoint:** `POST /api/evaluation/metrics`
* **JSON Request:**
```json
{
  "elevatorId": "B",
  "timestamp": "08:00:25",
  "action": "DOORS_CLOSED",
  "currentFloor": 0,
  "passengersOnboard": 1
}
```
