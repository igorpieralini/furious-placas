package org.lucas.furiousplacas;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.lucas.furiousplacas.commands.ReloadDiscountsCommand;
import org.lucas.furiousplacas.configurations.DatabaseConfig;
import org.lucas.furiousplacas.configurations.DescontosConfig;
import org.lucas.furiousplacas.configurations.MessageConfig;
import org.lucas.furiousplacas.listeners.*;
import org.lucas.furiousplacas.model.CustomSign;
import org.lucas.furiousplacas.utils.Database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@Getter
public final class FuriousPlacas extends JavaPlugin {
    private Runnable onDisable;
    private Economy economy = null;
    public HashMap<UUID, CustomSign> shopsInCreation = new HashMap<>();
    public ArrayList<CustomSign> createdShops = new ArrayList<>();
    private Database db;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        final MessageConfig messageConfig = new MessageConfig(this, "configurar.yml");
        final DescontosConfig descontosConfig = new DescontosConfig(this, "descontos.yml");
        final DatabaseConfig databaseConfig = new DatabaseConfig(this, "database.yml");
        setupDatabase(databaseConfig);

        this.onDisable = () -> {
            descontosConfig.reloadConfig();
            messageConfig.reloadConfig();
            databaseConfig.reloadConfig();
            db.disconnect();
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "[FuriousPlacas] desativado com sucesso!");
        };

        Bukkit.getPluginManager().registerEvents(new BuySignEvent(economy, messageConfig, this, descontosConfig, db), this);
        Bukkit.getPluginManager().registerEvents(new CreateShopEvent(economy, messageConfig, this), this);
        Bukkit.getPluginManager().registerEvents(new EventBreakShop(messageConfig, this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerShopEvent(messageConfig), this);
        Bukkit.getPluginManager().registerEvents(new SellSignEvent(economy, messageConfig, this, descontosConfig, db), this);
        Bukkit.getPluginManager().registerEvents(new CloseChooseItemInventoryEvent(messageConfig, this, db), this);

        getCommand("rldescontos").setExecutor(new ReloadDiscountsCommand(this, descontosConfig));
        getServer().getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[FuriousPlacas] Plugin ativado com sucesso!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public void setupDatabase(DatabaseConfig databaseConfig) {
        db = new Database(
                databaseConfig.getHost(),
                databaseConfig.getPort(),
                databaseConfig.getDatabaseName(),
                databaseConfig.getUser(),
                databaseConfig.getPassword()
        );

        try {
            db.connect();
        } catch (SQLException e) {
            getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[FuriousPlacas] A conexão com o banco de dados falhou!");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        onDisable.run();
    }
}