package org.lucas.furiousplacas.configurations;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.lucas.furiousplacas.FuriousPlacas;

public final class MessageConfig extends Config {
    public MessageConfig(FuriousPlacas plugin, String fileName) {
        super(plugin, fileName);
    }

    public String message(String string) {
        return colorText(getCustomConfig().getString(string));
    }


    public String colorText(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public String message(String string, String money) {
        return colorText(getCustomConfig().getString(string).replace("%m", money != null ? money : ""));
    }

    public String message(String string, Integer itemQuantia) {
        return colorText(
                getCustomConfig().getString(string).replace("%i", itemQuantia != null ? itemQuantia.toString() : ""));
    }

    public String message(String string, Integer itemQuantia, String money) {
        return colorText(
                getCustomConfig().getString(string).replace("%i", itemQuantia != null ? itemQuantia.toString() : "")
                        .replace("%m", money != null ? money : ""));
    }

    public String message(String string, Integer itemQuantia, String money, OfflinePlayer target) {
        return colorText(
                getCustomConfig().getString(string).replace("%i", itemQuantia != null ? itemQuantia.toString() : "")
                        .replace("%m", money != null ? money : "")
                        .replace("%p", target != null ? target.getName() : ""));
    }
}