package com.shashin.bookmap.dom;

import javax.swing.*;
import java.awt.*;

public class DomSettingsPanel extends JPanel {

    public DomSettingsPanel(DomSettings settings) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        // 1. Auto Recenter
        JCheckBox cbAutoRecenter = new JCheckBox("Auto Recenter", settings.autoRecenterEnabled);
        cbAutoRecenter.addActionListener(e -> settings.autoRecenterEnabled = cbAutoRecenter.isSelected());
        addComponent(this, cbAutoRecenter, gbc, row++, 2);

        // 2. Show Previous Center
        JCheckBox cbShowPrevCenter = new JCheckBox("Show Previous Center", settings.showPreviousCenter);
        cbShowPrevCenter.addActionListener(e -> settings.showPreviousCenter = cbShowPrevCenter.isSelected());
        addComponent(this, cbShowPrevCenter, gbc, row++, 2);

        // 3. Recenter Ticks
        addSpinner(this, gbc, row++, "Recenter ticks", settings.recenterTicksThreshold, 1, 100, 1,
                val -> settings.recenterTicksThreshold = val);

        // 4. Depth Levels
        addSpinner(this, gbc, row++, "Depth Levels", settings.depthLevels, 10, 200, 2,
                val -> settings.depthLevels = val);

        // 5. Min Depth Highlight (Placeholder for future impl)
        addSpinner(this, gbc, row++, "Min Depth Highlight", settings.minDepthHighlight, 1, 1000, 10,
                val -> settings.minDepthHighlight = val);

        // 6. Depth Highlight % (Placeholder for future impl)
        addSpinner(this, gbc, row++, "Depth Highlight >= %avg", settings.depthHighlightPercent, 1, 100, 1,
                val -> settings.depthHighlightPercent = val);

        // Push everything up
        gbc.weighty = 1.0;
        add(new JPanel(), gbc);
    }

    private void addComponent(JPanel panel, JComponent comp, GridBagConstraints gbc, int row, int width) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = width;
        panel.add(comp, gbc);
    }

    private interface IntConsumer { void accept(int val); }

    private void addSpinner(JPanel panel, GridBagConstraints gbc, int row, String labelTxt,
                            int initial, int min, int max, int step, IntConsumer setter) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.0;
        panel.add(new JLabel(labelTxt), gbc);

        SpinnerNumberModel model = new SpinnerNumberModel(initial, min, max, step);
        JSpinner spinner = new JSpinner(model);
        spinner.addChangeListener(e -> setter.accept((Integer) spinner.getValue()));

        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(spinner, gbc);
    }
}