package dev.chililisoup.diseased.reg;

import dev.chililisoup.diseased.Diseased;
import dev.chililisoup.diseased.item.SlotBlocker;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {
    public static final Item SLOT_BLOCKER = register("slot_blocker", SlotBlocker::new);

    public static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties properties) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Diseased.loc(name));
        Item item = itemFactory.apply(properties.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    public static Item register(String name, Function<Item.Properties, Item> itemFactory) {
        return register(name, itemFactory, new Item.Properties());
    }

    public static void noop() {}
}
