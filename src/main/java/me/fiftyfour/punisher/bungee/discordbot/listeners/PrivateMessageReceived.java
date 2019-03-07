package me.fiftyfour.punisher.bungee.discordbot.listeners;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.discordbot.DiscordMain;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import net.dv8tion.jda.core.entities.Guild;
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
        for (String code : DiscordMain.userCodes.values()){
            if (message.toLowerCase().contains(code.toLowerCase())){
                Guild guild = DiscordMain.jda.getGuildById(BungeeMain.PunisherConfig.getString("DiscordIntegration.GuildId"));
                List<Role> rolesToAdd = new ArrayList<>();
                for (String roleids : BungeeMain.PunisherConfig.getStringList("DiscordIntegration.RolesIdsToAddToLinkedUser")){
                    rolesToAdd.add(guild.getRoleById(roleids));
                }
                guild.getController().addRolesToMember(guild.getMember(user), rolesToAdd).queue();
                Map <UUID, String> usercodes = new HashMap<>(DiscordMain.userCodes);
                usercodes.forEach((uuid, string) -> {
                    if (string.equals(code.toUpperCase())){
                        event.getChannel().sendMessage("Linked: `" + user.getName() + "#" + user.getDiscriminator() + "` to minecraft user: " + NameFetcher.getName(uuid.toString().replace("-", "")) + "!" +
                                "\nTo unlink this minecraft account type \"/discord unlink\" in game").queue();
                        if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.EnableRoleSync"))
                        event.getChannel().sendMessage("Login to the server to get your ranks synced over to the discord server").queue();
                DiscordMain.userCodes.remove(uuid, string);
                DiscordMain.verifiedUsers.put(uuid, user.getId());
                    }
                });
            }
        }
        event.getChannel().sendMessage("That is not a valid verification code, Please check that the code is the same as the one given to you!").queue();
    }
}
