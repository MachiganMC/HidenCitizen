package be.machigan.hidecitizen;

import static com.comphenix.protocol.PacketType.Play.Server.*;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;

import java.util.*;

import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_EQUIPMENT;

public class PacketCitizen extends PacketAdapter {
    private static final PacketType[] PACKET_TYPES = {
            ENTITY_EQUIPMENT,
            ANIMATION,
            COLLECT,
            SPAWN_ENTITY,
            SPAWN_ENTITY_EXPERIENCE_ORB,
            ENTITY_VELOCITY,
            REL_ENTITY_MOVE,
            ENTITY_LOOK,
            ENTITY_TELEPORT,
            ENTITY_HEAD_ROTATION,
            ENTITY_STATUS,
            ATTACH_ENTITY,
            ENTITY_METADATA,
            ENTITY_EFFECT,
            REMOVE_ENTITY_EFFECT,
            BLOCK_BREAK_ANIMATION
    };

    public PacketCitizen() {
        super(HideCitizen.getInstance(), ListenerPriority.HIGH, PACKET_TYPES);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        UUID entityUUID = HideCitizen.ENTITY_ID_UUID_TRANSLATION.get(event.getPacket().getIntegers().read(0));
        if (entityUUID == null) return;
        if (CitizenVisibility.isCitizenVisibleFor(event.getPlayer(), entityUUID))
            event.setCancelled(true);
    }
}
