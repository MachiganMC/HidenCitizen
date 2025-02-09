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
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HideCitizenCommand implements CommandExecutor {
    private static final Map<String, BiConsumer<Player, Entity>> ACTIONS_PLAYER = Map.of(
            "show", HideCitizenCommand::showNPCFromPlayer,
            "hide", HideCitizenCommand::hideNPCPlayer,
            "toggle", HideCitizenCommand::toggleNPCVisibilityFromPlayer
    );
    private static final Map<String, BiConsumer<String, Entity>> ACTIONS_GROUP = Map.of(
            "show", HideCitizenCommand::showNPCFromGroup,
            "hide", HideCitizenCommand::hideNPCFromGroup,
            "toggle", HideCitizenCommand::toggleNPCVisibilityFromGroup
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
        Consumer<Entity> action;
        if (args.length >= 4 && args[3].equalsIgnoreCase("--group")) {
            String group = args[2];
            if (!GroupUtils.isGroupExists(group)) {
                sender.sendMessage("Group doesn't exist");
                return true;
            }
            action = Optional
                    .ofNullable(ACTIONS_GROUP.get(args[1].toLowerCase()))
                    .map(a -> (Consumer<Entity>) entity -> a.accept(group, entity))
                    .orElse(null);
        } else {
            Player player = Bukkit.getPlayer(args[2]);
            if (player == null) {
                sender.sendMessage("Player doesn't exist");
                return true;
            }
             action = Optional
                     .ofNullable(ACTIONS_PLAYER.get(args[1].toLowerCase()))
                     .map(a -> (Consumer<Entity>) entity -> a.accept(player, entity))
                     .orElse(null);
        }
        if (action == null) {
            sender.sendMessage("Action not found. Use hide/show/toggle.");
            return true;
        }
        if (npc.getEntity() == null) {
            sender.sendMessage("The npc is despawned, your action will be consumed when the npc will respawn");
            OnNPCSpawnEvent.addActionOnNPCRespawn(npc, action);
            return true;
        }
        HideCitizen.addCitizenTranslationIfNotExists(npc);
        action.accept(npc.getEntity());
        return true;
    }

    private static void showNPCFromPlayer(Player player, Entity citizenEntity) {
        CitizenVisibility.showCitizenFor(player, citizenEntity);
        executeCommand("lp user " + player.getName() + " permission unset " + CitizenVisibility.getPermissionVisibility(citizenEntity));
        HideCitizen.getProtocolManager().updateEntity(citizenEntity, List.of(player));
    }

    private static void hideNPCPlayer(Player player, Entity citizenEntity) {
        CitizenVisibility.hideCitizenFor(player, citizenEntity);
        PacketContainer destroyEntity = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyEntity.getModifier().write(0, new IntArrayList(new int[] {citizenEntity.getEntityId()}));
        HideCitizen.getProtocolManager().sendServerPacket(player, destroyEntity);
        executeCommand("lp user " + player.getName() + " permission set " + CitizenVisibility.getPermissionVisibility(citizenEntity) + " false");
    }

    private static void toggleNPCVisibilityFromPlayer(Player player, Entity entity) {
        if (CitizenVisibility.isCitizenVisibleFor(player, entity.getUniqueId())) {
            showNPCFromPlayer(player, entity);
        } else {
            hideNPCPlayer(player, entity);
        }
    }

    private static void showNPCFromGroup(String group, Entity citizenEntity) {
        List<Player> players = GroupUtils.getPlayersOfGroup(group);
        CitizenVisibility.showCitizenFor(players, citizenEntity);
        executeCommand("lp group " + group + " permission unset "+ CitizenVisibility.getPermissionVisibility(citizenEntity));
        HideCitizen.getProtocolManager().updateEntity(citizenEntity, players);
    }

    private static void hideNPCFromGroup(String group, Entity citizenEntity) {
        List<Player> players = GroupUtils.getPlayersOfGroup(group);
        CitizenVisibility.hideCitizenFor(players, citizenEntity);
        PacketContainer destroyEntity = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyEntity.getModifier().write(0, new IntArrayList(new int[] {citizenEntity.getEntityId()}));
        players.forEach(player -> HideCitizen.getProtocolManager().sendServerPacket(player, destroyEntity));
        executeCommand("lp group " + group + " permission set " + CitizenVisibility.getPermissionVisibility(citizenEntity) + " false");
    }

    private static void toggleNPCVisibilityFromGroup(String group, Entity entity) {
        if (!GroupUtils.hasPermission(group, CitizenVisibility.getPermissionVisibility(entity))) {
            hideNPCFromGroup(group, entity);
        } else {
            showNPCFromGroup(group, entity);
        }
    }

    private static void executeCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
