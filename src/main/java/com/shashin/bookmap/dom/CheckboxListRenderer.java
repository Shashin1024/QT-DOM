package com.shashin.bookmap.dom;

import javax.swing.*;
import java.awt.*;

public class CheckboxListRenderer extends JCheckBox implements ListCellRenderer<AddonItem> {
    public CheckboxListRenderer() {
        setOpaque(true); // Must be true to draw background colors
        setBorderPainted(true);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4)); // Padding
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends AddonItem> list,
            AddonItem value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        // 1. Set the text and checkbox state from the model
        setText(value.getName());
        setSelected(value.isEnabled());

        // 2. Handle Colors (The "Blue" Highlight)
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        // Ensure the font matches
        setFont(list.getFont());

        return this;
    }
}
