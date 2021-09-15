package com.sap.students.orders;

public interface Shipment
{
	/**
	 * @return inventory location (store) with the ordered products (shipment from location)
	 */
	String getLocation();

	/**
	 * @return destination id of delivery address (shipment to destination)
	 */
	String getDestination();

	/**
	 * @return Stock-keepeng unit of product
	 */
	String getSku();

	/**
	 * @return amount of product
	 */
	int getAmount();
}
