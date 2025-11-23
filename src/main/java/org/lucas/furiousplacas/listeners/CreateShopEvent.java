package org.lucas.furiousplacas.listeners;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.lucas.furiousplacas.FuriousPlacas;
import org.lucas.furiousplacas.api.Utilidades;
import org.lucas.furiousplacas.configurations.MessageConfig;
import org.lucas.furiousplacas.exceptions.CreateSignPlayerWithoutPermissionException;
import org.lucas.furiousplacas.exceptions.CreateSignServerWithoutPermissionException;
import org.lucas.furiousplacas.model.ChooseItemInventoryHolder;
import org.lucas.furiousplacas.model.CustomSign;

public final class CreateShopEvent implements Listener {

    private final Economy economy;
    private final MessageConfig messageConfig;
    private final FuriousPlacas plugin;

    public CreateShopEvent(Economy economy, MessageConfig messageConfig, FuriousPlacas plugin) {
        this.economy = economy;
        this.messageConfig = messageConfig;
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void onCriar(SignChangeEvent e) {
        Player player = e.getPlayer();
        if (player.isOp() || player.hasPermission("furiousplacas.setaritem")) {
            String placaLoja = messageConfig.getCustomConfig().getString("placa.nomeLoja");
            Sign sign = (Sign) e.getBlock().getState();

            Inventory chooseItemInventory = Bukkit.getServer().createInventory(new ChooseItemInventoryHolder(), 9, ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "Escolha o item");

            if (!Utilidades.isLojaValidCreatingShop(e.getLines())) {
                return;
            }
            e.setLine(3, "Sem item");
            for (int i : new int[]{0, 1, 2, 3, 5, 6, 7, 8}) {
                chooseItemInventory.setItem(i, new ItemStack(Material.DIAMOND));
            }
            plugin.shopsInCreation.put(player.getUniqueId(), new CustomSign(sign));
            e.setLine(2, Utilidades.updatePriceSign(e.getLine(1)));
            e.setLine(1, e.getLine(0));
            e.setLine(0, placaLoja);

            player.openInventory(chooseItemInventory);
        }
    }

    public static void createSignLoja(Player player)
            throws CreateSignPlayerWithoutPermissionException, CreateSignServerWithoutPermissionException {

        if ((!player.hasPermission("loja.admin")) && (!player.hasPermission("loja.player")) && (!player.isOp())) {
            throw new CreateSignPlayerWithoutPermissionException("O player " + player.getName() + " tentou criar loja sem permissão.");
        }
        if ((!player.hasPermission("loja.admin")) && (!player.isOp())) {
            throw new CreateSignServerWithoutPermissionException("O player " + player.getName() + " tentou criar uma loja com o nome do servidor sem permissão.");
        }
    }
}
