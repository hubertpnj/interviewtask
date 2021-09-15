## Technical interview task

### Task goal

The goal of this task is to implement method ***fulfillOrders*** in ***OrderFulfillmentServiceImpl***.
Method finds the way with a minimum number of shipments to process all orders for given inventory entries.
Solution should have also find a minimum number of location to destination pairs.

#### Data model

Order
- destination
- sku
- amount

InventoryEntry
- location
- sku
- amount

Shipment
- location
- destination
- sku
- amount

### Task verification

Correctness of the solution is checked by junit ***OrderFulfillmentServiceTest***
Please edit this file and ideally add more tests to it for checking other possible cases to make sure solution is more robust.

### Example

We have orders
* Order1 (destination: "Gliwice", sku: "prod-1", amount: 3)
* Order2 (destination: "Gliwice", sku: "prod-2", amount: 5)
* Order3 (destination: "Warszawa", sku: "prod-1", amount: 4)

and Inventories

* InventoryEntry(location: "Radom", sku: "prod-2", amount: 7)
* InventoryEntry(location: "Kraków", sku: "prod-1", amount: 7)
* InventoryEntry(location: "Kraków", sku: "prod-2", amount: 9)


Result of ***OrderFulfillmentService***.***fulfillOrders*** should be as follow

Shipments
* Shipment (location: "Kraków", destination: "Gliwice", sku: "prod-1", amount: 3)
* Shipment (location: "Kraków", destination: "Warszawa", sku: "prod-1", amount: 4)
* Shipment (location: "Kraków", destination: "Gliwice", sku: "prod-2", amount: 5)

We need only **two** packages (consignments) to fulfill the orders

> Algorithm should not pick Radom warehouse in this case as the result will have three packages and will not be optimal

### Others
Proejct is simple maven project and to run it easily maven is required (https://maven.apache.org/)
