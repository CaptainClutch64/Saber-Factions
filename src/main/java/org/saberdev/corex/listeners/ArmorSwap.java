package org.saberdev.corex.listeners;

import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.saberdev.corex.CoreX;

public class ArmorSwap implements Listener {

    public boolean isArmor(Material material) {
        String matName = material.toString().toLowerCase();
        return material != null
                && material != Material.AIR
                && (matName.contains("helmet")
                || matName.contains("chestplate")
                || matName.contains("leggings")
                || matName.contains("boots"));
    }

    public void equipArmor(Player player, ItemStack item) {
        if(item == null || item.getType() == Material.AIR) return;

        String name = item.getType().toString().toLowerCase();
        PlayerInventory inv = player.getInventory();

        ItemStack oldArmor = null;

        if(name.contains("helmet")) {
            oldArmor = inv.getHelmet();
            inv.setHelmet(item);
        } else if(name.contains("chestplate")) {
            oldArmor = inv.getChestplate();
            inv.setChestplate(item);
        } else if(name.contains("leggings")) {
            oldArmor = inv.getLeggings();
            inv.setLeggings(item);
        } else if(name.contains("boots")) {
            oldArmor = inv.getBoots();
            inv.setBoots(item);
        }

        if (oldArmor != null && oldArmor.getType() != Material.AIR) {
            inv.setItemInHand(oldArmor);
        } else {
            inv.setItemInHand(new ItemStack(Material.AIR));
        }

        player.updateInventory();
    }

    @EventHandler
    public void onArmorSwap(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (e.getPlayer().getItemInHand() == null || !isArmor(e.getPlayer().getItemInHand().getType()))
                return;

            if (e.hasBlock()
                    && (e.getClickedBlock().getState() instanceof org.bukkit.block.Chest
                    || e.getClickedBlock().getState() instanceof org.bukkit.block.Dispenser
                    || e.getClickedBlock().getState() instanceof org.bukkit.block.Dropper
                    || e.getClickedBlock().getState() instanceof org.bukkit.block.Furnace
                    || e.getClickedBlock().getState() instanceof org.bukkit.block.Hopper
                    || e.getClickedBlock().getType() == Material.GOLD_BLOCK
                    || e.getClickedBlock().getType() == Material.IRON_BLOCK))
                return;
            Player player = e.getPlayer();
            if (player.hasMetadata("lastArmorSwap")) {
                long dif = System.currentTimeMillis() - player.getMetadata("lastArmorSwap").get(0).asLong();
                if (dif < CoreX.getConfig().getConfig().getLong("Armor-Swap-Cooldown")) return;
            }

            if (e.isCancelled() && player.hasMetadata("noArmorSwap")) return;
            e.setCancelled(true);
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
            player.setMetadata("lastArmorSwap", new FixedMetadataValue(FactionsPlugin.getInstance(), System.currentTimeMillis()));
            equipArmor(player, e.getItem());
        }
    }
}
