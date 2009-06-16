package no.java.ems.cli;

import com.thoughtworks.xstream.XStream;
import no.java.ems.domain.Event;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ExportData extends AbstractCli {

    protected ExportData() {
        super("export-data");
    }

    public static void main(String[] args) throws Exception {
        new ExportData().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(eventId);
        options.addOption(null, OPTION_FILE, true, "");
        return options;
    }

    protected void work() throws Exception {
        String eventId = getDefaultEventId();
        String path = getCommandLine().getOptionValue(OPTION_FILE);

        XStream xstream = new EmsXStream();

        EventData data = exportData(eventId);

        if (data == null) {
            return;
        }

        OutputStream writer = null;
        try {
            if(path != null)
            {
                File file = new File(path);
                File parent = file.getParentFile();

                if (!parent.isDirectory()) {
                    if (!parent.mkdirs()) {
                        System.err.println("Unable to create direcetory: " + parent.getAbsolutePath());
                    }
                }

                writer = new FileOutputStream(file);
            }
            else{
                writer = System.out;
            }

            xstream.toXML(data, writer);
        } finally {
            if(path != null){
                close(writer);
            }
        }
    }

    private EventData exportData(String eventId) {
        EventData data = new EventData();
        data.setEventId(eventId);

        Event event = getEms().getEvent(eventId);
        if (event == null) {
            System.err.println("No such event: " + eventId);
            return null;
        }

        System.err.println("Exporting event: " + event.getName());

        exportSessions(data);

        return data;
    }

    private void exportSessions(EventData data) {
        data.setSessions(getEms().getSessions(data.getEventId()));
        System.err.println("Exported " + data.getSessions().size() + " sessions");
    }
}
