package be.machigan.hidecitizen;

import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class GroupUtils {
    public static boolean isGroupExists(String group) {
        return HideCitizen.getLuckPerms().getGroupManager().getGroup(group) != null;
    }

    @SuppressWarnings("unchecked")
    public static List<Player> getPlayersOfGroup(String group) {
        return (List<Player>) Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> {
                    User user = HideCitizen.getLuckPerms().getUserManager().getUser(player.getUniqueId());
                    if (user == null) return false;
                    return user.getInheritedGroups(user.getQueryOptions()).stream().anyMatch(g -> g.getName().equals(group));
                })
                .toList();
    }

    public static boolean hasPermission(String group, String permission) {
        return Optional
                .ofNullable(HideCitizen.getLuckPerms().getGroupManager().getGroup(group))
                .map(g -> g.getNodes(NodeType.PERMISSION).stream().anyMatch(n -> n.getPermission().equals(permission)))
                .orElse(false);
    }
}
