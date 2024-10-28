package be.machigan.hidecitizen;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class CitizenVisibility {
    private final Map<UUID, Boolean> hiddenCitizensState = new HashMap<>();
    private final static Map<UUID, CitizenVisibility> PLAYERS_CACHE = new HashMap<>();

    public static void showCitizenFor(Player player, Entity citizenEntity) {
        CitizenVisibility citizenVisibility = getCitizenVisibilityOf(player);
        UUID citizenUUID = citizenEntity.getUniqueId();
        citizenVisibility.hiddenCitizensState.put(citizenUUID, false);
        PLAYERS_CACHE.put(player.getUniqueId(), citizenVisibility);
    }

    public static void hideCitizenFor(Player player, Entity citizenEntity) {
        CitizenVisibility citizenVisibility = getCitizenVisibilityOf(player);
        UUID citizenUUID = citizenEntity.getUniqueId();
        citizenVisibility.hiddenCitizensState.put(citizenUUID, true);
        PLAYERS_CACHE.put(player.getUniqueId(), citizenVisibility);
    }

    public static boolean isCitizenVisibleFor(Player player, UUID citizenUUID) {
        CitizenVisibility citizenVisibility = PLAYERS_CACHE.get(player.getUniqueId());
        if (citizenVisibility == null) {
            citizenVisibility = getNewVisibilityFor(player, citizenUUID);
        }
        return Boolean.TRUE.equals(citizenVisibility.hiddenCitizensState.putIfAbsent(citizenUUID, player.hasPermission(getPermissionVisibility(citizenUUID))));
    }

    private static CitizenVisibility getNewVisibilityFor(Player player, UUID citizenUUID) {
        CitizenVisibility citizenVisibility = new CitizenVisibility();
        citizenVisibility.hiddenCitizensState.put(citizenUUID, player.hasPermission(getPermissionVisibility(citizenUUID)));
        return citizenVisibility;
    }

    private static CitizenVisibility getCitizenVisibilityOf(Player player) {
        CitizenVisibility citizenVisibility = PLAYERS_CACHE.get(player.getUniqueId());
        return citizenVisibility == null ? new CitizenVisibility() : citizenVisibility;
    }

    public static String getPermissionVisibility(UUID entityID) {
        return "hidecitizen.hidden." + entityID;
    }
}
