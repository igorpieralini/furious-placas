package org.lucas.furiousplacas.listeners;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.lucas.furiousplacas.api.Utilidades;
import org.lucas.furiousplacas.configurations.MessageConfig;

public final class EventBreakShop implements Listener {

    private final MessageConfig messageConfig;
    private final Plugin plugin;

    public EventBreakShop(MessageConfig messageConfig, Plugin plugin) {
        this.messageConfig = messageConfig;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void signBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (e.getBlock().getType() != Material.WALL_SIGN) {
            return;
        }

        Sign sign = (Sign) e.getBlock().getState();
        if (!Utilidades.isLojaValidShopCreated(sign.getLines())) {
            return;
        }

        if (e.getPlayer().hasPermission("furiousplacas.quebrarloja") || e.getPlayer().isOp()) {
            e.setCancelled(false);
            return;
        }
        e.getPlayer().sendMessage(
                messageConfig
                        .message("mensagens.break_chest_shop")
                        .replace("%p", sign.getLine(0)));

        e.setCancelled(true);
    }
}
