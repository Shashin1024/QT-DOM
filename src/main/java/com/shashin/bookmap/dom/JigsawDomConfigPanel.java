package com.shashin.bookmap.dom;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class JigsawDomConfigPanel extends JPanel {

    private final BookmapJigsawDom adapter;
    private final Map<String, JCheckBox> instrumentCheckBoxes = new HashMap<>();
    private final JPanel checkboxContainer;

    public JigsawDomConfigPanel(BookmapJigsawDom adapter, DomSettings globalSettings) {
        this.adapter = adapter;
        setLayout(new BorderLayout());

        // Container for checkboxes
        checkboxContainer = new JPanel();
        checkboxContainer.setLayout(new BoxLayout(checkboxContainer, BoxLayout.Y_AXIS));

        // Scroll pane in case of many instruments
        JScrollPane scrollPane = new JScrollPane(checkboxContainer);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addInstrument(String alias) {
        if (instrumentCheckBoxes.containsKey(alias))
            return;

        JCheckBox checkBox = new JCheckBox("Enable DOM Pro for " + alias);
        checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Set initial state
        checkBox.setSelected(adapter.isInstrumentActive(alias));

        checkBox.addActionListener(e -> {
            boolean selected = checkBox.isSelected();
            adapter.setInstrumentActive(alias, selected);
        });

        instrumentCheckBoxes.put(alias, checkBox);
        checkboxContainer.add(checkBox);
        checkboxContainer.revalidate();
        checkboxContainer.repaint();
    }

    public void removeInstrument(String alias) {
        JCheckBox checkBox = instrumentCheckBoxes.remove(alias);
        if (checkBox != null) {
            checkboxContainer.remove(checkBox);
            checkboxContainer.revalidate();
            checkboxContainer.repaint();
        }
    }
}