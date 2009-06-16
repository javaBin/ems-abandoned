package no.java.ems.server.cli;

import org.apache.commons.cli.CommandLine;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsCommandLine {
    private CommandLine commandLine;

    public EmsCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public File getEmsHome() {
        return new File(commandLine.getOptionValue(EmsOptions.OPTION_HOME, EmsOptions.getDefaultEmsHome()));
    }

    public boolean hasOption(String optionHelp) {
        return commandLine.hasOption(optionHelp);
    }

    public String getOptionValue(String option) {
        return commandLine.getOptionValue(option);
    }

    public String getOptionValue(String option, String defaultValue) {
        return commandLine.getOptionValue(option, defaultValue);
    }
}
