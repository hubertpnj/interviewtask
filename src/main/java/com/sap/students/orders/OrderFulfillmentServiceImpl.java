package com.sap.students.orders;

import java.util.*;
import java.util.stream.Collectors;

// NOTICE: I didn't know whether I can create separate classes, so they are nested
// NOTICE: As this code will never be reused I skipped most comments, leaving only explanatory ones
public class OrderFulfillmentServiceImpl implements OrderFulfillmentService {

    private static class Pair<A, B> {
        private final A first;
        private B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public Pair<A, B> copy() {
            return new Pair<>(first, second);
        }
    }

    private static class ListUtils {
        public static <E> E last(List<E> elements) {
            return elements.get(elements.size() - 1);
        }

        public static <E> void removeLast(List<E> elements) {
            elements.remove(elements.size() - 1);
        }
    }

    private static class Result {
        private final Map<String, Map<String, List<Shipment>>> shipmentPackages; // Location, Destination, Shipments
        private int packageCount;
        private int shipmentCount;

        public Result(Map<String, Map<String, List<Shipment>>> shipmentPackages, int packageCount, int shipmentCount) {
            this.shipmentPackages = shipmentPackages;
            this.packageCount = packageCount;
            this.shipmentCount = shipmentCount;
        }

        public Result() {
            this(new HashMap<>(), 0, 0);
        }

        public void addShipment(Shipment shipment) {
            shipmentPackages.computeIfAbsent(shipment.getLocation(), (ignore) -> new HashMap<>())
                    .computeIfAbsent(shipment.getDestination(), (ignore) -> {
                        packageCount++;
                        return new ArrayList<>();
                    })
                    .add(shipment);
            shipmentCount++;
        }

        public List<Shipment> getShipments() {
            return shipmentPackages.values().stream()
                    .flatMap(destinationShipments -> destinationShipments.values().stream())
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }

        public Result copy() {
            Map<String, Map<String, List<Shipment>>> shipmentPackagesCopy = new HashMap<>();

            for (Map.Entry<String, Map<String, List<Shipment>>> locationShipments : shipmentPackages.entrySet()) {
                Map<String, List<Shipment>> destinationShipmentsCopy = new HashMap<>();
                shipmentPackagesCopy.put(locationShipments.getKey(), destinationShipmentsCopy);

                for (Map.Entry<String, List<Shipment>> destinationShipments : locationShipments.getValue().entrySet())
                    destinationShipmentsCopy.put(destinationShipments.getKey(), new ArrayList<>(destinationShipments.getValue()));
            }

            return new Result(shipmentPackagesCopy, packageCount, shipmentCount);
        }
    }

    private static class State {
        private final Map<String, List<Pair<InventoryEntry, Integer>>> inventories; // SKU, (Inventory, remaining amount)
        private final List<Pair<Order, Integer>> orders; // (Order, remaining amount)

        public State(Map<String, List<Pair<InventoryEntry, Integer>>> inventories, List<Pair<Order, Integer>> orders) {
            this.inventories = inventories;
            this.orders = orders;
        }

        public State copy() {
            Map<String, List<Pair<InventoryEntry, Integer>>> inventoriesCopy = new HashMap<>();

            for (Map.Entry<String, List<Pair<InventoryEntry, Integer>>> inventory : inventories.entrySet()) {
                List<Pair<InventoryEntry, Integer>> entriesCopy = new ArrayList<>();
                inventoriesCopy.put(inventory.getKey(), entriesCopy);

                for (Pair<InventoryEntry, Integer> entry : inventory.getValue())
                    entriesCopy.add(entry.copy());
            }

            List<Pair<Order, Integer>> ordersCopy = new ArrayList<>();

            for (Pair<Order, Integer> order : orders)
                ordersCopy.add(order.copy());

            return new State(inventoriesCopy, ordersCopy);
        }
    }

