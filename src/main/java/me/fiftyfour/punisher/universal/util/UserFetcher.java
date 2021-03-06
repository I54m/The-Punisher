package me.fiftyfour.punisher.universal.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class UserFetcher implements Callable<User> {
    private UUID uuid;

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public User call() throws Exception {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null)
            throw new ClassNotFoundException("Luckperms is not enabled!");
        UserManager userManager = provider.getProvider().getUserManager();
        CompletableFuture<User> userFuture = userManager.loadUser(uuid);
        return userFuture.join();
    }
}
