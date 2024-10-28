package be.machigan.hidecitizen;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HideCitizenCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (!sender.hasPermission(HideCitizen.USE_PERMISSION)) {
            sender.sendMessage("Cannot hide");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("Please provide /hide-citizen <npc-id> [show|hide|toggle] <player>");
            return true;
        }
        NPC npc = null;
        try {
            npc = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(args[0]));
        } catch (NumberFormatException ignore) {}
        if (npc == null) {
            sender.sendMessage("NPC doesn't exists. If you don't know ID, use /npc list");
            return true;
        }
        Player player = Bukkit.getPlayer(args[2]);
        if (player == null) {
            sender.sendMessage("Player don't exist");
            return true;
        }
        HideCitizen.addCitizenTranslationIfNotExists(npc);
        switch (args[1].toLowerCase()) {
            case "show" -> showNPCFromPlayer(player, npc.getEntity());
            case "hide" -> hideNPCPlayer(player, npc.getEntity());
            case "toggle" -> toggleNPCVisibilityFromPlayer(player, npc.getEntity());
            default -> sender.sendMessage("Unknown action use show, hide or toggle");
        }

        return true;
    }

    private static void showNPCFromPlayer(Player player, Entity citizenEntity) {
        CitizenVisibility.showCitizenFor(player, citizenEntity);
        executeCommand("lp user " + player.getName() + " permission set " + CitizenVisibility.getPermissionVisibility(citizenEntity.getUniqueId()) + " false");
        HideCitizen.getProtocolManager().updateEntity(citizenEntity, List.of(player));
    }

    private static void hideNPCPlayer(Player player, Entity citizenEntity) {
        CitizenVisibility.hideCitizenFor(player, citizenEntity);
        PacketContainer destroyEntity = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyEntity.getModifier().write(0, new IntArrayList(new int[] {citizenEntity.getEntityId()}));
        HideCitizen.getProtocolManager().sendServerPacket(player, destroyEntity);
        executeCommand("lp user " + player.getName() + " permission set " + CitizenVisibility.getPermissionVisibility(citizenEntity.getUniqueId()) + " true");
    }

    private static void toggleNPCVisibilityFromPlayer(Player player, Entity entity) {
        if (CitizenVisibility.isCitizenVisibleFor(player, entity.getUniqueId())) {
            showNPCFromPlayer(player, entity);
        } else {
            hideNPCPlayer(player, entity);
        }
    }

    private static void executeCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