    private static State createState(final Collection<Order> orders, final Collection<InventoryEntry> inventoryEntries) {
        Map<String, List<Pair<InventoryEntry, Integer>>> inventories = new HashMap<>();

        for (InventoryEntry entry : inventoryEntries)
            inventories.computeIfAbsent(entry.getSku(), (ignore) -> new LinkedList<>())
                    .add(new Pair<>(entry, entry.getAmount()));

        return new State(inventories, orders.stream()
                .map(order -> new Pair<>(order, order.getAmount()))
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    private Result backtrackHorizontal(final State state, final Result result) {
        // pruning worse results
        if (bestResult != null)
            if (betterResult(result, bestResult) != result)
                return null;

        // return current result
        if (state.orders.isEmpty())
            return result;

        Pair<Order, Integer> order = ListUtils.last(state.orders);
        List<Pair<InventoryEntry, Integer>> inventories = state.inventories.get(order.first.getSku());

        // no result at all
        if (inventories == null)
            throw new CannotFulfillException();

        ListIterator<Pair<InventoryEntry, Integer>> iterator = inventories.listIterator();

        // spreading in decision tree
        while (iterator.hasNext()) {
            Pair<InventoryEntry, Integer> inventory = iterator.next();
            iterator.remove();

            State stateCopy = state.copy();
            Result resultCopy = result.copy();

            iterator.add(inventory);

            Pair<InventoryEntry, Integer> inventoryCopy = inventory.copy();

            // deepening in decision tree
            Result currentResult = backtrackVertical(stateCopy, resultCopy, inventoryCopy);

            // keeping best result
            if (currentResult != null) {
                if (bestResult != null)
                    bestResult = betterResult(currentResult, bestResult);
                else
                    bestResult = currentResult;
            }
        }

        return bestResult;
    }

    private Result backtrackVertical(final State state, final Result result, final Pair<InventoryEntry, Integer> inventory) {
        List<Pair<Order, Integer>> orders = state.orders;
        Pair<Order, Integer> order = ListUtils.last(orders);

        List<Pair<InventoryEntry, Integer>> inventories = state.inventories.get(order.first.getSku());

        final int amount = balanceAmount(orders, inventories, order, inventory);
        Shipment shipment = createShipmentEntry(
                inventory.first.getLocation(),
                order.first.getDestination(),
                order.first.getSku(),
                amount);

        result.addShipment(shipment);
        return backtrackHorizontal(state, result);
    }

    private int balanceAmount(List<Pair<Order, Integer>> orders, List<Pair<InventoryEntry, Integer>> inventories,
                              Pair<Order, Integer> order, Pair<InventoryEntry, Integer> inventory) {
        if ((int) order.second != inventory.second) {
            if (order.second < inventory.second) {
                inventory.second -= order.second;

                ListUtils.removeLast(orders);
                inventories.add(inventory);

                return order.second;

            } else {
                order.second -= inventory.second;
                return inventory.second;
            }

        } else {
            ListUtils.removeLast(orders);
            return order.second;
        }
    }

    private Result betterResult(Result result1, Result result2) {
        if (result1.packageCount == result2.packageCount)
            return result1.shipmentCount < result2.shipmentCount ? result1 : result2;

        return result1.packageCount < result2.packageCount ? result1 : result2;
    }

    private Result bestResult = null;

    @Override
    public Collection<Shipment> fulfillOrders(final Collection<Order> orders, final Collection<InventoryEntry> inventoryEntries)
            throws CannotFulfillException {
        Result result = backtrackHorizontal(createState(orders, inventoryEntries), new Result());

        if (result == null)
            throw new CannotFulfillException();

        bestResult = null;
        return result.getShipments();
    }

    private Shipment createShipmentEntry(final String location, final String destination, final String sku, final int amount) {
        return new Shipment() {
            @Override
            public String getLocation() {
                return location;
            }

            @Override
            public String getDestination() {
                return destination;
            }

            @Override
            public String getSku() {
                return sku;
            }

            @Override
            public int getAmount() {
                return amount;
            }
        };
    }
}
