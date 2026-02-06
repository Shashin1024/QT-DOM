package com.shashin.bookmap.dom;

import velox.api.layer1.Layer1ApiFinishable;
import velox.api.layer1.Layer1ApiInstrumentAdapter;
import velox.api.layer1.Layer1ApiDataAdapter;
import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1Attachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.Layer1CustomPanelsGetter;
import velox.gui.StrategyPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ConcurrentHashMap;

@Layer1Attachable
@Layer1StrategyName("Jigsaw DOM")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class BookmapJigsawDom implements
        Layer1ApiInstrumentAdapter,
        Layer1ApiDataAdapter,
        Layer1ApiFinishable,
        Layer1CustomPanelsGetter {

    private final Layer1ApiProvider provider;
    private volatile DomSettings settings = new DomSettings();

    private final ConcurrentHashMap<String, Boolean> activeInstruments = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DomResources> resources = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, InstrumentInfo> instrumentInfos = new ConcurrentHashMap<>();

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

    // --- Instrument lifecycle ---

    @Override
    public void onInstrumentAdded(String alias, InstrumentInfo instrumentInfo) {
        instrumentInfos.put(alias, instrumentInfo);
    }

    @Override
    public void onInstrumentRemoved(String alias) {
        stopDom(alias);
        instrumentInfos.remove(alias);
    }

    // --- Activation ---

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

    // --- DOM window management ---

    private void startDom(String alias) {
        if (resources.containsKey(alias))
            return;

        InstrumentInfo info = instrumentInfos.get(alias);
        if (info == null)
            return;

        DomResources res = new DomResources();
        res.model = new DomModel(settings);

        SwingUtilities.invokeLater(() -> {
            res.window = new JFrame("DOM: " + alias);
            res.panel = new JigsawDomPanel(settings);
            res.panel.setPips(info.pips);

            res.window.add(res.panel);
            res.window.setSize(500, 800);
            res.window.setVisible(true);

            res.window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    activeInstruments.put(alias, false);
                    stopDom(alias);
                }
            });

            res.timer = new Timer(33, e -> res.panel.updateSnapshot(res.model.getSnapshot()));
            res.timer.start();
        });
        resources.put(alias, res);
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

    // --- Settings panel (Layer1CustomPanelsGetter) ---
    // Bookmap calls getCustomGuiFor per instrument. We return a settings
    // panel for that specific instrument - no need for our own instrument list.

    @Override
    public StrategyPanel[] getCustomGuiFor(String alias, String title) {
        // 1. Enable panel
        StrategyPanel enablePanel = new StrategyPanel("Enable");
        enablePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JCheckBox enableCb = new JCheckBox("Enabled DOM Pro for " + alias, isInstrumentActive(alias));
        enableCb.addActionListener(e -> setInstrumentActive(alias, enableCb.isSelected()));
        enablePanel.add(enableCb);

        // 2. Settings panel
        StrategyPanel settingsPanel = new StrategyPanel("DOM Pro Settings");
        settingsPanel.setLayout(new BorderLayout());
        DomSettingsPanel form = new DomSettingsPanel(settings);
        settingsPanel.add(form, BorderLayout.CENTER);

        return new StrategyPanel[] { enablePanel, settingsPanel };
    }

    // --- Market data ---
    // Bookmap Level1 API: onTrade price is already in level/tick units
    // (same as onDepth int price). Real price = level * pips.
    // Do NOT divide by pips again.

    @Override
    public void onTrade(String alias, double price, int size, TradeInfo tradeInfo) {
        DomResources res = resources.get(alias);
        if (res != null) {
            int tickPrice = (int) Math.round(price);
            res.model.onTrade(tickPrice, size, tradeInfo.isBidAggressor);
        }
    }

    @Override
    public void onDepth(String alias, boolean isBid, int price, int size) {
        DomResources res = resources.get(alias);
        if (res != null)
            res.model.onDepth(isBid, price, size);
    }

    // --- Cleanup ---

    @Override
    public void finish() {
        resources.keySet().forEach(this::stopDom);
    }
}
