package com.shashin.bookmap.dom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JigsawDomPanel extends JPanel {
    private final DomSettings settings;
    private DomSnapshot currentSnapshot;
    private double pips = 1.0;

    // Layout Constants
    private final int HEADER_HEIGHT = 24;
    private int rowHeight = 20;
    private int centerPrice = 0;

    // Colors
    private static final Color COL_BG = new Color(20, 20, 20);
    private static final Color COL_GRID = new Color(45, 45, 45);
    private static final Color COL_HEADER_BG = new Color(10, 10, 10);
    private static final Color COL_HEADER_TEXT = new Color(180, 180, 180);

    private static final Color COL_PRICE_BG = new Color(40, 40, 40);
    private static final Color COL_BID_BG = new Color(0, 40, 80);
    private static final Color COL_ASK_BG = new Color(80, 20, 20);
    private static final Color COL_LTP_BG = new Color(255, 200, 0);
    private static final Color COL_VELOCITY_TEXT = new Color(255, 0, 220); // Magenta for Velocity

    private static final Color COL_TEXT_BID = new Color(100, 200, 255);
    private static final Color COL_TEXT_ASK = new Color(255, 100, 100);
    private static final Color COL_TEXT_X = new Color(100, 100, 100);
    private static final Color COL_TEXT_TRD_COUNT = new Color(220, 220, 220);

    private static final Color COL_RELOAD_POS = new Color(0, 255, 100);
    private static final Color COL_RELOAD_NEG = new Color(255, 50, 50);

    public JigsawDomPanel(DomSettings settings) {
        this.settings = settings;
        setBackground(COL_BG);
        setFont(new Font("Consolas", Font.BOLD, 12));

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

    public void setPips(double pips) { this.pips = pips; }

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
                centerPrice = currentSnapshot.bestBid();
            }
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // --- LAYOUT ---
        int wPrice = 60;
        int wVel = 35;    // Velocity Column Width
        int wReload = 35;
        int wQty = 40;

        // Calculate fixed width including the new Vel column
        int fixedWidth = wPrice + wVel + (wReload * 2) + (wQty * 2);
        int remaining = Math.max(0, w - fixedWidth);

        int wFP_5m = (int)(remaining * 0.25);
        int wFP_Trd = (int)(remaining * 0.25);
        int wFP_Tot = remaining - wFP_5m - wFP_Trd;

        // Calculate X positions
        int x1 = 0;              // Price
        int xVel = x1 + wPrice;  // Velocity
        int x2 = xVel + wVel;    // Bid Reload
        int x3 = x2 + wReload;   // Bid Qty
        int x4 = x3 + wQty;      // 5m Vol
        int x5 = x4 + wFP_5m;    // Ask Qty
        int x6 = x5 + wQty;      // Ask Reload
        int x7 = x6 + wReload;   // 5m Trades
        int x8 = x7 + wFP_Trd;   // Total Vol

        // --- HEADERS ---
        drawHeaders(g2, w, x1, xVel, x2, x3, x4, x5, x6, x7, x8,
                wPrice, wVel, wReload, wQty, wFP_5m, wFP_Trd, wFP_Tot);

        // --- STATUS INDICATOR ---
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        if (settings.autoRecenterEnabled) {
            g2.setColor(Color.GREEN);
            g2.drawString("[A]", 2, 10);
        } else {
            g2.setColor(Color.GRAY);
            g2.drawString("[M]", 2, 10);
        }

        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        if (currentSnapshot == null) return;

        int rowsVisible = settings.depthLevels;
        rowHeight = Math.max(1, (h - HEADER_HEIGHT) / rowsVisible);
        int topPrice = centerPrice + (rowsVisible / 2);

        g2.setColor(COL_PRICE_BG);
        g2.fillRect(x1, HEADER_HEIGHT, wPrice, h - HEADER_HEIGHT);

        for (int i = 0; i < rowsVisible; i++) {
            int price = topPrice - i;
            int y = HEADER_HEIGHT + (i * rowHeight);

            // 1. PRICE
            drawPriceCell(g2, price, x1, y, wPrice, rowHeight);

            // 1.5. VELOCITY (Recorded at Price)
            // Check if we have a recorded velocity for this price level
            if (currentSnapshot.priceRecordedVelocity().containsKey(price)) {
                int val = currentSnapshot.priceRecordedVelocity().get(price);
                g2.setColor(COL_VELOCITY_TEXT);
                drawCenteredString(g2, String.valueOf(val), xVel, y, wVel, rowHeight);
            }

            int bestBid = currentSnapshot.bestBid();
            int bestAsk = currentSnapshot.bestAsk();

            // 2. BID RELOAD
            if (currentSnapshot.bidReloads().containsKey(price)) {
                boolean isRelevant = false;
                if (bestBid != Integer.MIN_VALUE) {
                    if (price <= bestBid && price >= (bestBid - 10)) {
                        isRelevant = true;
                    }
                }
                int val = currentSnapshot.bidReloads().get(price);
                if (isRelevant && val != 0) {
                    drawReloadCell(g2, val, x2, y, wReload, rowHeight);
                }
            }

            // 3. BID QTY
            if (currentSnapshot.bids().containsKey(price)) {
                g2.setColor(COL_BID_BG); g2.fillRect(x3, y, wQty, rowHeight);
                g2.setColor(COL_TEXT_BID);
                drawCenteredString(g2, String.valueOf(currentSnapshot.bids().get(price)), x3, y, wQty, rowHeight);
            }

            // 4. 5 MIN VOL
            if (currentSnapshot.rollingFp().containsKey(price)) {
                drawFootprintCell(g2, x4, y, wFP_5m, rowHeight, currentSnapshot.rollingFp().get(price), false);
            }

            // 5. ASK QTY
            if (currentSnapshot.asks().containsKey(price)) {
                g2.setColor(COL_ASK_BG); g2.fillRect(x5, y, wQty, rowHeight);
                g2.setColor(COL_TEXT_ASK);
                drawCenteredString(g2, String.valueOf(currentSnapshot.asks().get(price)), x5, y, wQty, rowHeight);
            }

            // 6. ASK RELOAD
            if (currentSnapshot.askReloads().containsKey(price)) {
                boolean isRelevant = false;
                if (bestAsk != Integer.MAX_VALUE) {
                    if (price >= bestAsk && price <= (bestAsk + 10)) {
                        isRelevant = true;
                    }
                }
                int val = currentSnapshot.askReloads().get(price);
                if (isRelevant && val != 0) {
                    drawReloadCell(g2, val, x6, y, wReload, rowHeight);
                }
            }

            // 7. 5 MIN TRADE COUNT
            if (currentSnapshot.rollingFp().containsKey(price)) {
                drawFootprintCell(g2, x7, y, wFP_Trd, rowHeight, currentSnapshot.rollingFp().get(price), true);
            }

            // 8. TOTAL VOL
            if (currentSnapshot.sessionFp().containsKey(price)) {
                drawFootprintCell(g2, x8, y, wFP_Tot, rowHeight, currentSnapshot.sessionFp().get(price), false);
            }

            g2.setColor(COL_GRID);
            g2.drawLine(0, y + rowHeight, w, y + rowHeight);
        }
        drawGridLines(g2, h, xVel, x2, x3, x4, x5, x6, x7, x8);
    }

    // --- HELPERS ---
    private void drawHeaders(Graphics2D g2, int w, int x1, int xVel, int x2, int x3, int x4, int x5, int x6, int x7, int x8,
                             int wPrice, int wVel, int wReload, int wQty, int wFP_5m, int wFP_Trd, int wFP_Tot) {
        g2.setColor(COL_HEADER_BG);
        g2.fillRect(0, 0, w, HEADER_HEIGHT);
        g2.setColor(COL_HEADER_TEXT);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

        drawHeaderString(g2, "Price", x1, wPrice);
        drawHeaderString(g2, "Vel", xVel, wVel);
        drawHeaderString(g2, "B-Rld", x2, wReload);
        drawHeaderString(g2, "Bid",   x3, wQty);
        drawHeaderString(g2, "5m Vol", x4, wFP_5m);
        drawHeaderString(g2, "Ask",   x5, wQty);
        drawHeaderString(g2, "A-Rld", x6, wReload);
        drawHeaderString(g2, "5m Trd", x7, wFP_Trd);
        drawHeaderString(g2, "Total", x8, wFP_Tot);
    }

    private void drawPriceCell(Graphics2D g2, int price, int x, int y, int w, int h) {
        if (price == currentSnapshot.lastTradePrice()) {
            g2.setColor(COL_LTP_BG);
            g2.fillRect(x, y, w, h);
            g2.setColor(Color.BLACK);
        } else {
            g2.setColor(Color.WHITE);
        }
        String priceStr = (pips < 1) ? String.format("%.2f", price * pips) : String.valueOf(price);
        drawCenteredString(g2, priceStr, x, y, w, h);
    }

    private void drawReloadCell(Graphics2D g2, int val, int x, int y, int w, int h) {
        String text = (val > 0 ? "+" : "") + val;
        g2.setColor(val > 0 ? COL_RELOAD_POS : COL_RELOAD_NEG);
        drawCenteredString(g2, text, x, y, w, h);
    }

    private void drawFootprintCell(Graphics2D g, int x, int y, int w, int h, DomModel.FootprintData fp, boolean useCount) {
        if (useCount) {
            int delta = fp.askCnt - fp.bidCnt;
            String deltaStr = (delta > 0 ? "+" : "") + delta;
            g.setColor(COL_TEXT_TRD_COUNT);
            drawCenteredString(g, deltaStr, x, y, w, h);
            return;
        }

        String sAsk = String.valueOf(fp.askVol);
        String sBid = String.valueOf(fp.bidVol);
        String xStr = " x ";
        String text = sAsk + xStr + sBid;

        FontMetrics fm = g.getFontMetrics();
        int wAsk = fm.stringWidth(sAsk);
        int wX = fm.stringWidth(xStr);
        int startX = x + (w - fm.stringWidth(text)) / 2;
        int textY = y + (h / 2) + (fm.getAscent() / 2) - 1;

        g.setColor(COL_TEXT_ASK); g.drawString(sAsk, startX, textY);
        g.setColor(COL_TEXT_X);   g.drawString(xStr, startX + wAsk, textY);
        g.setColor(COL_TEXT_BID); g.drawString(sBid, startX + wAsk + wX, textY);
    }

    private void drawGridLines(Graphics2D g2, int h, int... xs) {
        g2.setColor(Color.GRAY);
        for (int x : xs) g2.drawLine(x, 0, x, h);
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
}