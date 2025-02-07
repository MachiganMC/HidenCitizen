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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class HideCitizenCommand implements CommandExecutor {
    private static final Map<String, BiConsumer<Player, Entity>> ACTIONS = Map.of(
            "show", HideCitizenCommand::showNPCFromPlayer,
            "hide", HideCitizenCommand::hideNPCPlayer,
            "toggle", HideCitizenCommand::toggleNPCVisibilityFromPlayer
    );

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
        BiConsumer<Player, Entity> action = ACTIONS.get(args[1].toLowerCase());
        if (action == null) {
            sender.sendMessage("The npc is despawned, your action will be consumed when the entity will respawn");
            return true;
        }
        if (npc.getEntity() == null) {
            sender.sendMessage("The npc is despawned, your action will be consumed when the npc will respawn");
            OnNPCSpawnEvent.addActionOnNPCRespawn(npc, newEntity ->  action.accept(player, newEntity));
            return true;
        }
        HideCitizen.addCitizenTranslationIfNotExists(npc);
        action.accept(player, npc.getEntity());
        return true;
    }

    private static void showNPCFromPlayer(Player player, Entity citizenEntity) {
        CitizenVisibility.showCitizenFor(player, citizenEntity);
        executeCommand("lp user " + player.getName() + " permission set " + CitizenVisibility.getPermissionVisibility(citizenEntity.getUniqueId()) + " true");
        HideCitizen.getProtocolManager().updateEntity(citizenEntity, List.of(player));
    }

    private static void hideNPCPlayer(Player player, Entity citizenEntity) {
        CitizenVisibility.hideCitizenFor(player, citizenEntity);
        PacketContainer destroyEntity = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyEntity.getModifier().write(0, new IntArrayList(new int[] {citizenEntity.getEntityId()}));
        HideCitizen.getProtocolManager().sendServerPacket(player, destroyEntity);
        executeCommand("lp user " + player.getName() + " permission set " + CitizenVisibility.getPermissionVisibility(citizenEntity.getUniqueId()) + " false");
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
