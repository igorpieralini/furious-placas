package org.lucas.furiousplacas.api;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.lucas.furiousplacas.FuriousPlacas;
import org.lucas.furiousplacas.configurations.DescontosConfig;
import org.lucas.furiousplacas.enums.LojaEnum;
import org.lucas.furiousplacas.model.CustomSign;
import org.lucas.furiousplacas.utils.Database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Utilidades {
    public static Double getPrices(LojaEnum type, Sign sign) {
        String[] linePrice = replace(sign.getLine(2)).toLowerCase().split(":");
        if (type.equals(LojaEnum.COMPRAR)) {
            if (linePrice[0].contains("c")) {
                return Double.valueOf(linePrice[0].replace("c", ""));
            } else if (linePrice.length == 2) {
                return Double.valueOf(linePrice[1].replace("v", ""));
            }
        } else if (type.equals(LojaEnum.VENDER)) {
            if (linePrice[0].contains("v")) {
                return Double.valueOf(linePrice[0].replace("v", ""));
            } else if (linePrice.length == 2) {
                return Double.valueOf(linePrice[1].replace("v", ""));
            }
        }
        return 0.0D;
    }

    public static void setItemLoja(Player player, FuriousPlacas furiousPlacas) {
        CustomSign customSign = furiousPlacas.shopsInCreation.get(player.getUniqueId());
        furiousPlacas.shopsInCreation.remove(player.getUniqueId());
        furiousPlacas.createdShops.add(customSign);
    }

    public static ItemStack getItemLoja(Sign sign, Database database) {
        ItemStack itemStack = null;
        try {
            itemStack = database.getItemStack(sign);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itemStack;
    }

    public static boolean isLojaValidCreatingShop(String[] lines) {
        String amount = replace(lines[0]).toLowerCase();
        if (!amount.matches("^[1-9](\\d)*(\\s|$)")) {
            return false;
        }
        String price = replace(lines[1]).toLowerCase();
        return (price.matches("[cC]\\s*\\d+(\\.\\d+)?\\s*:\\s*[vV]\\s*\\d+(\\.\\d+)?")) ||
                (price.matches("c\\s*\\d+(\\.\\d+)?")) ||
                (price.matches("v\\s*\\d+(\\.\\d+)?"));
    }

    public static boolean isLojaValidShopCreated(String[] lines) {
        String amount = replace(lines[1]).toLowerCase();
        if (!amount.matches("^[1-9](\\d)*(\\s|$)")) {
            return false;
        }
        String price = replace(lines[2]).toLowerCase();
        return (price.matches("[cC]\\s*\\d+(\\.\\d+)?\\s*:\\s*[vV]\\s*\\d+(\\.\\d+)?")) ||
                (price.matches("c\\s*\\d+(\\.\\d+)?")) ||
                (price.matches("v\\s*\\d+(\\.\\d+)?"));
    }

    public static String replace(String price) {
        return price.replace(" ", "").replace("§2", "").replace("§4", "").replace("§0", "").replace("§l", "");
    }

    public static String replaceShopName(String price) {
        return price.replace("§0", "");
    }

    public static String updatePriceSign(String line) {
        if (replace(line).matches("[cC]\\s*\\d+(\\.\\d+)?\\s*:\\s*[vV]\\s*\\d+(\\.\\d+)?")) {
            return "§2§lC§r " + replace(line).split(":")[0].replace("C", "").replace("c", "") + " : §4§lV§r " + replace(line).split(":")[1].replace("V", "").replace("v", "");
        }
        if (replace(line).matches("[cC]\\s*\\d+(\\.\\d+)?")) {
            return "§2§lC§r " + replace(line).replace("C", "").replace("c", "");
        }
        return "§4§lV§r " + replace(line).replace("V", "").replace("v", "");
    }

    public static boolean haveSlotClearInv(Inventory inventory, ItemStack itemStack, int amount) {
        int quantidade = amount;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) {
                if (itemStack.getMaxStackSize() == 1) {
                    quantidade -= 1;
                } else {
                    quantidade -= 64;
                }
            } else if ((itemStack.isSimilar(item)) &&
                    (itemStack.getMaxStackSize() != 1)) {
                quantidade -= 64 - item.getAmount();
            }
        }
        return quantidade <= 0;
    }

    public static int quantidadeItemInventory(Inventory inventory, ItemStack itemStack) {
        int quantia = 0;
        for (ItemStack item : inventory.getContents()) {
            if ((item != null) && (item.isSimilar(itemStack))) {
                quantia += item.getAmount();
            }
        }
        return quantia;
    }


    public static Double getSellDiscount(Player player, Double price, DescontosConfig descontosConfig) {
        List<Map<String, Double>> vipsComDesconto = descontosConfig.getDescontosVenda();
        return getDiscount(vipsComDesconto, player, price, true);
    }

    public static Double getBuyDiscount(Player player, Double price, DescontosConfig descontosConfig) {
        List<Map<String, Double>> vipsComDesconto = descontosConfig.getDescontosCompra();
        return getDiscount(vipsComDesconto, player, price, false);
    }

    private static Double getDiscount(List<Map<String, Double>> vipsComDesconto, Player player, Double price, boolean venda) {
        Map<String, Long> vipsName = new HashMap<>();
        int size = vipsComDesconto.size() == 1 ? 1 : vipsComDesconto.size() - 1;
        for (int i = 0; i < size; i++) {
            vipsName.put(vipsComDesconto.get(i).keySet().toArray()[0].toString(), (long) i);
        }
        AtomicReference<Double> toReturn = new AtomicReference<>(0D);
        vipsName.forEach((vipName, index) -> {
            if (player.hasPermission("furiousPlacas." + vipName)) {
                Double desconto = vipsComDesconto.get(index.intValue()).get(vipName);
                if (venda) {
                    toReturn.set(price + (price * (desconto / 100L)));
                } else {
                    toReturn.set(price - (price * (desconto / 100L)));
                }
            } else {
                toReturn.set(price);
            }
        });
        return toReturn.get();
    }
}