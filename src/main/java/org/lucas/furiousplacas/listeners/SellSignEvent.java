package org.lucas.furiousplacas.listeners;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.lucas.furiousplacas.FuriousPlacas;
import org.lucas.furiousplacas.api.Utilidades;
import org.lucas.furiousplacas.configurations.DescontosConfig;
import org.lucas.furiousplacas.configurations.MessageConfig;
import org.lucas.furiousplacas.enums.LojaEnum;
import org.lucas.furiousplacas.exceptions.PlayerEqualsTargetException;
import org.lucas.furiousplacas.exceptions.PlayerUnknowItemException;
import org.lucas.furiousplacas.exceptions.SignUnknowSell;
import org.lucas.furiousplacas.handlers.LojaSellServer;
import org.lucas.furiousplacas.model.ChooseItemInventoryHolder;
import org.lucas.furiousplacas.model.CustomSign;
import org.lucas.furiousplacas.utils.Database;

public final class SellSignEvent implements Listener {

    private final Economy economy;
    private final MessageConfig messageConfig;
    private final FuriousPlacas plugin;
    private final DescontosConfig descontosConfig;
    private final Database database;

    public SellSignEvent(Economy economy, MessageConfig messageConfig, FuriousPlacas plugin, DescontosConfig descontosConfig, Database database) {
        this.economy = economy;
        this.messageConfig = messageConfig;
        this.plugin = plugin;
        this.descontosConfig = descontosConfig;
        this.database = database;
    }

    @EventHandler(ignoreCancelled = true)
    private void onComprar(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (e.getClickedBlock().getType() != Material.SIGN && e.getClickedBlock().getType() != Material.WALL_SIGN) {
            return;
        }
        Player player = e.getPlayer();
        Sign sign = (Sign) e.getClickedBlock().getState();
        if (!Utilidades.isLojaValidShopCreated(sign.getLines())) {
            return;
        }
        String placaLoja = messageConfig.getCustomConfig().getString("placa.nomeLoja");

        if (!Utilidades.replaceShopName(sign.getLine(0)).equals(placaLoja)) {
            return;
        }

        boolean agachadoLeftClick = player.isSneaking() && e.getAction() == Action.LEFT_CLICK_BLOCK;
        boolean agachadoRightClick = player.isSneaking() && e.getAction() == Action.RIGHT_CLICK_BLOCK;
        boolean criativoComPermissao = player.getGameMode().equals(GameMode.CREATIVE) && (player.isOp() || player.hasPermission("furiousplacas.setaritem"));
        boolean placaVazia = sign.getLine(3).equalsIgnoreCase("Sem item");


        if (agachadoLeftClick && criativoComPermissao && !placaVazia) {
            plugin.shopsInCreation.put(player.getUniqueId(), new CustomSign(sign));
            String id = sign.getLine(3).split("#")[1];
            database.removeCustomSign(id);
            sign.setLine(3, "Sem item");
            sign.update();
            e.setCancelled(true);
            return;
        }

        if (agachadoRightClick && criativoComPermissao && placaVazia) {
            Inventory chooseItemInventory = Bukkit.getServer().createInventory(new ChooseItemInventoryHolder(), 9, ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "Escolha o item para ser comercializado");
            for (int i : new int[]{0, 1, 2, 3, 5, 6, 7, 8}) {
                chooseItemInventory.setItem(i, new ItemStack(Material.DIAMOND));
            }
            player.openInventory(chooseItemInventory);

        }

        if (placaVazia) return;

        try {
            ItemStack item = Utilidades.getItemLoja(sign, database);
            venderPelaPlaca(player, sign, item);
        } catch (PlayerEqualsTargetException error1) {
            player.sendMessage(messageConfig.message("mensagens.vender_erro1"));
        } catch (PlayerUnknowItemException error2) {
            player.sendMessage(messageConfig.message("mensagens.vender_erro3"));
        } catch (SignUnknowSell error3) {
            player.sendMessage(messageConfig.message("mensagens.vender_erro5"));
        }
    }

    private void venderPelaPlaca(Player player, Sign sign, ItemStack item) throws PlayerEqualsTargetException, PlayerUnknowItemException, SignUnknowSell {
        if (Utilidades.replaceShopName(sign.getLine(0)).equals(player.getDisplayName())) {
            throw new PlayerEqualsTargetException("O jogador '" + player.getName() + "' está tentando vender para ele mesmo.");
        }

        double priceSaleWithoutDiscount = Utilidades.getPrices(LojaEnum.VENDER, sign);
        if (priceSaleWithoutDiscount == 0.0D) {
            throw new SignUnknowSell("A placa {x=" + sign.getLocation().getX() + ",y=" + sign.getLocation().getY() + ",z=" + sign.getLocation().getZ() + "} não tem opção para vender.");
        }

        double amoutItemPlayerHas = Utilidades.quantidadeItemInventory(player.getInventory(), item);
        if (amoutItemPlayerHas == 0) {
            throw new PlayerUnknowItemException("O jogador '" + player.getName() + "' está tentando vender um item que ele não tem no inventário.");
        }

        double qntItemPlaca = Integer.parseInt(Utilidades.replace(sign.getLine(1)));
        double amount = player.isSneaking() ? amoutItemPlayerHas : Math.min(amoutItemPlayerHas, qntItemPlaca);
        priceSaleWithoutDiscount = priceSaleWithoutDiscount * amount / qntItemPlaca;

        double priceSaleWithDiscount = Utilidades.getSellDiscount(player, priceSaleWithoutDiscount, descontosConfig);

        if (priceSaleWithDiscount > 0.0D) {
            String moneyFormatted = String.format("%.2f", priceSaleWithDiscount);
            player.sendMessage(this.messageConfig.message("mensagens.vender_success_sign", (int) amount, moneyFormatted));

            economy.depositPlayer(player, priceSaleWithDiscount);

            LojaSellServer eventBuy = new LojaSellServer(player, priceSaleWithDiscount, item, (int) amount);
            Bukkit.getServer().getPluginManager().callEvent(eventBuy);
        } else {
            String dinheiroFormatado = String.format("%.2f", priceSaleWithoutDiscount);
            player.sendMessage(this.messageConfig.message("mensagens.vender_success_sign", (int) amount, dinheiroFormatado));

            economy.depositPlayer(player, priceSaleWithoutDiscount);

            LojaSellServer eventBuy = new LojaSellServer(player, priceSaleWithoutDiscount, item, (int) amount);
            Bukkit.getServer().getPluginManager().callEvent(eventBuy);
        }
        item.setAmount((int) amount);
        player.getInventory().removeItem(item);
    }
}
