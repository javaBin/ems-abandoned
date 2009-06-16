package no.java.ems.client.swing;

import no.java.ems.domain.Binary;
import no.java.ems.domain.URIBinary;
import no.java.swing.DefaultPanel;
import no.java.swing.IconSizeDecorator;
import no.java.swing.SessionFileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.codehaus.httpcache4j.MIMEType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URLConnection;
import java.util.TooManyListenersException;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public class PhotoPanel extends DefaultPanel {

    private final Dimension thumbnailSize;
    private SessionFileChooser fileChooser;
    private Binary binary;
    private JLabel thumbnail;
    private PhotoPanel.BrowseAction browseAction;
    private PhotoPanel.ClearAction clearAction;

    public PhotoPanel(Dimension thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
        initialize();
    }

    public Binary getBinary() {
        return binary;
    }

    public void setBinary(final Binary binary) {
        Validate.isTrue(binary == null || "image".equals(MIMEType.valueOf(binary.getMimeType()).getPrimaryType()), "Binary was null or not an image");
        firePropertyChange("binary", this.binary, this.binary = binary);
    }

    public void initModels() {
    }

    public void initActions() {
        browseAction = new BrowseAction();
        clearAction = new ClearAction();
    }

    public void initComponents() {
        thumbnail = new JLabel("", JLabel.CENTER);
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(browseAction);
        popupMenu.addSeparator();
        popupMenu.add(clearAction);
        setComponentPopupMenu(popupMenu);
    }

    public void initBindings() {
    }

    public void initListeners() {
        addPropertyChangeListener("binary", new BinaryListener());
        setTransferHandler(new DropHandler());
        addMouseListener(new MouseHandler());
        try {
            getDropTarget().addDropTargetListener(
                    new DropTargetAdapter() {

                        @Override
                        public void dragExit(final DropTargetEvent event) {
                            setBackground(UIManager.getColor("Panel.background"));
                        }

                        @Override
                        public void dragEnter(final DropTargetDragEvent event) {
                            setBackground(Color.ORANGE);
                        }

                        public void drop(final DropTargetDropEvent event) {
                            setBackground(UIManager.getColor("Panel.background"));
                        }

                    }
            );
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
    }

    public void initLayout() {
        thumbnail.setBorder(BorderFactory.createEmptyBorder());
        thumbnail.setPreferredSize(thumbnailSize);
        add(thumbnail);
    }

    public void initState() {
    }

    private Binary createBinaryFromFile(File file) {
        String type = URLConnection.guessContentTypeFromName(file.getName());
        return new URIBinary(
                file.getName(),
                type,
                file.length(),
                file.toURI()
        );
    }

    private class DropHandler extends TransferHandler {

        public DropHandler() {
            super(null);
        }

        @Override
        public boolean canImport(final TransferSupport support) {
            return support.isDrop() && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(final TransferSupport support) {
            try {
                final File[] droppedFiles = FileUtils.convertFileCollectionToFileArray((java.util.List)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                if (droppedFiles.length == 1 && droppedFiles[0].isFile()) {
                    setBinary(createBinaryFromFile(droppedFiles[0]));
                    return true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return false;
        }

    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mouseClicked(final MouseEvent event) {
            if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
                browseAction.actionPerformed(null);
            }
        }

    }

    private class BinaryListener implements PropertyChangeListener {

        public void propertyChange(final PropertyChangeEvent event) {
            Binary oldPhoto = (Binary)event.getOldValue();
            Binary newPhoto = (Binary)event.getNewValue();
            if (newPhoto == null) {
                thumbnail.setIcon(getResourceMap().getIcon(getFullResourceKey("noPicture.icon")));
            } else {
                if (oldPhoto != null &&
                    Entities.isLocalBinary(oldPhoto) &&
                    !Entities.isLocalBinary(newPhoto) &&
                    oldPhoto.getFileName().equals(newPhoto.getFileName()) &&
                    oldPhoto.getSize() == newPhoto.getSize()
                        ) {
                    // the new photo is the server version of the previous photo (after a save)
                    return;
                }
                // todo: we need a cache!
                thumbnail.setIcon(getResourceMap().getIcon(getFullResourceKey("loadingPicture.icon")));
                Application.getInstance().getContext().getTaskService().execute(
                        new Task<Image, Void>(Application.getInstance()) {
                            protected Image doInBackground() throws Exception {
                                return ImageIO.read(binary.getDataStream());
                            }

                            @Override
                            protected void succeeded(final Image result) {
                                thumbnail.setIcon(new IconSizeDecorator(new ImageIcon(result), thumbnailSize.width, thumbnailSize.height));
                            }

                            @Override
                            protected void failed(Throwable cause) {
                                thumbnail.setIcon(getResourceMap().getIcon(getFullResourceKey("failedPicture.icon")));
                            }
                        }
                );
            }

        }

    }

    private class BrowseAction extends DefaultAction {

        public BrowseAction() {
            super("browse");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if (fileChooser == null) {
                fileChooser = new SessionFileChooser("PhotoPanel.fileChooser");
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileSelectionMode(SessionFileChooser.FILES_ONLY);
            }
            if (fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(PhotoPanel.this)) == SessionFileChooser.APPROVE_OPTION) {
                setBinary(createBinaryFromFile(fileChooser.getSelectedFile()));
            }
        }

    }

    private class ClearAction extends DefaultAction {

        public ClearAction() {
            super("clear");
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            setBinary(null);
        }

    }

}
