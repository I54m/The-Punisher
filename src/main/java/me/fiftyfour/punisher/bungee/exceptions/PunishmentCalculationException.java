package me.fiftyfour.punisher.bungee.exceptions;

public class PunishmentCalculationException extends Exception {

    private String reasonForFailure, stage;
    private Throwable cause;

    public PunishmentCalculationException(String reasonForFailure, String stage, Throwable cause){
        super(cause);
        this.reasonForFailure = reasonForFailure;
        this.stage = stage;
        this.cause = cause;
    }

    public PunishmentCalculationException(String reasonForFailure, String stage){
        this.reasonForFailure = reasonForFailure;
        this.stage = stage;
    }

    @Override
    public String getMessage() {
        if (cause != null)
            return "Unable to calculate " + stage + " for automatic punishment because: " + reasonForFailure + ". This Error was caused by " + cause +
                    ". Error message for cause: " + cause.getMessage();
        else
            return "Unable to calculate " + stage + " for automatic punishment because: " + reasonForFailure + ". Cause was unknown.";

    }
}
