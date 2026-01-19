package com.shashin.bookmap.dom;

// REMOVED the @StrategySettingsVersion annotation to avoid dependency issues
public class DomSettings {
    public boolean autoRecenterEnabled = true;
    public boolean showPreviousCenter = false;
    public int recenterTicksThreshold = 2; // Set to 2 or 4 for AGGRESSIVE recentering
    public int depthLevels = 40;
    public int minDepthHighlight = 20;
    public int depthHighlightPercent = 8;
}