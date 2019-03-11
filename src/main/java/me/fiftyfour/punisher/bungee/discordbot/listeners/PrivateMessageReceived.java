package me.fiftyfour.punisher.bungee.discordbot.listeners;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.discordbot.DiscordMain;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.*;

public class PrivateMessageReceived extends ListenerAdapter {


    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        User user = event.getAuthor();
        if (user.isFake() || user.isBot()) return;
        if (DiscordMain.verifiedUsers.containsValue(user.getId())){
            event.getChannel().sendMessage("You are already a verified user, to unlink your discord from your minecraft type \"/discord unlink\" in game.").queue();
            return;
        }
        String message = event.getMessage().getContentRaw();
        if (DiscordMain.userCodes.containsValue(message.toUpperCase())){
            UUID uuid = getMinecraftUUID(message);
            linkAccounts(user, event.getChannel(), uuid);
            DiscordMain.userCodes.remove(uuid, message);
        }else{
            event.getChannel().sendMessage("That is not a valid verification code, Please check that the code is the same as the one given to you!").queue();
        }
    }

    private UUID getMinecraftUUID(String code) {
        Map <UUID, String> usercodes = new HashMap<>(DiscordMain.userCodes);
        for (Map.Entry<UUID, String> entry : usercodes.entrySet()){
            if (entry.getValue().equals(code.toUpperCase())){
                return entry.getKey();
            }
        }
        return null;
    }

    private void linkAccounts(User user, PrivateChannel channel, UUID uuid) {
        Guild guild = DiscordMain.jda.getGuildById(BungeeMain.PunisherConfig.getString("DiscordIntegration.GuildId"));
        List<Role> rolesToAdd = new ArrayList<>();
        for (String roleids : BungeeMain.PunisherConfig.getStringList("DiscordIntegration.RolesIdsToAddToLinkedUser")){
            rolesToAdd.add(guild.getRoleById(roleids));
        }
        guild.getController().addRolesToMember(guild.getMember(user), rolesToAdd).queue();
        channel.sendMessage("Linked: `" + user.getName() + "#" + user.getDiscriminator() + "` to minecraft user: " + NameFetcher.getName(uuid.toString().replace("-", "")) + "!" +
                "\nTo unlink this minecraft account type \"/discord unlink\" in game").queue();
        if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.EnableRoleSync"))
            channel.sendMessage("Login to the server to get your ranks synced over to the discord server").queue();
    }
}
