package com.tradeplugin;
 
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TradeGUI {

    // Layout (6 rows = 54 slots):
    // Rows 1-2  (0-17):  your items (18 slots)
    // Row  3    (18-26): divider (black glass)
    // Rows 4-5  (27-44): opponent items (18 slots, readonly)
    // Row  6    (45-53): cancel=45, gray=46-52, confirm=53

    public static final int TRADE_SIZE = 54;
    public static final int CANCEL_SLOT = 45;
    public static final int CONFIRM_SLOT = 53;

    public static Inventory createTradeInventory() {
        Inventory inv = Bukkit.createInventory(null, TRADE_SIZE,
                Component.text("Трейд", NamedTextColor.DARK_AQUA));

        // Divider row
        for (int i = 18; i <= 26; i++) inv.setItem(i, makeDivider());

        // Opponent section placeholders
        for (int i = 27; i <= 44; i++) inv.setItem(i, makeEmptySlot());

        // Control row
        for (int i = 45; i <= 53; i++) inv.setItem(i, makeGlass());
        inv.setItem(CANCEL_SLOT, makeRedGlass());
        inv.setItem(CONFIRM_SLOT, makeGreenGlass());

        return inv;
    }

    public static Inventory createPreviewInventory() {
        Inventory inv = Bukkit.createInventory(null, TRADE_SIZE,
                Component.text("Підтвердження трейду", NamedTextColor.GOLD));

        for (int i = 18; i <= 26; i++) inv.setItem(i, makeDivider());
        for (int i = 27; i <= 44; i++) inv.setItem(i, makeEmptySlot());
        for (int i = 45; i <= 53; i++) inv.setItem(i, makeGlass());
        inv.setItem(CANCEL_SLOT, makeRedGlass());
        inv.setItem(CONFIRM_SLOT, makeGreenGlass());

        return inv;
    }

    public static void fillPreview(Inventory preview, ItemStack[] myItems, ItemStack[] theirItems) {
        for (int i = 0; i < 18; i++) {
            ItemStack mine = (i < myItems.length) ? myItems[i] : null;
            ItemStack theirs = (i < theirItems.length) ? theirItems[i] : null;
            preview.setItem(i, mine != null ? mine.clone() : makeEmptySlot());
            preview.setItem(27 + i, theirs != null ? theirs.clone() : makeEmptySlot());
        }
    }

    public static void syncOpponentSection(Inventory inv, ItemStack[] opponentItems) {
        for (int i = 0; i < 18; i++) {
            ItemStack item = (i < opponentItems.length) ? opponentItems[i] : null;
            inv.setItem(27 + i, item != null ? item.clone() : makeEmptySlot());
        }
    }

    public static ItemStack[] getPlayerItems(Inventory inv) {
        ItemStack[] items = new ItemStack[18];
        for (int i = 0; i < 18; i++) {
            ItemStack item = inv.getItem(i);
            items[i] = (item != null && item.getType() != Material.AIR) ? item : null;
        }
        return items;
    }

    public static void markConfirmed(Inventory inv) {
        ItemStack waiting = makeGlass();
        ItemMeta meta = waiting.getItemMeta();
        meta.displayName(Component.text("Очікування...", NamedTextColor.YELLOW));
        waiting.setItemMeta(meta);
        inv.setItem(CONFIRM_SLOT, waiting);
    }

    public static void restoreConfirmButton(Inventory inv) {
        inv.setItem(CONFIRM_SLOT, makeGreenGlass());
    }

    // Slots 0-17: player's own items
    public static boolean isPlayerItemSlot(int slot) {
        return slot >= 0 && slot <= 17;
    }

    // Everything except player item slots and the two buttons
    public static boolean isLockedSlot(int slot) {
        return (slot >= 18 && slot <= 44) || (slot >= 46 && slot <= 52);
    }

    public static boolean isCancelSlot(int slot) { return slot == CANCEL_SLOT; }
    public static boolean isConfirmSlot(int slot) { return slot == CONFIRM_SLOT; }

    public static ItemStack makeRedGlass() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Закрити трейд", NamedTextColor.RED));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeGreenGlass() {
        ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Підтвердити", NamedTextColor.GREEN));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeGlass() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeDivider() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeEmptySlot() {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }
}
