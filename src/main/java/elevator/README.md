# Σύστημα Ανελκυστήρα 
# Διεπαφές Εξερχόμενης Επικοινωνίας Ανελκυστήρα (Outbound REST API)

Αυτό το έγγραφο περιγράφει τις εξερχόμενες διεπαφές REST (Paths, HTTP Methods, JSON Payloads) του Συστήματος Ελέγχου Ανελκυστήρα προς:
1. **Το Υλικό του Ανελκυστήρα** (Κινητήρας, Θύρες, Οθόνες) μέσω της διεπαφής `HardwareBridgeClient`.
2. **Το Σύστημα Δοκιμών & Αξιολόγησης** μέσω της διεπαφής `EvaluationMetricsClient`.

---

## 1. Επικοινωνία με Υλικό (`HardwareBridgeClient`)

### 1.1 Κίνηση Κινητήρα (`move`)
* **HTTP Method:** `POST`
* **Path:** `/api/hardware/motor/move`
* **Request Body (JSON):**
```json
{
  "elevatorId": "A",
  "direction": "UP"
}
```
*(Επιτρεπτές τιμές `Direction`: `"UP"`, `"DOWN"`, `"NONE"`)*
* **Response:** `200 OK`
```json
{
  "status": "MOVING",
  "direction": "UP"
}
```

### 1.2 Ακινητοποίηση Κινητήρα (`stop`)
* **HTTP Method:** `POST`
* **Path:** `/api/hardware/motor/stop`
* **Request Body (JSON):**
```json
{
  "elevatorId": "A"
}
```
* **Response:** `200 OK`
```json
{
  "status": "STOPPED"
}
```

### 1.3 Άνοιγμα Θυρών (`openDoors`)
* **HTTP Method:** `POST`
* **Path:** `/api/hardware/doors/open`
* **Request Body (JSON):**
```json
{
  "elevatorId": "A",
  "doorState": "OPEN"
}
```
*(Επιτρεπτές τιμές `DoorState`: `"OPEN"`, `"CLOSED"`, `"OPENING"`, `"CLOSING"`)*
* **Response:** `200 OK`
```json
{
  "doorState": "OPENING"
}
```

### 1.4 Κλείσιμο Θυρών (`closeDoors`)
* **HTTP Method:** `POST`
* **Path:** `/api/hardware/doors/close`
* **Request Body (JSON):**
```json
{
  "elevatorId": "A",
  "doorState": "CLOSED"
}
```
* **Response:** `200 OK`
```json
{
  "doorState": "CLOSING"
}
```

### 1.5 Ενημέρωση Οθόνης (`updateDisplay`)
* **HTTP Method:** `PUT`
* **Path:** `/api/hardware/display`
* **Request Body (JSON):**
```json
{
  "elevatorId": "A",
  "currentFloor": 3,
  "direction": "UP"
}
```
* **Response:** `200 OK`

---

## 2. Επικοινωνία με Σύστημα Αξιολόγησης (`EvaluationMetricsClient`)

### 2.1 Αποστολή Συμβάντος Λειτουργίας (`sendEvent`)
* **HTTP Method:** `POST`
* **Path:** `/api/evaluation/events`
* **Request Body (JSON - `ElevatorEventDto`):**
```json
{
  "elevatorId": "A",
  "timestamp": 1730712000000,
  "eventType": "FLOOR_REACHED",
  "floor": 4
}
```
* **Response:** `201 Created`

### 2.2 Αποστολή Μετρικής Απόδοσης (`sendMetric`)
* **HTTP Method:** `POST`
* **Path:** `/api/evaluation/metrics`
* **Request Body (JSON):**
```json
{
  "elevatorId": "A",
  "metricName": "DOOR_OPERATION_TIME_MS",
  "value": 1500.0
}
```
* **Response:** `201 Created`

---

## 3. Μοντέλο Παραμέτρων Λειτουργίας (`ElevatorConfigDto`)
```json
{
  "minFloor": 0,
  "maxFloor": 10,
  "floorTravelTimeMs": 2500,
  "doorOperationTimeMs": 1500
}
```
