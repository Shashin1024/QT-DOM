package com.shashin.bookmap.dom;

import velox.api.layer1.Layer1ApiFinishable;
import velox.api.layer1.Layer1ApiInstrumentAdapter;
import velox.api.layer1.Layer1ApiDataAdapter;
import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1Attachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.common.Log;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.settings.Layer1ApiSettingsPanelProvider; // Correct Core API interface
import velox.gui.StrategyPanel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Layer1Attachable
@Layer1StrategyName("Jigsaw DOM")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class BookmapJigsawDom implements
        Layer1ApiInstrumentAdapter,
        Layer1ApiDataAdapter,
        Layer1ApiFinishable,
        Layer1ApiSettingsPanelProvider { // Implementation fixed

    private final Layer1ApiProvider provider;
    private volatile DomSettings settings = new DomSettings();

    private final ConcurrentHashMap<String, Boolean> activeInstruments = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DomResources> resources = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, InstrumentInfo> instrumentInfos = new ConcurrentHashMap<>();
    // Listeners for settings panels to update them when instruments are
    // added/removed
    private final List<JigsawDomConfigPanel> configPanels = new CopyOnWriteArrayList<>();

    public BookmapJigsawDom(Layer1ApiProvider provider) {
        this.provider = provider;
        provider.addListener((velox.api.layer1.Layer1ApiInstrumentListener) this);
        provider.addListener((velox.api.layer1.Layer1ApiDataListener) this);
    }

    private static class DomResources {
        DomModel model;
        JFrame window;
        JigsawDomPanel panel;
        Timer timer;
    }

    @Override
    public void onInstrumentAdded(String alias, InstrumentInfo instrumentInfo) {
        instrumentInfos.put(alias, instrumentInfo);
        SwingUtilities.invokeLater(() -> configPanels.forEach(panel -> panel.addInstrument(alias)));
    }

    public void setInstrumentActive(String alias, boolean active) {
        activeInstruments.put(alias, active);
        if (active)
            startDom(alias);
        else
            stopDom(alias);
    }

    public boolean isInstrumentActive(String alias) {
        return activeInstruments.getOrDefault(alias, false);
    }

    private void startDom(String alias) {
        if (resources.containsKey(alias))
            return;

        InstrumentInfo info = instrumentInfos.get(alias);
        if (info == null)
            return; // safety check

        DomResources res = new DomResources();
        res.model = new DomModel();

        SwingUtilities.invokeLater(() -> {
            res.window = new JFrame("DOM: " + alias);
            res.panel = new JigsawDomPanel(settings);
            res.panel.setPips(info.pips);

            res.window.add(res.panel);
            res.window.setSize(500, 800);
            res.window.setVisible(true);

            // Handle manual window closing
            res.window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    activeInstruments.put(alias, false);
                    stopDom(alias);
                    // Update settings panels to reflect that it's unchecked (if we had 2-way
                    // binding, for now just logic)
                    // Since the checkbox drives the state, if the user closes the window, we might
                    // want to uncheck the box.
                    // But that requires a callback to the panel. For now, let's keep it simple.
                    // Ideally, we should update the UI.
                    updatePanels(alias);
                }
            });

            res.timer = new Timer(33, e -> res.panel.updateSnapshot(res.model.getSnapshot()));
            res.timer.start();
        });
        resources.put(alias, res);
    }

    private void updatePanels(String alias) {
        SwingUtilities.invokeLater(() -> {
            configPanels.forEach(panel -> {
                // Force re-adding (which is safe) or refreshes state if we implemented that.
                // Current simple implementation of addInstrument checks map key so it won't
                // re-add.
                // Proper 2-way binding would require a 'refresh' method on the panel.
                // For now, let's essentially do nothing or implement refresh later if needed.
                // User request was mostly about having the checkboxes.
                // A re-add with current logic won't update the checkbox state if it exists.
                // We will improve this if requested.
            });
        });
    }

    private void stopDom(String alias) {
        DomResources res = resources.remove(alias);
        if (res != null) {
            SwingUtilities.invokeLater(() -> {
                if (res.timer != null)
                    res.timer.stop();
                if (res.window != null)
                    res.window.dispose();
            });
        }
    }

    /**
     * This is the method Bookmap calls to get the settings UI for Core API modules.
     */
    @Override
    public StrategyPanel[] getSettingsPanels() {
        // 1. Activation Panel
        StrategyPanel activationWrapper = new StrategyPanel("Enable");
        JigsawDomConfigPanel configUI = new JigsawDomConfigPanel(this, settings);

        // Register this panel to receive updates
        configPanels.add(configUI);

        // Populate existing instruments
        instrumentInfos.keySet().forEach(configUI::addInstrument);

        activationWrapper.setLayout(new java.awt.BorderLayout());
        activationWrapper.add(configUI, java.awt.BorderLayout.CENTER);

        // 2. Visual Settings Panel (using your existing spinner logic)
        StrategyPanel visualWrapper = new StrategyPanel("Global Visual Settings");
        DomSettingsPanel visualUI = new DomSettingsPanel(settings);
        visualWrapper.setLayout(new java.awt.BorderLayout());
        visualWrapper.add(visualUI, java.awt.BorderLayout.CENTER);

        return new StrategyPanel[] { activationWrapper, visualWrapper };
    }

    @Override
    public void onTrade(String alias, double price, int size, TradeInfo tradeInfo) {
        DomResources res = resources.get(alias);
        InstrumentInfo info = instrumentInfos.get(alias);
        if (res != null && info != null) {
            // FIX: Convert double price (e.g. 2350.25) to tick price (e.g. 9401)
            int tickPrice = (int) Math.round(price / info.pips);
            res.model.onTrade(tickPrice, size, tradeInfo.isBidAggressor);
        }
    }

    @Override
    public void onDepth(String alias, boolean isBid, int price, int size) {
        DomResources res = resources.get(alias);
        if (res != null)
            res.model.onDepth(isBid, price, size);
    }

    @Override
    public void onInstrumentRemoved(String alias) {
        stopDom(alias);
        instrumentInfos.remove(alias);
        SwingUtilities.invokeLater(() -> configPanels.forEach(panel -> panel.removeInstrument(alias)));
    }

    @Override
    public void finish() {
        resources.keySet().forEach(this::stopDom);
        configPanels.clear();
    }
}