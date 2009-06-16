package no.java.ems.cli;

import fj.data.Option;
import no.java.ems.cli.command.ImportDirectory;
import no.java.ems.external.v1.EventV1;
import org.apache.commons.cli.Options;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ImportData extends AbstractCli {

    protected ImportData() {
        super("import-data");
    }

    public static void main(String[] args) throws Exception {
        new ImportData().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(null, OPTION_EVENT_ID, true, "The event to import data into");
        options.addOption(null, OPTION_DIRECTORY, true, "The data to import");
        return options;
    }

    protected void work() throws Exception {
        if (!assertIsPresent(OPTION_DIRECTORY)) {
            usage();
            return;
        }

        String eventId = getDefaultEventId();
        File dir = new File(getCommandLine().getOptionValue(OPTION_DIRECTORY));

        if (!dir.isDirectory()) {
            System.err.println("Not a directory: " + dir.getAbsolutePath());
            System.exit(-1);
        }

        Option<EventV1> eventOption = getEms().getEvent(eventId);
        if (eventOption.isNone()) {
            System.err.println("No such event: " + eventId);
        }

        System.out.println("Importing into " + eventOption.some().getName());

        new ImportDirectory(getEms(), eventId, dir).run();
    }
}
