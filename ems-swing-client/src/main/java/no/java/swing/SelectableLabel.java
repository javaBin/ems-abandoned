package no.java.swing;

import com.jgoodies.forms.factories.Borders;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Id $
 */
public class SelectableLabel extends JLabel {
    private final JTextComponent textComponent;

    public SelectableLabel() {
        this(false);
    }
    
    public SelectableLabel(boolean multiline) {
        Border border = UIManager.getBorder("Label.border");
        setBorder(border != null ? border : Borders.EMPTY_BORDER);
        setLayout(new BorderLayout());
        if (multiline) {
            JTextArea area = new JTextArea(1, 0) {
                @Override
                public void updateUI() {
                    setUI(new BasicTextAreaUI());
                    invalidate();
                }
            };
            area.setWrapStyleWord(true);
            area.setLineWrap(true);
            textComponent = area;
        }
        else {
            textComponent = new JTextField() {
                @Override
                public void updateUI() {
                    setUI(new BasicTextFieldUI());
                    invalidate();
                }
            };
        }
        textComponent.setOpaque(false);
        textComponent.setBackground(new Color(0, true));
        textComponent.setEditable(false);
        textComponent.setDropTarget(null);
        textComponent.setBorder(Borders.EMPTY_BORDER);
        add(textComponent);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void updateUI() {
        // We don't need no UI
    }

    @Override
    public void setFont(Font font) {
        textComponent.setFont(font);
    }

    @Override
    public Font getFont() {
        return textComponent.getFont();
    }

    @Override
    public void setText(String text) {
        // Invoked from sper constructor, so we need to guard...
        if (textComponent != null) {
            textComponent.setText(text);
        }
    }

    @Override
    public String getText() {
        return textComponent.getText();
    }

    protected Highlighter getHighlighter() {
        return textComponent.getHighlighter();
    }

    protected void setHighlighter(Highlighter highlighter) {
        textComponent.setHighlighter(highlighter);
    }

    protected int viewToModel(Point point) {
        return textComponent.viewToModel(point);
    }

    protected Rectangle modelToView(int pos) throws BadLocationException {
        return textComponent.modelToView(pos);
    }

    @Override
    public void setForeground(Color fg) {
        textComponent.setForeground(fg);
    }

    @Override
    public Color getForeground() {
        return textComponent.getForeground();
    }
}
