package me.fiftyfour.punisher.universal.fetchers;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.manager.UserManager;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class UserFetcher implements Callable<User> {
    private UUID uuid;

    public void setUuid(UUID uuid){
        this.uuid = uuid;
    }

    @Override
    public User call() throws Exception {
        UserManager userManager = LuckPerms.getApi().getUserManager();
        CompletableFuture<User> userFuture = userManager.loadUser(uuid);
        return userFuture.join();
    }
}
