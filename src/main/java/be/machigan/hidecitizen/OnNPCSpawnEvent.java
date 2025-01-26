package be.machigan.hidecitizen;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.function.Consumer;

public class OnNPCSpawnEvent implements Listener {
    private static final Multimap<Integer, Consumer<Entity>> ON_RESPAWN = ArrayListMultimap.create();

    public static void addActionOnNPCRespawn(NPC npc, Consumer<Entity> onRespawn) {
        ON_RESPAWN.put(npc.getId(), onRespawn);
    }

    @EventHandler
    @SuppressWarnings({"unused", "unchecked"})
    public void onSpawn(NPCSpawnEvent event) {
        Entity citizenEntity = event.getNPC().getEntity();
        if (citizenEntity == null) return;
        HideCitizen.addCitizenTranslationIfNotExists(event.getNPC());

        ON_RESPAWN
                .removeAll(event.getNPC().getId())
                .forEach(consumer -> consumer.accept(citizenEntity));
        HideCitizen.getProtocolManager().updateEntity(citizenEntity, (List<Player>) Bukkit.getOnlinePlayers());
    }

}
