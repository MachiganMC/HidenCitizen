package be.machigan.hidecitizen;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Husk;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class HideCitizen extends JavaPlugin {
    private static HideCitizen instance;
    public static final Map<Integer, UUID> ENTITY_ID_UUID_TRANSLATION = new HashMap<>();
    private static ProtocolManager protocolManager;
    public static final String USE_PERMISSION = "hidecitizen.hide";

    @Override
    public void onEnable() {
        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.removePacketListeners(this);
        protocolManager.addPacketListener(new PacketCitizen());
        this.setCommand();
        this.startFillFromNPCRegistryScheduler();
    }

    private void setCommand() {
        PluginCommand command = this.getCommand("hide-citizen");
        if (command == null) return;
        command.setExecutor(new HideCitizenCommand());;
        command.setTabCompleter(new HideCitizenTabCompleter());
    }

    /**
     * The registry of NPC is not filled synchronously, so we fetch it until it's filled or to many times has passed
     * (no NPC, it can happen)
     */
    private void startFillFromNPCRegistryScheduler() {
        AtomicInteger passedTimes = new AtomicInteger();
        final int[] taskID = new int[] { -1 };
        taskID[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            passedTimes.addAndGet(20);
            CitizensAPI.getNPCRegistry().forEach(npc -> ENTITY_ID_UUID_TRANSLATION.put(npc.getEntity().getEntityId(), npc.getEntity().getUniqueId()));
            if (!ENTITY_ID_UUID_TRANSLATION.isEmpty() || passedTimes.get() > 1200)
                Bukkit.getScheduler().cancelTask(taskID[0]);
        },  20L, 20L);
    }

    public static HideCitizen getInstance() {
        return instance;
    }

    public static void addCitizenTranslationIfNotExists(NPC npc) {
        Entity entity = npc.getEntity();
        if (!ENTITY_ID_UUID_TRANSLATION.containsKey(entity.getEntityId()))
            ENTITY_ID_UUID_TRANSLATION.put(entity.getEntityId(), entity.getUniqueId());
    }

    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
