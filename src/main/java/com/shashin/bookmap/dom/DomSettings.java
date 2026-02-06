package com.shashin.bookmap.dom;

// REMOVED the @StrategySettingsVersion annotation to avoid dependency issues
public class DomSettings {
    public boolean autoRecenterEnabled = true;
    public boolean showPreviousCenter = false;
    public int recenterTicksThreshold = 2; // Set to 2 or 4 for AGGRESSIVE recentering
    public int depthLevels = 40;
    public int minDepthHighlight = 20;
    public int depthHighlightPercent = 8;
    public int footprintResetMinutes = 5;

    // --- GRAPHICS ---
    public int fontSize = 12;
    public int rowSize = 18;

    // --- COLORS ---
    public java.awt.Color colBg = new java.awt.Color(30, 30, 30);
    public java.awt.Color colGrid = new java.awt.Color(55, 55, 55);
    public java.awt.Color colHeaderBg = new java.awt.Color(10, 10, 10);
    public java.awt.Color colHeaderText = new java.awt.Color(180, 180, 180);

    public java.awt.Color colPriceBg = new java.awt.Color(40, 40, 40);
    public java.awt.Color colPriceText = java.awt.Color.WHITE;
    public java.awt.Color colLtpBg = new java.awt.Color(255, 200, 0);
    public java.awt.Color colLtpText = java.awt.Color.BLACK;

    public java.awt.Color colBidColBg = new java.awt.Color(0, 40, 80);
    public java.awt.Color colAskColBg = new java.awt.Color(80, 20, 20);

    public java.awt.Color colBidBar = new java.awt.Color(0, 100, 200);
    public java.awt.Color colAskBar = new java.awt.Color(200, 60, 60);

    public java.awt.Color colTextOnBar = java.awt.Color.WHITE;
    public java.awt.Color colTextHighlight = java.awt.Color.YELLOW;

    public java.awt.Color colFpBid = new java.awt.Color(255, 100, 100);
    public java.awt.Color colFpAsk = new java.awt.Color(100, 200, 255);
    public java.awt.Color colFpX = new java.awt.Color(100, 100, 100);
    public java.awt.Color colTextTrdCount = new java.awt.Color(220, 220, 220);
    public java.awt.Color colVolumeBar = new java.awt.Color(50, 70, 90);
    public java.awt.Color colVolumeText = new java.awt.Color(180, 190, 200);
    public java.awt.Color colDeltaPos = new java.awt.Color(0, 180, 80);
    public java.awt.Color colDeltaNeg = new java.awt.Color(200, 50, 50);

    public java.awt.Color colVelocityText = new java.awt.Color(255, 0, 220);

    public java.awt.Color colReloadPos = new java.awt.Color(0, 255, 100);
    public java.awt.Color colReloadNeg = new java.awt.Color(255, 50, 50);

    // --- ICEBERG DETECTION ---
    public boolean icebergDetectionEnabled = true;
    public int minIcebergChunkSize = 10;
    public java.awt.Color colIcebergDot = new java.awt.Color(0, 255, 255); // Aqua
}