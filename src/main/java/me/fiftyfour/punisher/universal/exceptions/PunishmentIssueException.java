package me.fiftyfour.punisher.universal.exceptions;

import me.fiftyfour.punisher.bungee.objects.Punishment;

public class PunishmentIssueException extends Exception {

    private String reasonForFailure;
    private Punishment punishment;
    private Throwable cause;

    public PunishmentIssueException(String reasonForFailure, Punishment punishment, Throwable cause){
        super(cause);
        this.reasonForFailure = reasonForFailure;
        this.punishment = punishment;
        this.cause = cause;
    }

    public PunishmentIssueException(String reasonForFailure, Punishment punishment){
        this.reasonForFailure = reasonForFailure;
        this.punishment = punishment;
    }

    @Override
    public String getMessage() {
        if (cause != null)
            return "Punishment: " + punishment.toString() + " Was unable to be Issued because: " + reasonForFailure + ". This Error was caused by " + cause +
                ". Error message for cause: " + cause.getMessage();
        else
            return "Punishment: " + punishment.toString() + " Was unable to be Issued because: " + reasonForFailure + ". Cause was unknown.";

    }
}
