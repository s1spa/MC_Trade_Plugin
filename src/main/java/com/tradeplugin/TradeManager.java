package com.tradeplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TradeManager {

    private static final int REQUEST_TIMEOUT_TICKS = 600; // 30 секунд

    private final TradePlugin plugin;
    private final Map<UUID, TradeSession> activeSessions = new HashMap<>();
    private final Map<UUID, UUID> pendingRequests = new HashMap<>(); // target -> requester

    public TradeManager(TradePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasPendingRequest(UUID target) {
        return pendingRequests.containsKey(target);
    }

    public UUID getPendingRequester(UUID target) {
        return pendingRequests.get(target);
    }

    public boolean isInSession(UUID uuid) {
        return activeSessions.containsKey(uuid);
    }

    public TradeSession getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }

    public boolean sendRequest(Player requester, Player target) {
        if (isInSession(requester.getUniqueId()) || isInSession(target.getUniqueId())) return false;
        if (hasPendingRequest(target.getUniqueId())) return false;

        pendingRequests.put(target.getUniqueId(), requester.getUniqueId());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(target.getUniqueId())) {
                pendingRequests.remove(target.getUniqueId());
                requester.sendMessage(Component.text("Запит на трейд з ", NamedTextColor.GRAY)
                        .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" закінчився.", NamedTextColor.GRAY)));
                if (target.isOnline()) {
                    target.sendMessage(Component.text("Запит на трейд від ", NamedTextColor.GRAY)
                            .append(Component.text(requester.getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" закінчився.", NamedTextColor.GRAY)));
                }
            }
        }, REQUEST_TIMEOUT_TICKS);

        return true;
    }

    public TradeSession acceptRequest(Player target) {
        UUID requesterId = pendingRequests.remove(target.getUniqueId());
        if (requesterId == null) return null;

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null) return null;

        TradeSession session = new TradeSession(requesterId, target.getUniqueId());
        session.setState(TradeSession.State.ACTIVE);

        Inventory reqInv = TradeGUI.createTradeInventory();
        Inventory tgtInv = TradeGUI.createTradeInventory();
        session.setRequesterTradeInv(reqInv);
        session.setTargetTradeInv(tgtInv);

        activeSessions.put(requesterId, session);
        activeSessions.put(target.getUniqueId(), session);

        requester.openInventory(reqInv);
        target.openInventory(tgtInv);

        return session;
    }

    public void declineRequest(Player target) {
        UUID requesterId = pendingRequests.remove(target.getUniqueId());
        if (requesterId == null) return;

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null) {
            requester.sendMessage(Component.text(target.getName(), NamedTextColor.YELLOW)
                    .append(Component.text(" відхилив запит на трейд.", NamedTextColor.RED)));
        }
        target.sendMessage(Component.text("Ви відхилили запит на трейд.", NamedTextColor.RED));
    }

    // Sync the other player's opponent section with this player's current items
    public void syncPartner(TradeSession session, UUID whoChanged) {
        if (session.getState() != TradeSession.State.ACTIVE) return;

        Inventory myInv = session.getTradeInvOf(whoChanged);
        UUID partnerId = session.getPartnerOf(whoChanged);
        Inventory partnerInv = session.getTradeInvOf(partnerId);

        ItemStack[] myItems = TradeGUI.getPlayerItems(myInv);
        TradeGUI.syncOpponentSection(partnerInv, myItems);
    }

    // If partner had confirmed but I changed my items — reset their confirmation
    public void resetPartnerConfirmIfNeeded(TradeSession session, UUID whoChanged) {
        if (session.getState() != TradeSession.State.ACTIVE) return;

        UUID partnerId = session.getPartnerOf(whoChanged);
        if (!session.hasConfirmedTrade(partnerId)) return;

        session.setConfirmedTrade(partnerId, false);
        Inventory partnerInv = session.getTradeInvOf(partnerId);
        TradeGUI.restoreConfirmButton(partnerInv);

        Player partner = Bukkit.getPlayer(partnerId);
        if (partner != null) {
            partner.sendMessage(Component.text("Інший гравець змінив речі. Підтвердіть знову.", NamedTextColor.YELLOW));
        }
    }

    public void handleConfirmTrade(Player player) {
        TradeSession session = activeSessions.get(player.getUniqueId());
        if (session == null || session.getState() != TradeSession.State.ACTIVE) return;

        session.setConfirmedTrade(player.getUniqueId(), true);
        TradeGUI.markConfirmed(session.getTradeInvOf(player.getUniqueId()));

        player.sendMessage(Component.text("Ви підтвердили. Очікуєте на іншого гравця...", NamedTextColor.YELLOW));

        if (session.bothConfirmedTrade()) {
            openPreviewForBoth(session);
        }
    }

    private void openPreviewForBoth(TradeSession session) {
        session.setState(TradeSession.State.PREVIEW);

        Player requester = Bukkit.getPlayer(session.getRequesterId());
        Player target = Bukkit.getPlayer(session.getTargetId());
        if (requester == null || target == null) {
            cancelSession(session);
            return;
        }

        ItemStack[] reqItems = TradeGUI.getPlayerItems(session.getRequesterTradeInv());
        ItemStack[] tgtItems = TradeGUI.getPlayerItems(session.getTargetTradeInv());

        Inventory reqPreview = TradeGUI.createPreviewInventory();
        TradeGUI.fillPreview(reqPreview, reqItems, tgtItems);

        Inventory tgtPreview = TradeGUI.createPreviewInventory();
        TradeGUI.fillPreview(tgtPreview, tgtItems, reqItems);

        session.setRequesterPreviewInv(reqPreview);
        session.setTargetPreviewInv(tgtPreview);

        requester.openInventory(reqPreview);
        target.openInventory(tgtPreview);

        requester.sendMessage(Component.text("Перегляньте трейд та підтвердіть.", NamedTextColor.GREEN));
        target.sendMessage(Component.text("Перегляньте трейд та підтвердіть.", NamedTextColor.GREEN));
    }

    public void handleConfirmPreview(Player player) {
        TradeSession session = activeSessions.get(player.getUniqueId());
        if (session == null || session.getState() != TradeSession.State.PREVIEW) return;

        session.setConfirmedPreview(player.getUniqueId(), true);

        Inventory previewInv = session.getPreviewInvOf(player.getUniqueId());
        TradeGUI.markConfirmed(previewInv);

        player.sendMessage(Component.text("Ви підтвердили. Очікуєте на іншого гравця...", NamedTextColor.YELLOW));

        if (session.bothConfirmedPreview()) {
            completeTrade(session);
        }
    }

    private void completeTrade(TradeSession session) {
        session.setState(TradeSession.State.DONE);

        Player requester = Bukkit.getPlayer(session.getRequesterId());
        Player target = Bukkit.getPlayer(session.getTargetId());

        ItemStack[] reqItems = TradeGUI.getPlayerItems(session.getRequesterTradeInv());
        ItemStack[] tgtItems = TradeGUI.getPlayerItems(session.getTargetTradeInv());

        activeSessions.remove(session.getRequesterId());
        activeSessions.remove(session.getTargetId());

        if (requester != null) {
            requester.closeInventory();
            for (ItemStack item : tgtItems) {
                if (item != null) {
                    requester.getInventory().addItem(item).values().forEach(leftover ->
                            requester.getWorld().dropItem(requester.getLocation(), leftover));
                }
            }
            requester.sendMessage(Component.text("Трейд завершено!", NamedTextColor.GREEN));
        }

        if (target != null) {
            target.closeInventory();
            for (ItemStack item : reqItems) {
                if (item != null) {
                    target.getInventory().addItem(item).values().forEach(leftover ->
                            target.getWorld().dropItem(target.getLocation(), leftover));
                }
            }
            target.sendMessage(Component.text("Трейд завершено!", NamedTextColor.GREEN));
        }
    }

    public void cancelSession(Player player) {
        TradeSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        UUID partnerId = session.getPartnerOf(player.getUniqueId());
        Player partner = Bukkit.getPlayer(partnerId);
        if (partner != null) {
            partner.sendMessage(Component.text(player.getName(), NamedTextColor.YELLOW)
                    .append(Component.text(" скасував трейд.", NamedTextColor.RED)));
        }
        player.sendMessage(Component.text("Трейд скасовано.", NamedTextColor.RED));

        cancelSession(session);
    }

    public void cancelSession(TradeSession session) {
        if (session.getState() == TradeSession.State.DONE) return;
        session.setState(TradeSession.State.DONE);

        activeSessions.remove(session.getRequesterId());
        activeSessions.remove(session.getTargetId());

        Player requester = Bukkit.getPlayer(session.getRequesterId());
        Player target = Bukkit.getPlayer(session.getTargetId());

        returnItems(requester, session.getRequesterTradeInv());
        returnItems(target, session.getTargetTradeInv());

        if (requester != null) requester.closeInventory();
        if (target != null) target.closeInventory();
    }

    private void returnItems(Player player, Inventory inv) {
        if (player == null || inv == null) return;
        for (int i = 0; i < 18; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                player.getInventory().addItem(item).values().forEach(leftover ->
                        player.getWorld().dropItem(player.getLocation(), leftover));
            }
        }
    }

    public void removeRequest(UUID target) {
        pendingRequests.remove(target);
    }

    public TradePlugin getPlugin() {
        return plugin;
    }
}
