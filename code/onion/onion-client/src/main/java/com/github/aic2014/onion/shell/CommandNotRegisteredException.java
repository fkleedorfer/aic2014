package com.github.aic2014.onion.shell;

/**
 * Thrown in case an unknown command is issued.
 */
public class CommandNotRegisteredException extends Exception {
    public CommandNotRegisteredException() {
        super();
    }

    public CommandNotRegisteredException(String command) {
        super(String.format("Command '%s' not registered.", command));
        this.command = command;
    }

    private String command;

    public String getCommand() {
        return command;
    }
}
