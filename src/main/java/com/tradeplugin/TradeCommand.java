package com.tradeplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TradeCommand implements CommandExecutor {

    private final TradeManager manager;

    public TradeCommand(TradeManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Тільки для гравців.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(Component.text("Використання: /trade <гравець>", NamedTextColor.RED));
            return true;
        }

        String targetName = args[0];

        if (targetName.equalsIgnoreCase("accept")) {
            handleAccept(player);
            return true;
        }

        if (targetName.equalsIgnoreCase("deny")) {
            handleDeny(player);
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            player.sendMessage(Component.text("Гравець не знайдений.", NamedTextColor.RED));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(Component.text("Не можна торгувати з собою.", NamedTextColor.RED));
            return true;
        }

        if (manager.isInSession(player.getUniqueId())) {
            player.sendMessage(Component.text("Ви вже в трейді.", NamedTextColor.RED));
            return true;
        }

        if (manager.isInSession(target.getUniqueId())) {
            player.sendMessage(Component.text("Цей гравець вже в трейді.", NamedTextColor.RED));
            return true;
        }

        if (manager.hasPendingRequest(target.getUniqueId())) {
            player.sendMessage(Component.text("У цього гравця вже є запит.", NamedTextColor.RED));
            return true;
        }

        boolean sent = manager.sendRequest(player, target);
        if (!sent) {
            player.sendMessage(Component.text("Не вдалося надіслати запит.", NamedTextColor.RED));
            return true;
        }

        player.sendMessage(Component.text("Запит надіслано гравцю ", NamedTextColor.GREEN)
                .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                .append(Component.text(".", NamedTextColor.GREEN)));

        target.sendMessage(Component.text(""));
        target.sendMessage(Component.text("Гравець ", NamedTextColor.GREEN)
                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" хоче обмінятись з вами!", NamedTextColor.GREEN)));
        target.sendMessage(
                Component.text("[Прийняти]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand("/trade accept"))
                        .append(Component.text("  ", NamedTextColor.WHITE))
                        .append(Component.text("[Відхилити]", NamedTextColor.RED)
                                .clickEvent(ClickEvent.runCommand("/trade deny")))
        );
        target.sendMessage(Component.text(""));

        return true;
    }

    private void handleAccept(Player player) {
        if (!manager.hasPendingRequest(player.getUniqueId())) {
            player.sendMessage(Component.text("Немає активного запиту.", NamedTextColor.RED));
            return;
        }
        manager.acceptRequest(player);
    }

    private void handleDeny(Player player) {
        if (!manager.hasPendingRequest(player.getUniqueId())) {
            player.sendMessage(Component.text("Немає активного запиту.", NamedTextColor.RED));
            return;
        }
        manager.declineRequest(player);
    }
}
