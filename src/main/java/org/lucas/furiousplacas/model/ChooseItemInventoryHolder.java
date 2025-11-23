package org.lucas.furiousplacas.model;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ChooseItemInventoryHolder  implements InventoryHolder {
    private Inventory inventory;
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
