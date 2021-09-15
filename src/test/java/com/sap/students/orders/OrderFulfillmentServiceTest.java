package com.sap.students.orders;


import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;


public class OrderFulfillmentServiceTest
{
	private final OrderFulfillmentService fulfillment = new OrderFulfillmentServiceImpl();

	@Test
	public void simpleTestCase()
	{
		// given
		final InventoryEntry entry = createInventoryEntry("Gliwice", "p1", 10);
		final Order order = createOrder("Munich", "p1", 10);

		// when
		final Collection<Shipment> shipments = fulfillment.fulfillOrders(Collections.singleton(order), Collections.singleton(entry));

		// then
		assertThat(shipments).hasSize(1);
		final Shipment shipment = shipments.iterator().next();
		assertThat(shipment.getLocation()).isEqualTo(entry.getLocation());
		assertThat(shipment.getDestination()).isEqualTo(order.getDestination());
		assertThat(shipment.getSku()).isEqualTo(order.getSku());
		assertThat(shipment.getAmount()).isEqualTo(order.getAmount());
	}
	
	@Test
	public void sampleData()
	{
		final Collection<Order> orders = new ArrayList<>();
		final Collection<InventoryEntry> inventory = new ArrayList<>();
		
		// given
		orders.add(createOrder("Sydney", "prod-1", 14));
		orders.add(createOrder("Sydney", "prod-2", 10));
		orders.add(createOrder("Sydney", "prod-3", 3));
		orders.add(createOrder("Tokyo", "prod-2", 6));
		orders.add(createOrder("Sydney", "prod-3", 1));
		orders.add(createOrder("Tokyo", "prod-4", 5));
		
		inventory.add(createInventoryEntry("Gliwice", "prod-1", 10));
		inventory.add(createInventoryEntry("Gliwice", "prod-2", 12));
		inventory.add(createInventoryEntry("Gliwice", "prod-3", 5));
		inventory.add(createInventoryEntry("Munich", "prod-1", 4));
		inventory.add(createInventoryEntry("Munich", "prod-2", 4));
		inventory.add(createInventoryEntry("Munich", "prod-3", 4));
		inventory.add(createInventoryEntry("Montreal", "prod-1", 4));
		inventory.add(createInventoryEntry("Montreal", "prod-4", 5));
		
		// when
		final Collection<Shipment> shipments = fulfillment.fulfillOrders(orders, inventory);

		// count packages
		class TestPackage{
			String location;
			String destination;
		}
		final ArrayList<TestPackage> packages = new ArrayList<>();
		
		for(final Shipment shipment : shipments)
		{
			boolean found = false;
			
			for(final TestPackage singlePackage : packages)
			{
				if(shipment.getLocation().equalsIgnoreCase(singlePackage.location) &&
						shipment.getDestination().equalsIgnoreCase(singlePackage.destination))
				{
					found = true;
					break;
				}
			}
			
			if(!found)
			{				
				final TestPackage newPackage = new TestPackage();
				newPackage.location = shipment.getLocation();
				newPackage.destination = shipment.getDestination();
				
				packages.add(newPackage);
			}
		}
		
		// then
		assertThat(packages).hasSize(4);
	}
	
	@Test(expected = CannotFulfillException.class)
	public void lucksInInventory()
	{
		final Collection<Order> orders = new ArrayList<>();
		final Collection<InventoryEntry> inventory = new ArrayList<>();
		
		// given
		orders.add(createOrder("Sydney", "prod-1", 14));
		orders.add(createOrder("Sydney", "prod-2", 10));
		orders.add(createOrder("Sydney", "prod-5", 3)); // there is no such product in the inventory
		orders.add(createOrder("Tokyo", "prod-2", 6));
		orders.add(createOrder("Sydney", "prod-3", 1));
		orders.add(createOrder("Tokyo", "prod-4", 6)); // there is only 5 of this in the inventory
		
		inventory.add(createInventoryEntry("Gliwice", "prod-1", 10));
		inventory.add(createInventoryEntry("Gliwice", "prod-2", 12));
		inventory.add(createInventoryEntry("Gliwice", "prod-3", 5));
		inventory.add(createInventoryEntry("Munich", "prod-1", 4));
		inventory.add(createInventoryEntry("Munich", "prod-2", 4));
		inventory.add(createInventoryEntry("Munich", "prod-3", 4));
		inventory.add(createInventoryEntry("Montreal", "prod-1", 4));
		inventory.add(createInventoryEntry("Montreal", "prod-4", 5));
		
		// when
		fulfillment.fulfillOrders(orders, inventory);
	}

	@Test(expected = CannotFulfillException.class)
	public void shouldThrowException()
	{
		// given
		final Order order = createOrder("Munich", "p1", 10);

		// when
		fulfillment.fulfillOrders(Collections.singleton(order),
				Collections.<InventoryEntry>emptyList());

	}

	private Order createOrder(final String destination, final String sku, final int amount)
	{
		return new Order()
		{
			@Override
			public String getDestination()
			{
				return destination;
			}

			@Override
			public String getSku()
			{
				return sku;
			}

			@Override
			public int getAmount()
			{
				return amount;
			}
		};
	}

	private InventoryEntry createInventoryEntry(final String location, final String sku, final int amount)
	{
		return new InventoryEntry()
		{
			@Override
			public String getLocation()
			{
				return location;
			}

			@Override
			public String getSku()
			{
				return sku;
			}

			@Override
			public int getAmount()
			{
				return amount;
			}
		};
	}
}
