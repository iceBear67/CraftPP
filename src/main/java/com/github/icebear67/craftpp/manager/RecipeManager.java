package com.github.icebear67.craftpp.manager;

import com.github.icebear67.craftpp.CraftPP;
import com.github.icebear67.craftpp.api.machine.AbstractMachine;
import com.github.icebear67.craftpp.data.Metadata;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class RecipeManager implements Listener {
    @Getter
    private static final RecipeManager instance = new RecipeManager();
    private final HashMap<Integer, Class<? extends AbstractMachine>> items = new HashMap<>();

    private RecipeManager() {
    }

    public void register(Recipe recipe, Class<? extends AbstractMachine> machine) {
        items.put(recipe.getResult().hashCode(), machine);
    }

    @EventHandler
    private void onCraft(CraftItemEvent event) {
        if (items.containsKey(event.getRecipe().getResult().hashCode())) {
            ItemStack target = event.getRecipe().getResult();
            if (target.getAmount() > 1) {
                if (!CraftPP.getConf().allowStackCraft) {
                    event.setCancelled(true);
                    return;
                }
            }
            try {
                AbstractMachine machine = items.get(event.getRecipe().getResult().hashCode()).getDeclaredConstructor().newInstance();
                MachineManager.getInstance().registerMachine(machine);
                NBTItem nbtItem = new NBTItem(target);
                nbtItem.setString(Metadata.UUID.toString(), machine.getUUID().toString());
                event.setCurrentItem(nbtItem.getItem());
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
