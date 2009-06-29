/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package no.java.ems.client.swing;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.domain.AbstractEntity;
import no.java.ems.domain.Binary;
import no.java.ems.domain.ByteArrayBinary;
import no.java.ems.domain.URIBinary;
import no.java.swing.*;
import org.apache.commons.io.FileUtils;
import org.jdesktop.application.Application;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AttachementsPanel extends DefaultPanel {

    private final AbstractEntity entity;
    private JList attachementsList;
    private Action browseAction;
    private Action saveAction;
    private Action openAction;
    private Action deleteAction;
    private JListBinding<Binary, AbstractEntity, JList> binding;

    public AttachementsPanel(final AbstractEntity entity) {
        this.entity = entity;
        initialize();
    }

    public void initModels() {
    }

    public void initActions() {
        browseAction = new BrowseAction();
        saveAction = new SaveAction();
        openAction = new OpenAction();
        deleteAction = new DeleteAction();
        saveAction.setEnabled(false);
        openAction.setEnabled(false);
        deleteAction.setEnabled(false);
    }

    public void initComponents() {
        attachementsList = new JList();
        attachementsList.setPrototypeCellValue(new ByteArrayBinary("pretty-long-attachement-file-name.extension", "image/jpeg", new byte[0]));
        attachementsList.setVisibleRowCount(3);
        attachementsList.setCellRenderer(new BinaryCellRenderer());
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(browseAction);
        popupMenu.add(saveAction);
        popupMenu.addSeparator();
        popupMenu.add(openAction);
        popupMenu.addSeparator();
        popupMenu.add(deleteAction);
        popupMenu.add(new AddExternalBinaryAction());
        attachementsList.setComponentPopupMenu(popupMenu);
        SwingHelper.bindAction(browseAction, attachementsList, (KeyStroke)browseAction.getValue(Action.ACCELERATOR_KEY));
        SwingHelper.bindAction(saveAction, attachementsList, (KeyStroke)saveAction.getValue(Action.ACCELERATOR_KEY));
        SwingHelper.bindAction(openAction, attachementsList, (KeyStroke)openAction.getValue(Action.ACCELERATOR_KEY));
        SwingHelper.bindAction(deleteAction, attachementsList, (KeyStroke)deleteAction.getValue(Action.ACCELERATOR_KEY));
    }

    public void initBindings() {
        binding = SwingBindings.createJListBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                entity,
                BeanProperty.<AbstractEntity, List<Binary>>create("attachements"),
                attachementsList
        );
        binding.bind();
    }

    public void initListeners() {
        setTransferHandler(new DropHandler());
        attachementsList.addMouseListener(new MouseHandler());
        attachementsList.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(final ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            boolean enabled = attachementsList.getSelectedIndex() != -1;
                            saveAction.setEnabled(enabled);
                            openAction.setEnabled(enabled);
                            deleteAction.setEnabled(enabled);
                        }
                    }
                }
        );

    }

    public void initLayout() {
        FormLayout layout = new FormLayout("0:g,3dlu,p,1dlu,p,1dlu,p,1dlu,p", "p");
        layout.setColumnGroups(new int[][]{{3, 5, 7, 9}});
        JPanel buttonBar = new JPanel(layout);
        buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
        CellConstraints cc = new CellConstraints();
        buttonBar.add(new JButton(browseAction), cc.xy(3, 1));
        buttonBar.add(new JButton(saveAction), cc.xy(5, 1));
        buttonBar.add(new JButton(openAction), cc.xy(7, 1));
        buttonBar.add(new JButton(deleteAction), cc.xy(9, 1));
        for (Component component : buttonBar.getComponents()) {
            if (component instanceof JButton) {
                ((JButton)component).setText("");
            }
        }
        setLayout(new BorderLayout());
        add(new JScrollPane(attachementsList), BorderLayout.CENTER);
        add(buttonBar, BorderLayout.SOUTH);
    }

    public void initState() {
    }

    private class BrowseAction extends DefaultAction {

        private JFileChooser fileChooser;

        public BrowseAction() {
            super("browse");
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            JFileChooser fileChooser = getFileChooser();
            int result = fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor((Component)event.getSource()));
            if (result == JFileChooser.APPROVE_OPTION) {
                Application.getInstance().getContext().getTaskService().execute(
                        new AttachFilesTask(fileChooser.getSelectedFiles())
                );
            }
        }

        private JFileChooser getFileChooser() {
            if (fileChooser == null) {
                fileChooser = new SessionFileChooser("AttachementsPanel.fileChooser");
                fileChooser.setDialogTitle(getString("dialog.title"));
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                TextWithMnemonic approve = new TextWithMnemonic(getString("dialog.approveButton"));
                fileChooser.setApproveButtonText(approve.getTextWithoutMnemonic());
                if (approve.getMnemonic() != null) {
                    fileChooser.setApproveButtonMnemonic(approve.getMnemonic());
                }
            }
            return fileChooser;
        }

    }

    private class OpenAction extends DefaultAction {

        public OpenAction() {
            super("open");
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            List<Binary> selected = new ArrayList<Binary>();
            for (Object binaryObject : attachementsList.getSelectedValues()) {
                selected.add((Binary)binaryObject);
            }
            getContext().getTaskService().execute(new OpenTask(selected));

        }

    }

    private class SaveAction extends DefaultAction {

        private JFileChooser fileChooser;

        public SaveAction() {
            super("save");
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            List<Binary> selected = new ArrayList<Binary>();
            for (Object binaryObject : attachementsList.getSelectedValues()) {
                selected.add((Binary)binaryObject);
            }
            JFileChooser fileChooser = getFileChooser();
            int result = fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor((Component)event.getSource()));
            if (result == JFileChooser.APPROVE_OPTION) {
                getContext().getTaskService().execute(new SaveBinariesTask(selected, fileChooser.getSelectedFile()));
            }
        }

        private JFileChooser getFileChooser() {
            if (fileChooser == null) {
                fileChooser = new SessionFileChooser("AttachementsPanel.fileChooser");
                fileChooser.setDialogTitle(getString("dialog.title"));
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                TextWithMnemonic approve = new TextWithMnemonic(getString("dialog.approveButton"));
                fileChooser.setApproveButtonText(approve.getTextWithoutMnemonic());
                if (approve.getMnemonic() != null) {
                    fileChooser.setApproveButtonMnemonic(approve.getMnemonic());
                }
            }
            return fileChooser;
        }

    }

    private class DeleteAction extends DefaultAction {

        public DeleteAction() {
            super("delete");
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            List<Binary> selected = new ArrayList<Binary>();
            for (Object binaryObject : attachementsList.getSelectedValues()) {
                selected.add((Binary)binaryObject);
            }
            List<Binary> newBinaries = new ArrayList<Binary>(binding.getSourceObject().getAttachements());
            newBinaries.removeAll(selected);
            binding.getSourceObject().setAttachements(newBinaries);
        }

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
                final File[] droppedFiles = FileUtils.convertFileCollectionToFileArray((List)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                Application.getInstance().getContext().getTaskService().execute(new AttachFilesTask(droppedFiles));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

    }

    private class AttachFilesTask extends DefaultTask<List<File>, Binary> {

        private final File[] files;

        public AttachFilesTask(final File[] files) {
            super("AttachFilesTask");
            this.files = files;
        }

        protected List<File> doInBackground() throws Exception {
            List<File> allFiles = new ArrayList<File>();
            for (File file : files) {
                if (file.isFile()) {
                    allFiles.add(file);
                }
                if (file.isDirectory()) {
                    for (Object fileObject : FileUtils.listFiles(file, null, true)) {
                        if (fileObject instanceof File) {
                            File temp = (File)fileObject;
                            if (temp.isFile()) {
                                allFiles.add(temp);
                            }
                        }
                    }
                }
            }
            for (int n = 0; n < allFiles.size(); n++) {
                File file = allFiles.get(n);
                setProgress(n + 1, 0, allFiles.size());
                if (allFiles.size() == 1) {
                    message(getFullResourceKey("progress.singular"), file.getName());
                } else {
                    message(getFullResourceKey("progress.plural"), file.getName(), n + 1, allFiles.size());
                }
                publish(
                        new URIBinary(
                                file.getName(),
                                URLConnection.guessContentTypeFromName(file.getName()),
                                file.length(),
                                file.toURI()
                        )
                );
            }
            return allFiles;
        }

        @Override
        protected void process(final List<Binary> binaries) {
            List<Binary> newBinaries = new ArrayList<Binary>(binding.getSourceObject().getAttachements());
            newBinaries.addAll(binaries);
            binding.getSourceObject().setAttachements(newBinaries);
            // todo: select added attachements
        }

        @Override
        protected void succeeded(final List<File> files) {
            if (files.size() == 1) {
                setMessage(getString("succeeded.singular", files.get(0).getName(), getExecutionDuration(TimeUnit.MILLISECONDS)));
            } else {
                setMessage(getString("succeeded.plural", files.size(), getExecutionDuration(TimeUnit.MILLISECONDS)));
            }
        }
    }

    private class OpenTask extends SaveBinariesTask {

        public OpenTask(final List<Binary> binaries) {
            super(binaries, new File(System.getProperty("java.io.tmpdir")));
        }

        @Override
        protected File getTargetFile(final File folder, final Binary binary) {
            File tempFile = new File(folder, Long.toHexString(System.nanoTime()) + "_" + binary.getFileName());
            tempFile.deleteOnExit();
            return tempFile;
        }

        @Override
        protected void wroteBinaryToFile(final Map.Entry<Binary, File> binaryAndFile) {
            try {
                Desktop.getDesktop().open(binaryAndFile.getValue());
            } catch (IOException e) {
                throw new RuntimeException("Unable to open file: " + binaryAndFile.getValue().getAbsolutePath(), e);
            }
        }

    }

    private class BinaryCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
            if (value instanceof Binary) {
                Binary binary = (Binary)value;
                setText(
                        AttachementsPanel.this.getString(
                                "attachementText",
                                binary.getFileName(),
                                FileUtils.byteCountToDisplaySize(binary.getSize())
                        )
                );
                setToolTipText(
                        AttachementsPanel.this.getString(
                                "attachementToolTip",
                                binary.getFileName(),
                                FileUtils.byteCountToDisplaySize(binary.getSize()),
                                binary.getMimeType()
                        ) + (binary instanceof URIBinary ? " URI: " + ((URIBinary)binary).getURI() : "")
                );
                // todo: better icon lookup. preferred filename->mimetype->default
                if (binary.getFileName() != null) {
                    int pos = binary.getFileName().lastIndexOf('.');
                    if (pos != -1) {
                        setIcon(IconUtils.getIconForExtension(binary.getFileName().substring(pos), false));
                    }
                }
            }
            return this;
        }

    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(final MouseEvent event) {
            if (SwingUtilities.isRightMouseButton(event)) {
                int index = attachementsList.locationToIndex(event.getPoint());
                if (index != -1 && !attachementsList.isSelectedIndex(index)) {
                    attachementsList.setSelectedIndex(index);
                }
                if (!attachementsList.hasFocus()) {
                    attachementsList.requestFocusInWindow();
                }
            }
            if (event.isPopupTrigger()) {
                Point point = attachementsList.getPopupLocation(event);
                getComponentPopupMenu().show(event.getComponent(), point.x, point.y);
            }
        }

        @Override
        public void mouseReleased(final MouseEvent event) {
            if (event.isPopupTrigger()) {
                Point point = attachementsList.getPopupLocation(event);
                getComponentPopupMenu().show(event.getComponent(), point.x, point.y);
            }
        }

        @Override
        public void mouseClicked(final MouseEvent event) {
            int index = attachementsList.locationToIndex(event.getPoint());
            if (index != -1 && SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2 && event.getModifiersEx() == 0) {
                if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    List<Binary> selected = new ArrayList<Binary>();
                    for (Object binaryObject : attachementsList.getSelectedValues()) {
                        selected.add((Binary)binaryObject);
                    }
                    Application.getInstance().getContext().getTaskService().execute(new OpenTask(selected));
                }
            }
        }

    }

    private class AddExternalBinaryAction extends DefaultAction {
        protected AddExternalBinaryAction() {
            super("external");
        }

        public void actionPerformed(ActionEvent event) {
            //TODO: open dialog
        }

    }
}
