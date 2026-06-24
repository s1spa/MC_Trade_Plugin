package com.tradeplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TradePlugin extends JavaPlugin {

    private TradeManager tradeManager;

    @Override
    public void onEnable() {
        tradeManager = new TradeManager(this);

        getCommand("trade").setExecutor(new TradeCommand(tradeManager));
        getServer().getPluginManager().registerEvents(new TradeListener(tradeManager), this);

        long intervalTicks = 20L * 60 * 10; // кожні 10 хвилин
        Bukkit.getScheduler().runTaskTimer(this, () ->
                Bukkit.broadcast(
                        Component.text("✦ Безпечно торгуй з іншими гравцями на відстані → ")
                                .color(NamedTextColor.GOLD)
                                .append(Component.text("/trade")
                                        .color(NamedTextColor.YELLOW)
                                        .decorate(TextDecoration.UNDERLINED)
                                        .clickEvent(ClickEvent.runCommand("/trade")))),
                intervalTicks, intervalTicks);

        long halfInterval = intervalTicks / 2; // офсет 5 хвилин
        Bukkit.getScheduler().runTaskTimer(this, () ->
                Bukkit.broadcast(
                        Component.text("✦ Плагін «Трейди» — ")
                                .color(NamedTextColor.GOLD)
                                .append(Component.text("автор: ").color(NamedTextColor.GRAY))
                                .append(Component.text("s1sp(s1si4kaa)")
                                        .color(NamedTextColor.AQUA)
                                        .decorate(TextDecoration.UNDERLINED)
                                        .clickEvent(ClickEvent.openUrl("https://s1spa.github.io/Portfolio/"))
                                        .hoverEvent(HoverEvent.showText(Component.text("Відкрити портфоліо"))))
                                .append(Component.text(" │ ").color(NamedTextColor.DARK_GRAY))
                                .append(Component.text("тестери: ").color(NamedTextColor.GRAY))
                                .append(Component.text("JopaBoBu").color(NamedTextColor.YELLOW))),
                halfInterval, intervalTicks);

        long thirdOffset = 20L * 60 * 3; // офсет 3 хвилини
        Bukkit.getScheduler().runTaskTimer(this, () ->
                Bukkit.broadcast(
                        Component.text("✦ Плагін «Трейди» — ")
                                .color(NamedTextColor.GOLD)
                                .append(Component.text("Знайшли баг? Повідомте Адміністраторам або в GitHub →")
                                        .color(NamedTextColor.GRAY))
                                .append(Component.text("GitHub Issues")
                                        .color(NamedTextColor.AQUA)
                                        .decorate(TextDecoration.UNDERLINED)
                                        .clickEvent(ClickEvent.openUrl("https://github.com/s1spa/MC_Trade_Plugin/issues"))
                                        .hoverEvent(HoverEvent.showText(Component.text("Відкрити GitHub Issues"))))),
                thirdOffset, intervalTicks);

        getLogger().info("TradePlugin увімкнено.");
    }

    @Override
    public void onDisable() {
        getLogger().info("TradePlugin вимкнено.");
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }
}
 