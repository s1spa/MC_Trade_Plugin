package com.tradeplugin;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class TradeSession {

    public enum State {
        PENDING,
        ACTIVE,
        CONFIRMING,
        PREVIEW,
        DONE
    }

    private final UUID requesterId;
    private final UUID targetId;
    private State state = State.PENDING;

    private Inventory requesterTradeInv;
    private Inventory targetTradeInv;

    private boolean requesterConfirmedTrade = false;
    private boolean targetConfirmedTrade = false;

    private Inventory requesterPreviewInv;
    private Inventory targetPreviewInv;

    private boolean requesterConfirmedPreview = false;
    private boolean targetConfirmedPreview = false;

    private int timeoutTaskId = -1;

    public TradeSession(UUID requesterId, UUID targetId) {
        this.requesterId = requesterId;
        this.targetId = targetId;
    }

    public UUID getRequesterId() { return requesterId; }
    public UUID getTargetId() { return targetId; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public Inventory getRequesterTradeInv() { return requesterTradeInv; }
    public void setRequesterTradeInv(Inventory inv) { this.requesterTradeInv = inv; }
    public Inventory getTargetTradeInv() { return targetTradeInv; }
    public void setTargetTradeInv(Inventory inv) { this.targetTradeInv = inv; }

    public boolean isRequesterConfirmedTrade() { return requesterConfirmedTrade; }
    public void setRequesterConfirmedTrade(boolean v) { this.requesterConfirmedTrade = v; }
    public boolean isTargetConfirmedTrade() { return targetConfirmedTrade; }
    public void setTargetConfirmedTrade(boolean v) { this.targetConfirmedTrade = v; }

    public Inventory getRequesterPreviewInv() { return requesterPreviewInv; }
    public void setRequesterPreviewInv(Inventory inv) { this.requesterPreviewInv = inv; }
    public Inventory getTargetPreviewInv() { return targetPreviewInv; }
    public void setTargetPreviewInv(Inventory inv) { this.targetPreviewInv = inv; }

    public boolean isRequesterConfirmedPreview() { return requesterConfirmedPreview; }
    public void setRequesterConfirmedPreview(boolean v) { this.requesterConfirmedPreview = v; }
    public boolean isTargetConfirmedPreview() { return targetConfirmedPreview; }
    public void setTargetConfirmedPreview(boolean v) { this.targetConfirmedPreview = v; }

    public int getTimeoutTaskId() { return timeoutTaskId; }
    public void setTimeoutTaskId(int id) { this.timeoutTaskId = id; }

    public boolean involves(UUID uuid) {
        return requesterId.equals(uuid) || targetId.equals(uuid);
    }

    public boolean isRequester(Player player) {
        return requesterId.equals(player.getUniqueId());
    }

    public UUID getPartnerOf(UUID uuid) {
        return requesterId.equals(uuid) ? targetId : requesterId;
    }

    public Inventory getTradeInvOf(UUID uuid) {
        return requesterId.equals(uuid) ? requesterTradeInv : targetTradeInv;
    }

    public Inventory getTradeInvOfPartner(UUID uuid) {
        return requesterId.equals(uuid) ? targetTradeInv : requesterTradeInv;
    }

    public Inventory getPreviewInvOf(UUID uuid) {
        return requesterId.equals(uuid) ? requesterPreviewInv : targetPreviewInv;
    }

    public boolean hasConfirmedTrade(UUID uuid) {
        return requesterId.equals(uuid) ? requesterConfirmedTrade : targetConfirmedTrade;
    }

    public void setConfirmedTrade(UUID uuid, boolean value) {
        if (requesterId.equals(uuid)) requesterConfirmedTrade = value;
        else targetConfirmedTrade = value;
    }

    public boolean hasConfirmedPreview(UUID uuid) {
        return requesterId.equals(uuid) ? requesterConfirmedPreview : targetConfirmedPreview;
    }

    public void setConfirmedPreview(UUID uuid, boolean value) {
        if (requesterId.equals(uuid)) requesterConfirmedPreview = value;
        else targetConfirmedPreview = value;
    }

    public boolean bothConfirmedTrade() {
        return requesterConfirmedTrade && targetConfirmedTrade;
    }

    public boolean bothConfirmedPreview() {
        return requesterConfirmedPreview && targetConfirmedPreview;
    }
}
