package pl.poldzialka.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import pl.poldzialka.config.MessageManager;
import pl.poldzialka.managers.PlotManager;
import pl.poldzialka.model.PlotData;

public class PlayerChatListener implements Listener {
    private final PlotManager plotManager;
    private final MessageManager messages;

    public PlayerChatListener(PlotManager plotManager, MessageManager messages) {
        this.plotManager = plotManager;
        this.messages = messages;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlotManager.PendingAction pending = plotManager.getPendingAction(player);
        if (pending == null) {
            return;
        }
        event.setCancelled(true);
        plotManager.finishMemberInput(player);

        String targetName = event.getMessage().trim();
        if (targetName.isEmpty() || targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(messages.getComponent("invalid-chat-input"));
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(messages.getComponent("plot-member-not-found"));
            return;
        }

        PlotData plot = pending.getPlot();
        if (plot == null) {
            player.sendMessage(messages.getComponent("plot-member-not-found"));
            return;
        }

        if (plot.getOwnerUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(messages.getComponent("member-added-self"));
            return;
        }

        if (plotManager.addMember(plot, target.getName())) {
            player.sendMessage(messages.formatComponent("plot-added-member", "member", target.getName()));
        } else {
            player.sendMessage(messages.getComponent("plot-member-not-found"));
        }
    }
}
