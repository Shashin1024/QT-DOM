package com.shashin.bookmap.dom;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DomSettingsPanel extends JPanel {

    // Dark theme palette
    private static final Color PANEL_BG = new Color(45, 50, 55);
    private static final Color SECTION_BG = new Color(55, 60, 65);
    private static final Color SECTION_BORDER = new Color(70, 75, 80);
    private static final Color LABEL_FG = new Color(160, 165, 170);
    private static final Color VALUE_FG = new Color(210, 215, 220);
    private static final Color ICON_FG = new Color(140, 145, 150);
    private static final Color ICON_HOVER = new Color(200, 205, 210);

    public DomSettingsPanel(DomSettings settings) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        // --- Behavior Section ---
        CollapsibleSection behaviorSection = new CollapsibleSection("Behavior");
        behaviorSection.addRow(createCheckboxRow("Auto Recenter", settings.autoRecenterEnabled,
                v -> settings.autoRecenterEnabled = v));
        behaviorSection.addRow(createCheckboxRow("Show Previous Center", settings.showPreviousCenter,
                v -> settings.showPreviousCenter = v));
        behaviorSection.addRow(createSpinnerRow("Recenter Ticks", settings.recenterTicksThreshold, 1, 100, 1,
                v -> settings.recenterTicksThreshold = v));
        behaviorSection.addRow(createSpinnerRow("Depth Levels", settings.depthLevels, 10, 200, 2,
                v -> settings.depthLevels = v));
        behaviorSection.addRow(createSpinnerRow("Min Depth Highlight", settings.minDepthHighlight, 1, 1000, 10,
                v -> settings.minDepthHighlight = v));
        behaviorSection.addRow(createSpinnerRow("Depth Highlight >= %avg", settings.depthHighlightPercent, 1, 100, 1,
                v -> settings.depthHighlightPercent = v));
        behaviorSection.addRow(createSpinnerRow("Footprint Reset (min)", settings.footprintResetMinutes, 1, 60, 1,
                v -> settings.footprintResetMinutes = v));
        add(behaviorSection);
        add(Box.createVerticalStrut(4));

        // --- Graphics Section ---
        CollapsibleSection graphicsSection = new CollapsibleSection("Graphics");
        graphicsSection.addRow(createSpinnerRow("Font Size", settings.fontSize, 8, 24, 1,
                v -> settings.fontSize = v));
        graphicsSection.addRow(createSpinnerRow("Row Size", settings.rowSize, 12, 40, 1,
                v -> settings.rowSize = v));
        add(graphicsSection);
        add(Box.createVerticalStrut(4));

        // --- General Colors ---
        CollapsibleSection generalColors = new CollapsibleSection("General Colors");
        generalColors.addRow(createColorRow("Table Background", settings.colBg,
                new Color(30, 30, 30), c -> settings.colBg = c));
        generalColors.addRow(createColorRow("Grid Lines", settings.colGrid,
                new Color(55, 55, 55), c -> settings.colGrid = c));
        generalColors.addRow(createColorRow("Header Background", settings.colHeaderBg,
                new Color(10, 10, 10), c -> settings.colHeaderBg = c));
        generalColors.addRow(createColorRow("Header Text", settings.colHeaderText,
                new Color(180, 180, 180), c -> settings.colHeaderText = c));
        add(generalColors);
        add(Box.createVerticalStrut(4));

        // --- Price Column ---
        CollapsibleSection priceColors = new CollapsibleSection("Price Column");
        priceColors.addRow(createColorRow("Price Column Background", settings.colPriceBg,
                new Color(40, 40, 40), c -> settings.colPriceBg = c));
        priceColors.addRow(createColorRow("Price Text Color", settings.colPriceText,
                Color.WHITE, c -> settings.colPriceText = c));
        priceColors.addRow(createColorRow("Last Traded Price Background", settings.colLtpBg,
                new Color(255, 200, 0), c -> settings.colLtpBg = c));
        priceColors.addRow(createColorRow("Last Traded Price Text", settings.colLtpText,
                Color.BLACK, c -> settings.colLtpText = c));
        add(priceColors);
        add(Box.createVerticalStrut(4));

        // --- Bid / Ask ---
        CollapsibleSection dataColors = new CollapsibleSection("Bid / Ask");
        dataColors.addRow(createColorRow("Bid Column Background", settings.colBidColBg,
                new Color(0, 40, 80), c -> settings.colBidColBg = c));
        dataColors.addRow(createColorRow("Ask Column Background", settings.colAskColBg,
                new Color(80, 20, 20), c -> settings.colAskColBg = c));
        dataColors.addRow(createColorRow("Bid Size Bar Fill", settings.colBidBar,
                new Color(0, 100, 200), c -> settings.colBidBar = c));
        dataColors.addRow(createColorRow("Ask Size Bar Fill", settings.colAskBar,
                new Color(200, 60, 60), c -> settings.colAskBar = c));
        dataColors.addRow(createColorRow("Size Text on Bar", settings.colTextOnBar,
                Color.WHITE, c -> settings.colTextOnBar = c));
        dataColors.addRow(createColorRow("Large Size Highlight", settings.colTextHighlight,
                Color.YELLOW, c -> settings.colTextHighlight = c));
        add(dataColors);
        add(Box.createVerticalStrut(4));

        // --- Footprint ---
        CollapsibleSection fpColors = new CollapsibleSection("Footprint");
        fpColors.addRow(createColorRow("Bid Volume Text", settings.colFpBid,
                new Color(100, 200, 255), c -> settings.colFpBid = c));
        fpColors.addRow(createColorRow("Ask Volume Text", settings.colFpAsk,
                new Color(255, 100, 100), c -> settings.colFpAsk = c));
        fpColors.addRow(createColorRow("Volume Separator (x)", settings.colFpX,
                new Color(100, 100, 100), c -> settings.colFpX = c));
        fpColors.addRow(createColorRow("Trade Count Delta", settings.colTextTrdCount,
                new Color(220, 220, 220), c -> settings.colTextTrdCount = c));
        fpColors.addRow(createColorRow("Volume Histogram Bar", settings.colVolumeBar,
                new Color(50, 70, 90), c -> settings.colVolumeBar = c));
        fpColors.addRow(createColorRow("Volume Text", settings.colVolumeText,
                new Color(180, 190, 200), c -> settings.colVolumeText = c));
        fpColors.addRow(createColorRow("Delta Positive", settings.colDeltaPos,
                new Color(0, 180, 80), c -> settings.colDeltaPos = c));
        fpColors.addRow(createColorRow("Delta Negative", settings.colDeltaNeg,
                new Color(200, 50, 50), c -> settings.colDeltaNeg = c));
        add(fpColors);
        add(Box.createVerticalStrut(4));

        // --- Indicators ---
        CollapsibleSection indicatorColors = new CollapsibleSection("Indicators");
        indicatorColors.addRow(createColorRow("Velocity Text", settings.colVelocityText,
                new Color(255, 0, 220), c -> settings.colVelocityText = c));
        indicatorColors.addRow(createColorRow("Positive Reload", settings.colReloadPos,
                new Color(0, 255, 100), c -> settings.colReloadPos = c));
        indicatorColors.addRow(createColorRow("Negative Reload", settings.colReloadNeg,
                new Color(255, 50, 50), c -> settings.colReloadNeg = c));
        add(indicatorColors);
        add(Box.createVerticalStrut(4));

        // --- Iceberg Detection ---
        CollapsibleSection icebergSection = new CollapsibleSection("Iceberg Detection");
        icebergSection.addRow(createCheckboxRow("Enable Iceberg Detection", settings.icebergDetectionEnabled,
                v -> settings.icebergDetectionEnabled = v));
        icebergSection.addRow(createSpinnerRow("Min Chunk Size", settings.minIcebergChunkSize, 1, 500, 1,
                v -> settings.minIcebergChunkSize = v));
        icebergSection.addRow(createColorRow("Iceberg Dot Color", settings.colIcebergDot,
                new Color(0, 255, 255), c -> settings.colIcebergDot = c));
        add(icebergSection);

        // Bottom glue
        add(Box.createVerticalGlue());
    }

    // ========== ROW BUILDERS ==========

    private JPanel createCheckboxRow(String label, boolean initial, Consumer<Boolean> setter) {
        JPanel row = createBaseRow(label);
        JCheckBox cb = new JCheckBox("", initial);
        cb.setOpaque(false);
        cb.setForeground(VALUE_FG);
        cb.addActionListener(e -> setter.accept(cb.isSelected()));
        row.add(cb, BorderLayout.EAST);
        return row;
    }

    private JPanel createSpinnerRow(String label, int initial, int min, int max, int step,
                                     Consumer<Integer> setter) {
        JPanel row = createBaseRow(label);
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initial, min, max, step));
        spinner.setPreferredSize(new Dimension(70, 24));
        spinner.setBackground(SECTION_BG);
        spinner.setForeground(VALUE_FG);
        spinner.addChangeListener(e -> setter.accept((Integer) spinner.getValue()));
        row.add(spinner, BorderLayout.EAST);
        return row;
    }

    private JPanel createColorRow(String label, Color initial, Color defaultColor, Consumer<Color> setter) {
        JPanel row = createBaseRow(label);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controls.setOpaque(false);

        // Rounded color swatch
        ColorSwatch swatch = new ColorSwatch(initial);

        // Pen/edit button
        IconButton editBtn = new IconButton("\u270E"); // pencil
        editBtn.setToolTipText("Choose color");

        // Reset button
        IconButton resetBtn = new IconButton("\u21BA"); // reset arrow
        resetBtn.setToolTipText("Reset to default");

        editBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Choose " + label + " color",
                    swatch.getSwatchColor());
            if (chosen != null) {
                swatch.setSwatchColor(chosen);
                setter.accept(chosen);
            }
        });

        resetBtn.addActionListener(e -> {
            swatch.setSwatchColor(defaultColor);
            setter.accept(defaultColor);
        });

        // Clicking the swatch also opens the chooser
        swatch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editBtn.doClick();
            }
        });

        controls.add(swatch);
        controls.add(editBtn);
        controls.add(resetBtn);
        row.add(controls, BorderLayout.EAST);
        return row;
    }

    private JPanel createBaseRow(String label) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(3, 8, 3, 8));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lbl = new JLabel(label);
        lbl.setForeground(LABEL_FG);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 12f));
        row.add(lbl, BorderLayout.WEST);
        return row;
    }

    // ========== COLLAPSIBLE SECTION ==========

    private class CollapsibleSection extends JPanel {
        private final JPanel contentPanel;
        private boolean collapsed = false;
        private final JLabel chevron;

        CollapsibleSection(String title) {
            setLayout(new BorderLayout());
            setBackground(SECTION_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(SECTION_BORDER, 1),
                    new EmptyBorder(0, 0, 0, 0)));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            // Header bar
            JPanel header = new JPanel(new BorderLayout(6, 0));
            header.setBackground(SECTION_BG);
            header.setBorder(new EmptyBorder(6, 10, 6, 10));
            header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Section line decoration
            JLabel dash = new JLabel("\u2014");
            dash.setForeground(ICON_FG);
            header.add(dash, BorderLayout.WEST);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setForeground(LABEL_FG);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
            header.add(titleLabel, BorderLayout.CENTER);

            chevron = new JLabel("\u25B2"); // up arrow = expanded
            chevron.setForeground(ICON_FG);
            header.add(chevron, BorderLayout.EAST);

            header.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    collapsed = !collapsed;
                    contentPanel.setVisible(!collapsed);
                    chevron.setText(collapsed ? "\u25BC" : "\u25B2");
                    revalidate();
                    repaint();
                }
            });
            add(header, BorderLayout.NORTH);

            // Content area
            contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(SECTION_BG);
            contentPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
            add(contentPanel, BorderLayout.CENTER);
        }

        void addRow(JPanel row) {
            contentPanel.add(row);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension pref = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }
    }

    // ========== COLOR SWATCH (rounded pill) ==========

    private static class ColorSwatch extends JComponent {
        private Color swatchColor;

        ColorSwatch(Color color) {
            this.swatchColor = color;
            setPreferredSize(new Dimension(40, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Click to change");
        }

        Color getSwatchColor() {
            return swatchColor;
        }

        void setSwatchColor(Color c) {
            this.swatchColor = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = h; // full pill shape

            // Fill
            g2.setColor(swatchColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

            // Border
            g2.setColor(new Color(100, 105, 110));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc));

            g2.dispose();
        }
    }

    // ========== ICON BUTTON ==========

    private static class IconButton extends JButton {
        IconButton(String icon) {
            super(icon);
            setFont(new Font("SansSerif", Font.PLAIN, 14));
            setForeground(ICON_FG);
            setBackground(SECTION_BG);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(24, 24));
            setMargin(new Insets(0, 0, 0, 0));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setForeground(ICON_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setForeground(ICON_FG);
                }
            });
        }
    }
}
