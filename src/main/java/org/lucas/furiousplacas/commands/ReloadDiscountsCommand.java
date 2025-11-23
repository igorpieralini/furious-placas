package org.lucas.furiousplacas.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.lucas.furiousplacas.FuriousPlacas;
import org.lucas.furiousplacas.configurations.DescontosConfig;

public class ReloadDiscountsCommand implements CommandExecutor {

    private final FuriousPlacas furiousPlacas;
    private final DescontosConfig descontosConfig;


    public ReloadDiscountsCommand(FuriousPlacas furiousPlacas, DescontosConfig descontosConfig) {
        this.furiousPlacas = furiousPlacas;
        this.descontosConfig = descontosConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(player.isOp()) {
                descontosConfig.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "Arquivo de descontos recarregado.");
            }
        }
        return false;
    }

}
