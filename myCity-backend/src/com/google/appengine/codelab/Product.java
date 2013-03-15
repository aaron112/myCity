package com.google.appengine.codelab;

import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

/**
 * This class handles all the CRUD operations related to Product entity.
 * 
 */
public class Product
{

	/**
	 * Update the product
	 * 
	 * @param name
	 *           : name of the product
	 * @param description
	 *           : description
	 * @param picKey
	 * @param visibility
	 * @return updated product
	 */
	public static void createOrUpdateProduct(String user, String name,
	         String description, String lat, String longi, String picKey,
	         String visibility)
	{
		Entity product = getProduct(name);
		if (product == null)
		{
			product = new Entity("Product", name);
			product.setProperty("user", user);
			product.setProperty("description", description);
			product.setProperty("latitude", lat);
			product.setProperty("longitude", longi);
			product.setProperty("picKey", picKey);
			product.setProperty("public", visibility);
		}
		else
		{
			product.setProperty("description", description);
			product.setProperty("latitude", lat);
			product.setProperty("longitude", longi);
			product.setProperty("picKey", picKey);
			product.setProperty("public", visibility);
		}
		Util.persistEntity(product);
	}

	/**
	 * Retrun all the products
	 * 
	 * @param kind
	 *           : of kind product
	 * @return products
	 */
	public static Iterable<Entity> getAllProducts(String kind)
	{
		return Util.listEntities(kind, null, null);
	}

	/**
	 * Get product entity
	 * 
	 * @param name
	 *           : name of the product
	 * @return: product entity
	 */
	public static Entity getProduct(String name)
	{
		Key key = KeyFactory.createKey("Product", name);
		return Util.findEntity(key);
	}

	/**
	 * Get all items for a product
	 * 
	 * @param name
	 *           : name of the product
	 * @return list of items
	 */

	@SuppressWarnings("deprecation")
	public static List<Entity> getItems(String name)
	{
		Query query = new Query();
		Key parentKey = KeyFactory.createKey("Product", name);
		query.setAncestor(parentKey);
		query.addFilter(Entity.KEY_RESERVED_PROPERTY,
		         Query.FilterOperator.GREATER_THAN, parentKey);
		List<Entity> results = Util.getDatastoreServiceInstance().prepare(query)
		         .asList(FetchOptions.Builder.withDefaults());
		return results;
	}

	/**
	 * Delete product entity
	 * 
	 * @param productKey
	 *           : product to be deleted
	 * @return status string
	 */
	public static String deleteProduct(String productKey)
	{
		Key key = KeyFactory.createKey("Product", productKey);

		List<Entity> items = getItems(productKey);
		if (!items.isEmpty())
		{
			return "Cannot delete, as there are items associated with this product.";
		}
		Util.deleteEntity(key);
		return "Product deleted successfully";

	}
}
