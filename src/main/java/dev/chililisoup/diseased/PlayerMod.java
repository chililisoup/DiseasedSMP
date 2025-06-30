package dev.chililisoup.diseased;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PlayerMod {
    public static void addAttribute(Player player, Holder<Attribute> attribute, String name, double value, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        instance.addOrReplacePermanentModifier(new AttributeModifier(
                Diseased.loc(name),
                value,
                operation
        ));
    }

    public static void removeAttribute(Player player, Holder<Attribute> attribute, String name) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) instance.removeModifier(Diseased.loc(name));
    }

    public static void replaceSlots(Player player, ItemStack item, int startSlot, int endSlot) {
        Inventory inventory = player.getInventory();

        for (int i = startSlot; i < endSlot; i++) {
            ItemStack replaced = inventory.removeItem(i, 99);
            player.drop(replaced, true, false);
            inventory.setItem(i, item);
        }
    }

    public static void clearSlots(Player player, int startSlot, int endSlot) {
        Inventory inventory = player.getInventory();

        for (int i = startSlot; i < endSlot; i++)
            inventory.removeItemNoUpdate(i);
    }
}
