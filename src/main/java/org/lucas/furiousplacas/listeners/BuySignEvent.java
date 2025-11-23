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
import org.lucas.furiousplacas.exceptions.InventoryFullException;
import org.lucas.furiousplacas.exceptions.PlayerEqualsTargetException;
import org.lucas.furiousplacas.exceptions.PlayerMoneyException;
import org.lucas.furiousplacas.exceptions.SignUnknowBuy;
import org.lucas.furiousplacas.handlers.LojaBuyServer;
import org.lucas.furiousplacas.model.ChooseItemInventoryHolder;
import org.lucas.furiousplacas.model.CustomSign;
import org.lucas.furiousplacas.utils.Database;

public final class BuySignEvent implements Listener {

    private final Economy economy;
    private final MessageConfig messageConfig;
    private final FuriousPlacas plugin;
    private final DescontosConfig descontosConfig;
    private final Database database;


    public BuySignEvent(Economy economy, MessageConfig messageConfig, FuriousPlacas plugin, DescontosConfig descontosConfig, Database database) {
        this.economy = economy;
        this.descontosConfig = descontosConfig;
        this.messageConfig = messageConfig;
        this.plugin = plugin;
        this.database = database;
    }

    @EventHandler(ignoreCancelled = true)
    private void onComprar(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (e.getClickedBlock().getType() != Material.SIGN
                && e.getClickedBlock().getType() != Material.WALL_SIGN) {
            return;
        }

        Sign sign = (Sign) e.getClickedBlock().getState();
        if (!Utilidades.isLojaValidShopCreated(sign.getLines())) {
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
            if (sign.getLine(3).equalsIgnoreCase("Sem item")) {
                Inventory chooseItemInventory = Bukkit.getServer().createInventory(new ChooseItemInventoryHolder(), 9, ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "Escolha o item para ser comercializado");
                for (int i : new int[]{0, 1, 2, 3, 5, 6, 7, 8}) {
                    chooseItemInventory.setItem(i, new ItemStack(Material.DIAMOND));
                }
                player.openInventory(chooseItemInventory);
            }
        }

        String placaLoja = messageConfig.getCustomConfig().getString("placa.nomeLoja");
        if (!Utilidades.replaceShopName(sign.getLine(0)).equals(placaLoja)) {
            return;
        }

        if (placaVazia) return;

        try {
            ItemStack item = Utilidades.getItemLoja(sign, database);
            comprarPelaPlaca(player, sign, item);
        } catch (PlayerEqualsTargetException error1) {
            player.sendMessage(messageConfig.message("mensagens.comprar_erro3"));
        } catch (SignUnknowBuy error2) {
            player.sendMessage(messageConfig.message("mensagens.comprar_erro4"));
        } catch (InventoryFullException error3) {
            player.sendMessage(messageConfig.message("mensagens.inventory_full"));
        } catch (PlayerMoneyException erro4) {
            player.sendMessage(messageConfig.message("mensagens.comprar_erro1"));
        }
    }

    private void comprarPelaPlaca(Player player, Sign placa, ItemStack item)
            throws PlayerMoneyException, SignUnknowBuy, InventoryFullException, PlayerEqualsTargetException {
        double priceBuy = Utilidades.getPrices(LojaEnum.COMPRAR, placa);
        if (priceBuy == 0.0D) {
            throw new SignUnknowBuy("A placa {x=" + placa.getLocation().getX() + ",y=" + placa.getLocation().getY() + ",z=" + placa.getLocation().getZ() + "} não tem opção para comprar.");
        }
        if (Utilidades.replaceShopName(placa.getLine(0)).equals(player.getDisplayName())) {
            throw new PlayerEqualsTargetException("O jogador '" + player.getName() + "' está tentando comprar dele mesmo.");
        }
        int amountItemSign = player.isSneaking() ? 64 : Short.parseShort(Utilidades.replace(placa.getLine(1)));
        if (!Utilidades.haveSlotClearInv(player.getInventory(), item, amountItemSign)) {
            throw new InventoryFullException("Inventário do jogador está lotado e não tem como receber os itens.");
        }

        Double priceWithDiscount = Utilidades.getBuyDiscount(player, priceBuy, descontosConfig);

        if (economy.getBalance(player) < priceWithDiscount) {
            throw new PlayerMoneyException("O jogador '" + player.getName() + "' não tem dinheiro suficiente para fazer a compra.");
        }

        String moneyFormatted = String.format("%.2f", priceWithDiscount);
        player.sendMessage(this.messageConfig.message("mensagens.comprar_success_sign", amountItemSign, moneyFormatted));

        economy.withdrawPlayer(player, priceWithDiscount);

        item.setAmount(amountItemSign);
        player.getInventory().addItem(item);

        LojaBuyServer eventBuy = new LojaBuyServer(player, priceWithDiscount, item, amountItemSign);
        Bukkit.getServer().getPluginManager().callEvent(eventBuy);
    }
}
