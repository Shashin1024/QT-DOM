package com.shashin.bookmap.dom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class JigsawDomPanel extends JPanel {
    private final DomSettings settings;
    private DomSnapshot currentSnapshot;
    private double pips = 1.0;

    // Layout Constants
    private final int HEADER_HEIGHT = 24;
    private int rowHeight = 20;
    private int centerPrice = 0;

    // Colors - REMOVED CONSTANTS, NOW USING settings.colName

    public JigsawDomPanel(DomSettings settings) {
        this.settings = settings;
        setBackground(settings.colBg);
        setFont(new Font("Consolas", Font.BOLD, settings.fontSize));

        addMouseWheelListener(e -> {
            scrollPrice(-e.getWheelRotation());
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    recenter();
                    settings.autoRecenterEnabled = true;
                    repaint();
                }
            }
        });
    }

    public void setPips(double pips) {
        this.pips = pips;
    }

    public void updateSnapshot(DomSnapshot snapshot) {
        this.currentSnapshot = snapshot;
        int ltp = snapshot.lastTradePrice();

        int targetPrice = ltp;
        if (targetPrice == 0) {
            if (snapshot.bestBid() != Integer.MIN_VALUE && snapshot.bestAsk() != Integer.MAX_VALUE) {
                targetPrice = (snapshot.bestBid() + snapshot.bestAsk()) / 2;
            } else if (snapshot.bestBid() != Integer.MIN_VALUE) {
                targetPrice = snapshot.bestBid();
            } else if (snapshot.bestAsk() != Integer.MAX_VALUE) {
                targetPrice = snapshot.bestAsk();
            }
        }

        if (centerPrice == 0 && targetPrice != 0) {
            centerPrice = targetPrice;
        }

        if (settings.autoRecenterEnabled && targetPrice != 0) {
            int drift = Math.abs(centerPrice - targetPrice);
            if (drift >= settings.recenterTicksThreshold) {
                centerPrice = targetPrice;
            }
        }
        repaint();
    }

    public void scrollPrice(int ticks) {
        centerPrice += ticks;
        settings.autoRecenterEnabled = false;
        repaint();
    }

    public void recenter() {
        if (currentSnapshot != null) {
            int ltp = currentSnapshot.lastTradePrice();
            if (ltp != 0) {
                centerPrice = ltp;
            } else if (currentSnapshot.bestBid() != Integer.MIN_VALUE) {
                centerPrice = (currentSnapshot.bestBid() + currentSnapshot.bestAsk()) / 2;
            }
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("Consolas", Font.BOLD, settings.fontSize));

        int w = getWidth();
        int h = getHeight();

        // --- LAYOUT ---
        int wPrice = 60;
        int wVel = 35;
        int wReload = 35;
        int wQty = 40;

        int fixedWidth = wPrice + wVel + (wReload * 2) + (wQty * 2);
        int remaining = Math.max(0, w - fixedWidth);

        int wFP_5m = (int) (remaining * 0.22);
        int wFP_Trd = (int) (remaining * 0.18);
        int wFP_Vol = (int) (remaining * 0.32);
        int wDelta = remaining - wFP_5m - wFP_Trd - wFP_Vol;

        int x1 = 0; // Price
        int xVel = x1 + wPrice; // Velocity
        int x2 = xVel + wVel; // Bid Reload
        int x3 = x2 + wReload; // Bid Qty
        int x4 = x3 + wQty; // 5m Vol
        int x5 = x4 + wFP_5m; // Ask Qty
        int x6 = x5 + wQty; // Ask Reload
        int x7 = x6 + wReload; // 5m Trades
        int x8 = x7 + wFP_Trd; // Volume
        int x9 = x8 + wFP_Vol; // Delta

        // 1. GLOBAL BACKGROUND
        g2.setColor(settings.colBg);
        g2.fillRect(0, HEADER_HEIGHT, w, h - HEADER_HEIGHT);

        // 2. COLUMN STRIPS
        g2.setColor(settings.colPriceBg);
        g2.fillRect(x1, HEADER_HEIGHT, wPrice, h - HEADER_HEIGHT);

        g2.setColor(settings.colBidColBg);
        g2.fillRect(x3, HEADER_HEIGHT, wQty, h - HEADER_HEIGHT);

        g2.setColor(settings.colAskColBg);
        g2.fillRect(x5, HEADER_HEIGHT, wQty, h - HEADER_HEIGHT);

        // --- HEADERS ---
        drawHeaders(g2, w, x1, xVel, x2, x3, x4, x5, x6, x7, x8, x9,
                wPrice, wVel, wReload, wQty, wFP_5m, wFP_Trd, wFP_Vol, wDelta);

        // --- STATUS INDICATOR ---
        drawStatusIndicator(g2);

        if (currentSnapshot == null)
            return;

        rowHeight = Math.max(1, settings.rowSize);
        int rowsVisible = Math.max(1, (h - HEADER_HEIGHT) / rowHeight);
        int topPrice = centerPrice + (rowsVisible / 2);

        // Pre-compute max volume and max delta for histogram scaling
        long maxVolume = 1;
        long maxDelta = 1;
        for (int i = 0; i < rowsVisible; i++) {
            int p = topPrice - i;
            DomModel.FootprintData fp = currentSnapshot.sessionFp().get(p);
            if (fp != null) {
                long total = fp.askVol + fp.bidVol;
                if (total > maxVolume)
                    maxVolume = total;
                long absDelta = Math.abs(fp.askVol - fp.bidVol);
                if (absDelta > maxDelta)
                    maxDelta = absDelta;
            }
        }

        // --- DRAW ROWS ---
        for (int i = 0; i < rowsVisible; i++) {
            int price = topPrice - i;
            int y = HEADER_HEIGHT + (i * rowHeight);

            // PRICE
            drawPriceCell(g2, price, x1, y, wPrice, rowHeight);

            // VELOCITY
            // VELOCITY
            if (currentSnapshot.priceRecordedVelocity().containsKey(price)) {
                int val = currentSnapshot.priceRecordedVelocity().get(price);
                g2.setColor(settings.colVelocityText);
                drawCenteredString(g2, String.valueOf(val), xVel, y, wVel, rowHeight);
            }

            int bestBid = currentSnapshot.bestBid();
            int bestAsk = currentSnapshot.bestAsk();

            // BID RELOAD
            drawReloadIfRelevant(g2, currentSnapshot.bidReloads().get(price), price, bestBid, true, x2, y, wReload,
                    rowHeight);

            // BID QTY
            if (currentSnapshot.bids().containsKey(price)) {
                int val = currentSnapshot.bids().get(price);
                g2.setColor(settings.colBidBar);
                g2.fillRect(x3, y, wQty, rowHeight);

                // Text Color Logic: Highlight if significant
                if (val >= settings.minDepthHighlight) {
                    g2.setColor(settings.colTextHighlight); // Yellow
                } else {
                    g2.setColor(settings.colTextOnBar); // White
                }
                drawCenteredString(g2, String.valueOf(val), x3, y, wQty, rowHeight);

                // Iceberg chunk dot
                if (settings.icebergDetectionEnabled && currentSnapshot.bidIcebergChunks().containsKey(price)) {
                    drawIcebergDot(g2, x3, y, wQty, rowHeight, true);
                }
            }

            // 5M VOL (Footprint)
            if (currentSnapshot.rollingFp().containsKey(price)) {
                drawFootprintCell(g2, x4, y, wFP_5m, rowHeight, currentSnapshot.rollingFp().get(price), false);
            }

            // ASK QTY
            if (currentSnapshot.asks().containsKey(price)) {
                int val = currentSnapshot.asks().get(price);
                g2.setColor(settings.colAskBar);
                g2.fillRect(x5, y, wQty, rowHeight);

                // Text Color Logic
                if (val >= settings.minDepthHighlight) {
                    g2.setColor(settings.colTextHighlight); // Yellow
                } else {
                    g2.setColor(settings.colTextOnBar); // White
                }
                drawCenteredString(g2, String.valueOf(val), x5, y, wQty, rowHeight);

                // Iceberg chunk dot
                if (settings.icebergDetectionEnabled && currentSnapshot.askIcebergChunks().containsKey(price)) {
                    drawIcebergDot(g2, x5, y, wQty, rowHeight, false);
                }
            }

            // ASK RELOAD
            drawReloadIfRelevant(g2, currentSnapshot.askReloads().get(price), price, bestAsk, false, x6, y, wReload,
                    rowHeight);

            // 5M TRADE COUNT
            if (currentSnapshot.rollingFp().containsKey(price)) {
                drawFootprintCell(g2, x7, y, wFP_Trd, rowHeight, currentSnapshot.rollingFp().get(price), true);
            }

            // VOLUME (session total with histogram)
            if (currentSnapshot.sessionFp().containsKey(price)) {
                drawVolumeCell(g2, x8, y, wFP_Vol, rowHeight, currentSnapshot.sessionFp().get(price), maxVolume);
            }

            // DELTA (session delta with gradient histogram)
            if (currentSnapshot.sessionFp().containsKey(price)) {
                drawDeltaCell(g2, x9, y, wDelta, rowHeight, currentSnapshot.sessionFp().get(price), maxDelta);
            }

            // Grid Line
            g2.setColor(settings.colGrid);
            g2.drawLine(0, y + rowHeight, w, y + rowHeight);
        }

        // Vertical Grid Lines
        drawGridLines(g2, h, xVel, x2, x3, x4, x5, x6, x7, x8, x9);
    }

    // --- DRAW HELPERS ---

    private void drawStatusIndicator(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        if (settings.autoRecenterEnabled) {
            g2.setColor(Color.GREEN);
            g2.drawString("[A]", 2, 10);
        } else {
            g2.setColor(Color.GRAY);
            g2.drawString("[M]", 2, 10);
        }
    }

    private void drawReloadIfRelevant(Graphics2D g2, Integer val, int price, int bestPrice, boolean isBid, int x, int y,
            int w, int h) {
        if (val == null || val == 0)
            return;
        boolean isRelevant = false;
        if (isBid && bestPrice != Integer.MIN_VALUE) {
            if (price <= bestPrice && price >= (bestPrice - 10))
                isRelevant = true;
        } else if (!isBid && bestPrice != Integer.MAX_VALUE) {
            if (price >= bestPrice && price <= (bestPrice + 10))
                isRelevant = true;
        }

        if (isRelevant) {
            drawReloadCell(g2, val, x, y, w, h);
        }
    }

    private void drawHeaders(Graphics2D g2, int w, int x1, int xVel, int x2, int x3, int x4, int x5, int x6, int x7,
            int x8, int x9,
            int wPrice, int wVel, int wReload, int wQty, int wFP_5m, int wFP_Trd, int wFP_Vol, int wDelta) {
        g2.setColor(settings.colHeaderBg);
        g2.fillRect(0, 0, w, HEADER_HEIGHT);
        g2.setColor(settings.colHeaderText);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

        drawHeaderString(g2, "Price", x1, wPrice);
        drawHeaderString(g2, "Vel", xVel, wVel);
        drawHeaderString(g2, "B-Rld", x2, wReload);
        drawHeaderString(g2, "Bid", x3, wQty);
        drawHeaderString(g2, "5m Vol", x4, wFP_5m);
        drawHeaderString(g2, "Ask", x5, wQty);
        drawHeaderString(g2, "A-Rld", x6, wReload);
        drawHeaderString(g2, "5m Trd", x7, wFP_Trd);
        drawHeaderString(g2, "Volume", x8, wFP_Vol);
        drawHeaderString(g2, "Delta", x9, wDelta);
    }

    private void drawPriceCell(Graphics2D g2, int price, int x, int y, int w, int h) {
        if (price == currentSnapshot.lastTradePrice()) {
            g2.setColor(settings.colLtpBg);
            g2.fillRect(x, y, w, h);
            g2.setColor(settings.colLtpText);
        }

        g2.setColor(settings.colPriceText);
        String priceStr = (pips < 1) ? String.format("%.2f", price * pips) : String.valueOf(price);
        drawCenteredString(g2, priceStr, x, y, w, h);
    }

    private void drawIcebergDot(Graphics2D g2, int x, int y, int cellWidth, int cellHeight, boolean isLeftSide) {
        int dotDiameter = Math.max(6, cellHeight / 3);
        int dotY = y + (cellHeight - dotDiameter) / 2;
        int dotX = isLeftSide ? x + 2 : x + cellWidth - dotDiameter - 2;
        g2.setColor(settings.colIcebergDot);
        g2.fillOval(dotX, dotY, dotDiameter, dotDiameter);
    }

    private void drawReloadCell(Graphics2D g2, int val, int x, int y, int w, int h) {
        String text = (val > 0 ? "+" : "") + val;
        g2.setColor(val > 0 ? settings.colReloadPos : settings.colReloadNeg);
        drawCenteredString(g2, text, x, y, w, h);
    }

    private void drawFootprintCell(Graphics2D g, int x, int y, int w, int h, DomModel.FootprintData fp,
            boolean useCount) {
        if (useCount) {
            int delta = fp.askCnt - fp.bidCnt;
            String deltaStr = (delta > 0 ? "+" : "") + delta;
            g.setColor(settings.colTextTrdCount);
            drawCenteredString(g, deltaStr, x, y, w, h);
            return;
        }

        String sAsk = String.valueOf(fp.askVol);
        String sBid = String.valueOf(fp.bidVol);
        String xStr = " x ";
        String text = sBid + xStr + sAsk;

        FontMetrics fm = g.getFontMetrics();
        int wBid = fm.stringWidth(sBid);
        int wX = fm.stringWidth(xStr);
        int startX = x + (w - fm.stringWidth(text)) / 2;
        int textY = y + (h / 2) + (fm.getAscent() / 2) - 1;

        g.setColor(settings.colFpBid);
        g.drawString(sBid, startX, textY);
        g.setColor(settings.colFpX);
        g.drawString(xStr, startX + wBid, textY);
        g.setColor(settings.colFpAsk);
        g.drawString(sAsk, startX + wBid + wX, textY);
    }

    private void drawGridLines(Graphics2D g2, int h, int... xs) {
        g2.setColor(settings.colGrid);
        for (int x : xs)
            g2.drawLine(x, 0, x, h);
    }

    private void drawHeaderString(Graphics2D g, String text, int x, int w) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x + (w - fm.stringWidth(text)) / 2, 16);
    }

    private void drawCenteredString(Graphics g, String text, int x, int y, int w, int h) {
        FontMetrics fm = g.getFontMetrics();
        int textY = y + (h / 2) + (fm.getAscent() / 2) - 1;
        g.drawString(text, x + (w - fm.stringWidth(text)) / 2, textY);
    }

    private void drawDeltaCell(Graphics2D g, int x, int y, int w, int h,
            DomModel.FootprintData fp, long maxDelta) {
        long delta = fp.askVol - fp.bidVol;
        if (delta == 0)
            return;

        boolean positive = delta > 0;
        double ratio = (double) Math.abs(delta) / maxDelta;
        int barWidth = Math.max(1, (int) (w * ratio));

        // Gradient: transparent edge -> full color
        Color baseColor = positive ? settings.colDeltaPos : settings.colDeltaNeg;
        Color fadeColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 40);

        // Bar grows from left; gradient fades from left (transparent) to right (solid)
        Paint oldPaint = g.getPaint();
        g.setPaint(new GradientPaint(x, y, fadeColor, x + barWidth, y, baseColor));
        g.fillRect(x, y, barWidth, h);
        g.setPaint(oldPaint);

        // Delta text, right-aligned
        String text = (positive ? "+" : "") + delta;
        g.setColor(baseColor);
        FontMetrics fm = g.getFontMetrics();
        int textY = y + (h / 2) + (fm.getAscent() / 2) - 1;
        g.drawString(text, x + w - fm.stringWidth(text) - 4, textY);
    }

    private void drawVolumeCell(Graphics2D g, int x, int y, int w, int h,
            DomModel.FootprintData fp, long maxVolume) {
        long total = fp.askVol + fp.bidVol;
        if (total <= 0)
            return;

        // 1. Histogram bar background (grows from left)
        double ratio = (double) total / maxVolume;
        int barWidth = (int) (w * ratio);
        g.setColor(settings.colVolumeBar);
        g.fillRect(x, y, barWidth, h);

        // 2. Footprint text (bidVol x askVol) right-aligned on top
        String sAsk = String.valueOf(fp.askVol);
        String sBid = String.valueOf(fp.bidVol);
        String xStr = " x ";
        String text = sBid + xStr + sAsk;

        FontMetrics fm = g.getFontMetrics();
        int textY = y + (h / 2) + (fm.getAscent() / 2) - 1;
        int startX = x + w - fm.stringWidth(text) - 4; // right-aligned with 4px padding

        g.setColor(settings.colFpBid);
        g.drawString(sBid, startX, textY);
        g.setColor(settings.colFpX);
        g.drawString(xStr, startX + fm.stringWidth(sBid), textY);
        g.setColor(settings.colFpAsk);
        g.drawString(sAsk, startX + fm.stringWidth(sBid) + fm.stringWidth(xStr), textY);
    }
}