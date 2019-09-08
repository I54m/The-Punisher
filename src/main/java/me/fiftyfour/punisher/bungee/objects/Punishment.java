package me.fiftyfour.punisher.bungee.objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class Punishment {

    private Reason reason;
    private long duration;
    private Punishment.Type type;
    private String targetUUID, punisherUUID, message;

    public Punishment(@NotNull Reason reason, @Nullable String message, @Nullable Long duration, @NotNull Punishment.Type type, @NotNull String targetUUID, @Nullable String punisherUUID){
        this.reason = reason;
        if (duration != null)
            this.duration = duration;
        this.type = type;
        this.targetUUID = targetUUID;
        this.punisherUUID = punisherUUID;
        this.message = message;
    }

    public enum Type {
        BAN, KICK, MUTE, WARN, ALL
    }
    
    public enum Reason {
        Minor_Chat_Offence, Major_Chat_Offence, DDoS_DoX_Threats, Inappropriate_Link, Scamming, X_Raying, AutoClicker, Fly_Speed_Hacking, Disallowed_Mods, Malicious_PvP_Hacks, Server_Advertisment,
        Greifing, Exploiting, Tpa_Trapping, Impersonation, Other_Minor_Offence, Other_Major_Offence, Other_Offence, Manual, Manual_Hour, Manual_1_Day, Manual_3_Day, Manual_1_Week, Manual_2_Week,
        Manual_3_Week, Manual_1_Month, Manual_Permanently
    }

    public String getPunisherUUID() {
        return punisherUUID;
    }

    public long getDuration() {
        return duration;
    }

    public Reason getReason() {
        return reason;
    }

    public String getTargetUUID() {
        return targetUUID;
    }

    public Punishment.Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "{Reason = " + reason.toString() + ", Message = \"" + message + "\", Duration = " + duration + ", Type = " + type.toString() + ", TargetUUID = " + targetUUID + ", PunisherUUID = " + punisherUUID + "}";
    }
}