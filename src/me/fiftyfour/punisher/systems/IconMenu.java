package me.fiftyfour.punisher.systems;

import me.fiftyfour.punisher.bukkit.BukkitMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IconMenu implements Listener {

    private List<String> viewing = new ArrayList<>();
    private String name;
    private int size;
    private onClick click;
    private ItemStack[] items;

    public IconMenu(String name, int size, onClick click) {
        this.name = name;
        this.size = size * 9;
        items = new ItemStack[this.size];
        this.click = click;
        BukkitMain plugin = BukkitMain.getInstance();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    public void setSize(int size) {
        this.size = size * 9;
        items = Arrays.copyOf(items, this.size);
    }
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        for (Player p : this.getViewers())
            close(p);
    }
    public void open(Player p) {
        p.openInventory(getInventory(p));
        viewing.add(p.getName());
    }
    private Inventory getInventory(Player p) {
        Inventory inv = Bukkit.createInventory(p, size, name);
        for (int i = 0; i < items.length; i++)
            if (items[i] != null)
                inv.setItem(i, items[i]);
        return inv;
    }
    public void close(Player p) {
        if (p.getOpenInventory().getTitle().equals(name))
            p.closeInventory();
    }
    private List<Player> getViewers() {
        List<Player> viewers = new ArrayList<>();
        for (String s : viewing)
            viewers.add(Bukkit.getPlayer(s));
        return viewers;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (viewing.contains(event.getWhoClicked().getName())) {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            Row row = getRowFromSlot(event.getSlot());
            if (event.getCurrentItem() != null) {
                if (click.click(p, this, row, event.getSlot() - row.getRow() * 9, event.getCurrentItem()))
                    close(p);
            }
        }
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (viewing.contains(event.getPlayer().getName()))
            viewing.remove(event.getPlayer().getName());
    }
    public void addButton(int position, ItemStack item, String name, String... lore) {
        items[position] = getItem(item, name, lore);
    }
    private Row getRowFromSlot(int slot) {
        return new Row(slot / 9, items);
    }
    public Row getRow(int row) {
        return new Row(row, items);
    }
    private ItemStack getItem(ItemStack item, String name, String... lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }
    public interface onClick {
        boolean click(Player clicker, IconMenu menu, Row row, int slot, ItemStack item);
    }
    public class Row {
        int row;
        private ItemStack[] rowItems = new ItemStack[9];

        private Row(int row, ItemStack[] items) {
            try {
                this.row = row;
                int j = 0;
                for (int i = (row * 9); i < (row * 9) + 9; i++) {
                    rowItems[j] = items[i];
                    j++;
                }
            }catch (ArrayIndexOutOfBoundsException e){}
        }
        public int getRow() {
            return row;
        }
    }

}