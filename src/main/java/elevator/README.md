# Σύστημα Ανελκυστήρα
## Διεπαφές Εξερχόμενης Επικοινωνίας Ανελκυστήρα (Outbound REST API)

Αυτό το έγγραφο περιγράφει τις εξερχόμενες διεπαφές REST (Paths, HTTP Methods, JSON Payloads) του Συστήματος Ελέγχου Ανελκυστήρα προς:
1. **Το Υλικό του Ανελκυστήρα** (Κινητήρας, Θύρες, Οθόνες) μέσω της διεπαφής `HardwareBridgeClient`.
2. **Το Σύστημα Δοκιμών & Αξιολόγησης** (`EvaluationApp`, προεπιλεγμένη θύρα `7001`) μέσω της διεπαφής `EvaluationMetricsClient`.

---

## 1. Επικοινωνία με Υλικό (`HardwareBridgeClient`)

### 1.1 Κίνηση Κινητήρα (`move`)
- **HTTP Method:** `POST`
- **Path:** `/api/hardware/motor/move`
- **Request Body (JSON):**
```json
{
  "elevatorId": "A",
  "direction": "UP"
}
```
*(Επιτρεπτές τιμές `Direction`: `"UP"`, `"DOWN"`, `"IDLE"`)*
- **Response:** `200 OK`
```json
{
  "status": "MOVING",
  "direction": "UP"
}
```

### 1.2 Ακινητοποίηση Κινητήρα (`stop`)
- **HTTP Method:** `POST`
- **Path:** `/api/hardware/motor/stop`
- **Request Body (JSON):**
```json
{
  "elevatorId": "A"
}
```
- **Response:** `200 OK`
```json
{
  "status": "STOPPED"
}
```

### 1.3 Άνοιγμα Θυρών (`openDoors`)
- **HTTP Method:** `POST`
- **Path:** `/api/hardware/doors/open`
- **Request Body (JSON):**
```json
{
  "elevatorId": "A",
  "doorState": "OPEN"
}
```
*(Επιτρεπτές τιμές `DoorState`: `"OPEN"`, `"CLOSED"`, `"OPENING"`, `"CLOSING"`)*
- **Response:** `200 OK`
```json
{
  "doorState": "OPENING"
}
```

### 1.4 Κλείσιμο Θυρών (`closeDoors`)
- **HTTP Method:** `POST`
- **Path:** `/api/hardware/doors/close`
- **Request Body (JSON):**
```json
{
  "elevatorId": "A",
  "doorState": "CLOSED"
}
```
- **Response:** `200 OK`
```json
{
  "doorState": "CLOSING"
}
```

### 1.5 Ενημέρωση Οθόνης (`updateDisplay`)
- **HTTP Method:** `PUT`
- **Path:** `/api/hardware/display`
- **Request Body (JSON):**
```json
{
  "elevatorId": "A",
  "currentFloor": 3,
  "direction": "UP"
}
```
- **Response:** `200 OK`

---

## 2. Επικοινωνία με Σύστημα Αξιολόγησης (`EvaluationMetricsClient`)

Προεπιλεγμένη διεύθυνση `EvaluationApp`: `http://localhost:7001`.

### 2.1 Αποστολή Συμβάντος Λειτουργίας (`sendEvent`)
- **HTTP Method:** `POST`
- **Path:** `/api/evaluation/events`
- **Request Body (JSON - `ElevatorEventDto`):**
```json
{
  "elevatorId": "A",
  "timestamp": 1730712000000,
  "eventType": "FLOOR_REACHED",
  "floor": 4
}
```
- **Response:** `200 OK`
```json
{
  "status": "SUCCESS",
  "message": "Event recorded",
  "totalEvents": 1
}
```

### 2.2 Αποστολή Μετρικής Απόδοσης (`sendMetric` & KPI Wrappers)
Καλύπτει τη `sendMetric(metricName, value)` καθώς και τις wrapper μεθόδους `sendWaitTimeMetric` (`AVERAGE_WAIT_TIME_SEC`), `sendJourneyTimeMetric` (`AVERAGE_JOURNEY_TIME_SEC`) και `sendEnergyEfficiencyMetric` (`TOTAL_FLOORS_MOVED`).
- **HTTP Method:** `POST`
- **Path:** `/api/evaluation/metrics`
- **Request Body (JSON):**
```json
{
  "elevatorId": "A",
  "metricName": "AVERAGE_WAIT_TIME_SEC",
  "value": 12.5
}
```
- **Response:** `200 OK`
```json
{
  "status": "SUCCESS",
  "message": "Metrics recorded successfully. Total requests: 1"
}
```

### 2.3 Αποστολή Τηλεμετρίας & Έλεγχος Χωρητικότητας (`sendTelemetry`)
Ευθυγραμμισμένο με το `evaluation/api-specs.md` για τον έλεγχο ορίου 8 επιβατών στο `EvaluationApp`.
- **HTTP Method:** `POST`
- **Path:** `/api/evaluation/metrics`
- **Request Body (JSON):**
```json
{
  "elevatorId": "B",
  "timestamp": "08:00:25",
  "action": "DOORS_CLOSED",
  "currentFloor": 0,
  "passengersOnboard": 1
}
```
- **Response (Εντός ορίων):** `200 OK` (`"status": "SUCCESS"`)
- **Response (Υπέρβαση > 8 επιβάτες):** `200 OK`
```json
{
  "status": "FAILED",
  "violation": "CAPACITY_EXCEEDED",
  "message": "Overload detected! Max limit is 8 passengers."
}
```

### 2.4 Ανάκτηση Σύνοψης Αξιολόγησης (`getEvaluationSummary`)
- **HTTP Method:** `GET`
- **Path:** `/api/evaluation/summary`
- **Response:** `200 OK`
```json
{
  "totalRequests": 2,
  "totalViolations": 0,
  "totalEvents": 1,
  "recordedMetricTypes": 1
}
```

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

---

## 4. Outbound REST API Communication (Client Layer) & Testing (TDD)

Αυτό το υποσύστημα αναλαμβάνει όλη την εξερχόμενη επικοινωνία (outbound HTTP requests) του ανελκυστήρα χρησιμοποιώντας τα κοινά DTOs του πακέτου `common` (`ElevatorEventDto`, `ElevatorConfigDto`).

Αποτελείται από δύο REST Clients:
1. **`HardwareBridgeRestClient`:** Επικοινωνεί με το hardware/προσομοιωτή (`openDoors`, `closeDoors`, `stop`, `move`, `updateDisplay`, `sendElevatorEvent`, `sendConfiguration`).
2. **`EvaluationMetricsRestClient`:** Επικοινωνεί με το `EvaluationApp` στέλνοντας events, μετρικές KPIs, τηλεμετρία χωρητικότητας και ανακτώντας το evaluation summary.

Τα unit tests (`HardwareBridgeRestClientTest` και `EvaluationMetricsRestClientTest`) εκτελούνται με JUnit 5 και τοπικό HTTP mock server, επιβεβαιώνοντας τόσο την ακριβή δομή των JSON payloads όσο και το **Graceful Error Handling** σε σφάλματα δικτύου ή HTTP 4xx/5xx χωρίς να διακόπτεται η λειτουργία του `ElevatorApiController`.