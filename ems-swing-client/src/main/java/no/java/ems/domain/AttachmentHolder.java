package no.java.ems.domain;

import java.util.List;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public interface AttachmentHolder {
    List<Binary> getAttachments();
    void setAttachments(List<Binary> attachments);
}
