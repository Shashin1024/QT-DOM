package com.shashin.bookmap.dom;

import javax.swing.*;

public class AddonItem {
    private String name;
    private boolean enabled;
    private JPanel settingsPanel; // The custom UI for this specific add-on (or instrument)

    // We can store a reference to the settings object or specific details if needed
    // For now, the panel is enough to switch views.

    public AddonItem(String name, boolean enabled, JPanel settingsPanel) {
        this.name = name;
        this.enabled = enabled;
        this.settingsPanel = settingsPanel;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public JPanel getSettingsPanel() {
        return settingsPanel;
    }

    @Override
    public String toString() {
        return name;
    }
}
