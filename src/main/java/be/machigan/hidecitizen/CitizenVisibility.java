package be.machigan.hidecitizen;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CitizenVisibility {
    private final Object2BooleanMap<UUID> hiddenCitizensState = new Object2BooleanArrayMap<>();
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
            PLAYERS_CACHE.put(player.getUniqueId(), citizenVisibility);
        }
        return citizenVisibility.hiddenCitizensState.getBoolean(citizenUUID);
    }

    private static CitizenVisibility getNewVisibilityFor(Player player, UUID citizenUUID) {
        CitizenVisibility citizenVisibility = new CitizenVisibility();
        citizenVisibility.hiddenCitizensState.put(citizenUUID, hideViaPermission(player, citizenUUID));
        return citizenVisibility;
    }

    private static boolean hideViaPermission(Player player, UUID citizenUUID) {
        String permission = getPermissionVisibility(citizenUUID);
        if (!player.isPermissionSet(permission)) return false;
        return !player.hasPermission(permission);
    }

    private static CitizenVisibility getCitizenVisibilityOf(Player player) {
        CitizenVisibility citizenVisibility = PLAYERS_CACHE.get(player.getUniqueId());
        return citizenVisibility == null ? new CitizenVisibility() : citizenVisibility;
    }

    public static String getPermissionVisibility(UUID entityID) {
        return "hidecitizen.show." + entityID;
    }
}
