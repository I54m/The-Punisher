package me.fiftyfour.punisher.systems;

import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.PermissionData;
import me.lucko.luckperms.api.context.ContextManager;
import me.lucko.luckperms.api.manager.UserManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Permissions {

    public static Boolean higher(ProxiedPlayer player, String targetuuid, String targetname) {
        UUID formattedUUID = UUIDFetcher.formatUUID(targetuuid);
        User user = LuckPerms.getApi().getUser(targetname);
        if (user == null) {
            user = giveMeADamnUser(formattedUUID);
            if(user == null){
                throw new IllegalStateException();
            }
        }
        ContextManager cm = LuckPerms.getApi().getContextManager();
        Contexts contexts = cm.lookupApplicableContexts(user).orElse(cm.getStaticContexts());
        PermissionData permissionData = user.getCachedData().getPermissionData(contexts);
        int playerlevel = 0;
        int targetlevel = 0;
        for (int i = 0; i <= 3; i++){
            if (player.hasPermission("punisher.punish.level." + i))
                playerlevel = i;
            if (permissionData.getPermissionValue("punisher.punish.level." + i).asBoolean())
                targetlevel = i;
        }
        if (playerlevel < targetlevel)
            return false;
        else if (player.hasPermission("punisher.bypass") && playerlevel == targetlevel)
            return true;
        else{
            return true;
        }
    }
    public static Boolean higher(Player player, String targetuuid, String targetname) {
        UUID formattedUUID = UUIDFetcher.formatUUID(targetuuid);
        User user = LuckPerms.getApi().getUser(targetname);
        if (user == null) {
            user = giveMeADamnUser(formattedUUID);
            if(user == null){
                throw new IllegalStateException();
            }
        }
        ContextManager cm = LuckPerms.getApi().getContextManager();
        Contexts contexts = cm.lookupApplicableContexts(user).orElse(cm.getStaticContexts());
        PermissionData permissionData = user.getCachedData().getPermissionData(contexts);
        int playerlevel = 0;
        int targetlevel = 0;
        for (int i = 0; i <= 3; i++){
            if (player.hasPermission("punisher.punish.level." + i))
                playerlevel = i;
            if (permissionData.getPermissionValue("punisher.punish.level." + i).asBoolean())
                targetlevel = i;
        }
        if (playerlevel < targetlevel)
            return false;
        else if (player.hasPermission("punisher.bypass") && playerlevel == targetlevel)
            return true;
        else{
            return true;
        }
    }
    public static User giveMeADamnUser(UUID uuid) {
        UserManager userManager = LuckPerms.getApi().getUserManager();
        CompletableFuture<User> userFuture = userManager.loadUser(uuid);

        return userFuture.join();
    }
}
