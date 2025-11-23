package org.lucas.furiousplacas.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.lucas.furiousplacas.FuriousPlacas;
import org.lucas.furiousplacas.api.Utilidades;
import org.lucas.furiousplacas.configurations.MessageConfig;
import org.lucas.furiousplacas.exceptions.CreateSignPlayerWithoutPermissionException;
import org.lucas.furiousplacas.exceptions.CreateSignServerWithoutPermissionException;
import org.lucas.furiousplacas.model.ChooseItemInventoryHolder;
import org.lucas.furiousplacas.model.CustomSign;
import org.lucas.furiousplacas.utils.Database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import static org.lucas.furiousplacas.listeners.CreateShopEvent.createSignLoja;

public class CloseChooseItemInventoryEvent implements Listener {
    private final MessageConfig messageConfig;
    private final FuriousPlacas furiousPlacas;
    private final Database database;

    public CloseChooseItemInventoryEvent(MessageConfig messageConfig, FuriousPlacas furiousPlacas, Database database) {
        this.messageConfig = messageConfig;
        this.database = database;
        this.furiousPlacas = furiousPlacas;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        CustomSign customSign = furiousPlacas.shopsInCreation.get(player.getUniqueId());
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString().split("-")[1];

        if (e.getInventory().getHolder() instanceof ChooseItemInventoryHolder) {
            ItemStack item = e.getInventory().getItem(4);
            if (item == null) return;
            try {
                database.addCustomSign(id, customSign.getSign(), item);
            } catch (SQLException | IOException ex) {
                ex.printStackTrace();
            }

            customSign.setItem(item);
            Utilidades.setItemLoja(player, furiousPlacas);

            String itemName = item.getData().getItemType().name();
            Block signBlock = customSign.getSign().getBlock();
            Sign sign = (Sign) signBlock.getState();
            sign.setLine(3, itemName + "#" + id);
            sign.update();
            try {
                createSignLoja(player);
                player.sendMessage(messageConfig.message("mensagens.criar_success"));

            } catch (CreateSignPlayerWithoutPermissionException error) {
                customSign.getSign().getBlock().breakNaturally(new ItemStack(Material.SIGN));
                player.sendMessage(messageConfig.message("mensagens.criar_erro5"));
                return;
            } catch (CreateSignServerWithoutPermissionException error) {
                customSign.getSign().getBlock().breakNaturally(new ItemStack(Material.SIGN));
                player.sendMessage(messageConfig.message("mensagens.criar_erro4"));
                return;
            }
        }
    }


}
