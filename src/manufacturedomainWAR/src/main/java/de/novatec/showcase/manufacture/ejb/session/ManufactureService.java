package de.novatec.showcase.manufacture.ejb.session;

import de.novatec.showcase.manufacture.ejb.entity.*;
import de.novatec.showcase.manufacture.ejb.session.exception.AssemblyNotFoundException;
import de.novatec.showcase.manufacture.ejb.session.exception.BomNotFoundException;
import de.novatec.showcase.manufacture.ejb.session.exception.ComponentNotFoundException;
import de.novatec.showcase.manufacture.ejb.session.exception.InventoryNotFoundException;

import java.util.Collection;
import java.util.List;

public interface ManufactureService {
	Component findComponent(String id) throws ComponentNotFoundException;

	Collection<Component> getAllComponents();

	Assembly findAssembly(String id) throws AssemblyNotFoundException;

	Collection<Assembly> getAllAssemblies();

	Collection<String> getAllAssemblyIds();

	Collection<Bom> getAllBoms();

	Inventory getInventory(String compId, Integer location) throws InventoryNotFoundException;

	Collection<Inventory> getAllInventories();

	void deliver(List<ComponentDemand> delivery) throws InventoryNotFoundException;

	Component createComponent(Component component) throws ComponentNotFoundException;

	Component deleteComponent(Integer componentId) throws ComponentNotFoundException;

	Assembly createAssembly(Assembly assembly);

	Inventory createInventory(Inventory inventory) throws ComponentNotFoundException;

	Bom createBom(Bom bom) throws AssemblyNotFoundException, ComponentNotFoundException;

	Bom findBom(BomPK bomPK) throws BomNotFoundException;

	void addBomToComponent(BomPK bomPK) throws AssemblyNotFoundException, ComponentNotFoundException, BomNotFoundException;
}
