package me.fiftyfour.punisher.bungee.systems;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.bungee.objects.Punishment;
import me.fiftyfour.punisher.universal.exceptions.PunishmentsDatabaseException;

import java.sql.SQLException;
import java.text.DecimalFormat;

public class ReputationSystem {
    private static BungeeMain plugin = BungeeMain.getInstance();
    private static PunishmentManager punishMnger = PunishmentManager.getInstance();

    public static void minusRep(String targetname, String uuid, double amount) {
        if (!BungeeMain.RepStorage.contains(uuid)) {
            BungeeMain.RepStorage.set(uuid,  Double.valueOf(new DecimalFormat("##.##").format((5.0 - amount))));
            BungeeMain.saveRep();
        } else {
            double currentRep = BungeeMain.RepStorage.getDouble(uuid);
            if ((currentRep - amount) > -10 && (currentRep - amount) < 10) {
                BungeeMain.RepStorage.set(uuid,  Double.valueOf(new DecimalFormat("##.##").format((currentRep - amount))));
                BungeeMain.saveRep();
            } else if ((currentRep - amount) > 10) {
                BungeeMain.RepStorage.set(uuid, 10.00);
                BungeeMain.saveRep();
            } else if ((currentRep - amount) <= -10) {
                BungeeMain.RepStorage.set(uuid, Double.valueOf(new DecimalFormat("##.##").format((currentRep - amount))));
                BungeeMain.saveRep();
                try {
                    Punishment ban = new Punishment(Punishment.Reason.Manual, "Overly Toxic (Rep dropped below -10)", (long) 3.154e+12, Punishment.Type.BAN, uuid, "CONSOLE");
                    punishMnger.issue(ban, null, targetname, false, true, false);
                } catch (SQLException e) {
                    try {
                        throw new PunishmentsDatabaseException("Issuing perm rep ban", targetname, ReputationSystem.class.getName(), e);
                    } catch (PunishmentsDatabaseException pde) {
                        ErrorHandler errorHandler = ErrorHandler.getInstance();
                        errorHandler.log(pde);
                    }
                    if (plugin.testConnectionManual()) {
                        try {
                            Punishment ban = new Punishment(Punishment.Reason.Manual, "Overly Toxic (Rep dropped below -10)", (long) 3.154e+12, Punishment.Type.BAN, uuid, "CONSOLE");
                            punishMnger.issue(ban, null, targetname, false, true, false);
                        } catch (SQLException sqle) {
                            try {
                                throw new PunishmentsDatabaseException("Issuing perm rep ban", targetname, ReputationSystem.class.getName(), e);
                            } catch (PunishmentsDatabaseException pde) {
                                ErrorHandler errorHandler = ErrorHandler.getInstance();
                                errorHandler.log(pde);
                            }
                        }
                    }
                }
            }
        }

    }
    public static void addRep(String targetname, String uuid, double amount) {
        if (!BungeeMain.RepStorage.contains(uuid)){
            BungeeMain.RepStorage.set(uuid, Double.valueOf(new DecimalFormat("##.##").format((5.0 + amount))));
            BungeeMain.saveRep();
        }else {
            double currentRep = BungeeMain.RepStorage.getDouble(uuid);
            if ((currentRep + amount) > -10 && (currentRep + amount) < 10) {
                BungeeMain.RepStorage.set(uuid, Double.valueOf(new DecimalFormat("##.##").format((currentRep + amount))));
                BungeeMain.saveRep();
            } else if ((currentRep + amount) > 10) {
                BungeeMain.RepStorage.set(uuid, 10.00);
                BungeeMain.saveRep();
            } else if ((currentRep + amount) <= -10) {
                BungeeMain.RepStorage.set(uuid, Double.valueOf(new DecimalFormat("##.##").format((currentRep + amount))));
                BungeeMain.saveRep();
                try {
                    Punishment ban = new Punishment(Punishment.Reason.Manual, "Overly Toxic (Rep dropped below -10)", (long) 3.154e+12, Punishment.Type.BAN, uuid, "CONSOLE");
                    punishMnger.issue(ban, null, targetname, false, true, false);
                } catch (SQLException e) {
                    try {
                        throw new PunishmentsDatabaseException("Issuing perm rep ban", targetname, ReputationSystem.class.getName(), e);
                    } catch (PunishmentsDatabaseException pde) {
                        ErrorHandler errorHandler = ErrorHandler.getInstance();
                        errorHandler.log(pde);
                    }
                    if (plugin.testConnectionManual()) {
                        try {
                            throw new PunishmentsDatabaseException("Issuing perm rep ban", targetname, ReputationSystem.class.getName(), e);
                        } catch (PunishmentsDatabaseException pde) {
                            ErrorHandler errorHandler = ErrorHandler.getInstance();
                            errorHandler.log(pde);
                        }
                    }
                }
            }
        }
    }
    public static void setRep(String targetname, String uuid, double amount) {
        if (amount > 10){
            BungeeMain.RepStorage.set(uuid, 10.00);
            BungeeMain.saveRep();
        }else{
            BungeeMain.RepStorage.set(uuid, Double.valueOf(new DecimalFormat("##.##").format(amount)));
            BungeeMain.saveRep();
        }
        if (!(amount > -10)) {
            BungeeMain.RepStorage.set(uuid, Double.valueOf(new DecimalFormat("##.##").format(amount)));
            BungeeMain.saveRep();
            try {
                Punishment ban = new Punishment(Punishment.Reason.Manual, "Overly Toxic (Rep dropped below -10)", (long) 3.154e+12, Punishment.Type.BAN, uuid, "CONSOLE");
                punishMnger.issue(ban, null, targetname, false, true, false);
            } catch (SQLException e) {
                try {
                    throw new PunishmentsDatabaseException("Issuing perm rep ban", targetname, ReputationSystem.class.getName(), e);
                } catch (PunishmentsDatabaseException pde) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.log(pde);
                }
                if (plugin.testConnectionManual()) {
                    try {
                        Punishment ban = new Punishment(Punishment.Reason.Manual, "Overly Toxic (Rep dropped below -10)", (long) 3.154e+12, Punishment.Type.BAN, uuid, "CONSOLE");
                        punishMnger.issue(ban, null, targetname, false, true, false);
                    } catch (SQLException sqle) {
                        try {
                            throw new PunishmentsDatabaseException("Issuing perm rep ban", targetname, ReputationSystem.class.getName(), e);
                        } catch (PunishmentsDatabaseException pde) {
                            ErrorHandler errorHandler = ErrorHandler.getInstance();
                            errorHandler.log(pde);
                        }
                    }
                }
            }
        }
    }
    public static String getRep(String uuid) {
        if (BungeeMain.RepStorage.contains(uuid))
            return String.valueOf(BungeeMain.RepStorage.getDouble(uuid));
        else return null;
    }
}