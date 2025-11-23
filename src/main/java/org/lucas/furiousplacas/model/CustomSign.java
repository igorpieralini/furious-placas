package org.lucas.furiousplacas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Random;


@Data
public class CustomSign {
    private Sign sign;
    private ItemStack item;

    public CustomSign(Sign sign) {
        this.sign = sign;
    }
}
