package com.shashin.bookmap.dom;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JigsawDomConfigPanel extends JPanel {

    private final BookmapJigsawDom adapter;
    private final DomSettings globalSettings;

    // UI Components
    private DefaultListModel<AddonItem> listModel;
    private JList<AddonItem> addOnList;
    private JPanel rightContainer;

    public JigsawDomConfigPanel(BookmapJigsawDom adapter, DomSettings globalSettings) {
        this.adapter = adapter;
        this.globalSettings = globalSettings;
        setLayout(new BorderLayout());

        // --- LEFT SIDE: The List ---
        listModel = new DefaultListModel<>();
        addOnList = new JList<>(listModel);
        addOnList.setCellRenderer(new CheckboxListRenderer());
        addOnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Handle Checkbox Toggling on Click
        addOnList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = addOnList.locationToIndex(e.getPoint());
                if (index != -1) {
                    AddonItem item = listModel.getElementAt(index);
                    Rectangle bounds = addOnList.getCellBounds(index, index);

                    // Simple logic: if click is roughly on the "box" (left side) or just toggle
                    // For now, toggle on any click within bounds if users prefer that,
                    // BUT typical UX is: Click selection -> details; Click box -> toggle.
                    // Let's implement refined click check: Checkbox width is roughly 24
                    if (e.getX() < 24) {
                        boolean newState = !item.isEnabled();
                        item.setEnabled(newState); // Update Model
                        adapter.setInstrumentActive(item.getName(), newState); // Update Logic
                        addOnList.repaint(bounds); // Update UI
                    }
                }
            }
        });

        // Handle Selection -> Update Right Panel
        addOnList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateRightPanel(addOnList.getSelectedValue());
            }
        });

        JScrollPane leftScroll = new JScrollPane(addOnList);
        leftScroll.setMinimumSize(new Dimension(200, 0));
        leftScroll.setPreferredSize(new Dimension(250, 0));

        // --- RIGHT SIDE: The Details ---
        rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(new JLabel("Select an instrument to configure", JLabel.CENTER));

        // --- SPLIT PANE ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightContainer);
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);
    }

    public void addInstrument(String alias) {
        // Check if exists
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).getName().equals(alias))
                return;
        }

        // Create the settings panel for this specific item (or reuse global settings
        // interface)
        // For now, we create a new instance of DomSettingsPanel bound to the GLOBAL
        // settings
        // If per-instrument settings are needed, we would fetch that here.
        JPanel settingsPanel = new DomSettingsPanel(globalSettings);

        // Create Item
        boolean active = adapter.isInstrumentActive(alias);
        AddonItem item = new AddonItem(alias, active, settingsPanel);

        listModel.addElement(item);
    }

    public void removeInstrument(String alias) {
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).getName().equals(alias)) {
                listModel.remove(i);
                // If it was selected, right panel might clear automatically or stay stale
                // Let's clear right panel if selected
                rightContainer.removeAll();
                rightContainer.add(new JLabel("Select an instrument", JLabel.CENTER));
                rightContainer.revalidate();
                rightContainer.repaint();
                return;
            }
        }
    }

    private void updateRightPanel(AddonItem item) {
        rightContainer.removeAll();

        if (item != null && item.getSettingsPanel() != null) {
            // Add a Header for context
            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel title = new JLabel("Settings for: " + item.getName());
            title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
            header.add(title);

            rightContainer.add(header, BorderLayout.NORTH);
            rightContainer.add(item.getSettingsPanel(), BorderLayout.CENTER);
        } else {
            rightContainer.add(new JLabel("No settings available", JLabel.CENTER));
        }

        rightContainer.revalidate();
        rightContainer.repaint();
    }
}
