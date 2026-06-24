package com.tradeplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class TradeListener implements Listener {

    private final TradeManager manager;

    public TradeListener(TradeManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        TradeSession session = manager.getSession(uuid);
        if (session == null) return;

        Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;

        int slot = event.getSlot();

        if (session.getState() == TradeSession.State.ACTIVE) {
            Inventory tradeInv = session.getTradeInvOf(uuid);
            boolean inTradeInv = clicked.equals(tradeInv);

            if (inTradeInv) {
                // Always block locked slots (divider, border, opponent section, gray fill)
                if (TradeGUI.isLockedSlot(slot)) {
                    event.setCancelled(true);
                    return;
                }
                // Cancel button
                if (TradeGUI.isCancelSlot(slot)) {
                    event.setCancelled(true);
                    manager.cancelSession(player);
                    return;
                }
                // Confirm button
                if (TradeGUI.isConfirmSlot(slot)) {
                    event.setCancelled(true);
                    if (!session.hasConfirmedTrade(uuid)) {
                        manager.handleConfirmTrade(player);
                    }
                    return;
                }
                // Player item slot — block if already confirmed
                if (TradeGUI.isPlayerItemSlot(slot) && session.hasConfirmedTrade(uuid)) {
                    event.setCancelled(true);
                    return;
                }
            }

            // Schedule sync after item placement (1 tick delay so the item move finishes first)
            Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
                if (session.getState() == TradeSession.State.ACTIVE) {
                    manager.syncPartner(session, uuid);
                    manager.resetPartnerConfirmIfNeeded(session, uuid);
                }
            }, 1L);

        } else if (session.getState() == TradeSession.State.PREVIEW) {
            Inventory previewInv = session.getPreviewInvOf(uuid);
            if (!clicked.equals(previewInv)) return;

            event.setCancelled(true);

            if (TradeGUI.isCancelSlot(slot)) {
                manager.cancelSession(player);
            } else if (TradeGUI.isConfirmSlot(slot)) {
                if (!session.hasConfirmedPreview(uuid)) {
                    manager.handleConfirmPreview(player);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        TradeSession session = manager.getSession(player.getUniqueId());
        if (session == null) return;

        if (session.getState() == TradeSession.State.ACTIVE) {
            Inventory tradeInv = session.getTradeInvOf(player.getUniqueId());

            for (int slot : event.getRawSlots()) {
                // rawSlots >= inventory size = player's own inventory rows (safe)
                if (slot >= tradeInv.getSize()) continue;

                if (TradeGUI.isLockedSlot(slot)
                        || TradeGUI.isCancelSlot(slot)
                        || TradeGUI.isConfirmSlot(slot)
                        || (TradeGUI.isPlayerItemSlot(slot) && session.hasConfirmedTrade(player.getUniqueId()))) {
                    event.setCancelled(true);
                    return;
                }
            }

            Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
                if (session.getState() == TradeSession.State.ACTIVE) {
                    manager.syncPartner(session, player.getUniqueId());
                    manager.resetPartnerConfirmIfNeeded(session, player.getUniqueId());
                }
            }, 1L);

        } else if (session.getState() == TradeSession.State.PREVIEW) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        // New inventory opening replaces old one — not a real close
        if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

        UUID uuid = player.getUniqueId();
        TradeSession session = manager.getSession(uuid);
        if (session == null || session.getState() == TradeSession.State.DONE) return;

        Inventory closed = event.getInventory();
        boolean isTradeInv = closed.equals(session.getTradeInvOf(uuid));
        boolean isPreviewInv = session.getState() == TradeSession.State.PREVIEW
                && closed.equals(session.getPreviewInvOf(uuid));

        if (isTradeInv || isPreviewInv) {
            manager.cancelSession(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        manager.removeRequest(uuid);

        TradeSession session = manager.getSession(uuid);
        if (session != null && session.getState() != TradeSession.State.DONE) {
            manager.cancelSession(session);
        }
    }
}
