package org.eagle.eagleshopfix;

import com.snowgears.shop.shop.*;
import net.lightshard.prisoncells.*;
import com.snowgears.shop.Shop;
import com.snowgears.shop.event.PlayerDestroyShopEvent;
import net.lightshard.prisoncells.cell.*;
import net.lightshard.prisoncells.cell.world.region.*;
import net.lightshard.prisoncells.event.*;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

import java.util.*;

public final class EagleShopFix extends JavaPlugin implements Listener {

    private Shop getShop() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Shop");
        // Shop may not be loaded
        if (plugin == null || !(plugin instanceof Shop)) {
            return null; // Maybe you want throw an exception instead
        }
        return (Shop) plugin;
    }

    //only allow players to break shops if they are holding an iron axe in their main hand
    @EventHandler
    public void onShopDestroy(PlayerDestroyShopEvent event){
        if (event.getPlayer().getItemInHand().getType() != Material.IRON_AXE) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You must break shop signs with an Iron Axe!");
        }
    }

    // Delete all shops in the unclaimed cell
    @EventHandler
    public void onCellUnclaim(CellUnclaimEvent event) {
        PrisonCells prisonCells = PrisonCells.getInstance();
        Shop shopPlugin = getShop();
        System.out.println("A cell has been unclaimed");
        if (prisonCells != null && shopPlugin != null) {
            Iterator<UUID> iterator = shopPlugin.getShopHandler().getShopOwnerUUIDs().iterator();
            while (iterator.hasNext()) {
                UUID ownerUUID = iterator.next();
                Iterator<AbstractShop> shopIterator = shopPlugin.getShopHandler().getShops(ownerUUID).iterator();

                while (shopIterator.hasNext()) {
                    AbstractShop shop = shopIterator.next();
                    if (shop != null && shop.getSignLocation() != null) {
                        if (isLocationWithinRegion(shop.getSignLocation(), (CuboidRegion) event.getCell().getRegion())) {
                            shop.delete();
                            shopPlugin.getShopHandler().saveShops(ownerUUID); // Save the changes
                            System.out.println("Shop owned by " + shop.getOwnerName() + " has been removed. In Cell " + event.getCell().getName());
                        }
                    }
                }
            }
        }
    }

    private boolean isLocationWithinRegion(Location location, CuboidRegion region) {
        World world = location.getWorld();
        if (world != null) {
            // Check if the location's world matches the region's world
            if (!world.getName().equals(region.getWorld().getName())) {
                return false;
            }

            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();

            // Check if the location is within the region's boundaries
            if (x >= region.getXMin() && x <= region.getXMax() &&
                    y >= region.getYMin() && y <= region.getYMax() &&
                    z >= region.getZMin() && z <= region.getZMax()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("Eagle plugin has started");
        getServer().getPluginManager().registerEvents(this,this);
    }
}
