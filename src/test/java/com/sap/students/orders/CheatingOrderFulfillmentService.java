package com.sap.students.orders;

import java.util.Collection;
import java.util.Collections;


/**
 * Sample implementation of OrderFulfillment that can pass OrderFulfillmentTest#simpleTestCase()
 * It actually doesn't work ;-)
 */
public class CheatingOrderFulfillmentService implements OrderFulfillmentService
{
	@Override
	public Collection<Shipment> fulfillOrders(final Collection<Order> orders, final Collection<InventoryEntry> inventoryEntries)
			throws CannotFulfillException
	{
		if (inventoryEntries.isEmpty())
		{
			throw new CannotFulfillException();
		}

		final Shipment shipment = new Shipment()
		{
			@Override
			public String getLocation()
			{
				return inventoryEntries.iterator().next().getLocation();
			}

			@Override
			public String getDestination()
			{
				return orders.iterator().next().getDestination();
			}

			@Override
			public String getSku()
			{
				return orders.iterator().next().getSku();
			}

			@Override
			public int getAmount()
			{
				return orders.iterator().next().getAmount();
			}
		};
		return Collections.singleton(shipment);
	}
}
