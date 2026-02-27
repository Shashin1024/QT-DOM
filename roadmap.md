# QTDom - Jigsaw DOM Pro Roadmap

## Completed

### Phase 1: Core DOM Engine
- [x] Real-time DOM (order book) visualization with Bookmap Layer 1 API
- [x] Footprint analysis: session-wide and 5-minute rolling window
- [x] Price velocity tracking (15-second rolling window, per-price stamping)
- [x] Order stacking/pulling detection (bid/ask reload tracking)
- [x] Per-instrument DOM windows with auto-recentering
- [x] Custom Swing rendering at 30 FPS (JigsawDomPanel)
- [x] 9-column layout: Price, Velocity, Bid Reload, Bid Qty, 5m Vol, Ask Qty, Ask Reload, 5m Trd, Total Vol

### Phase 2: Settings Panel & Bug Fixes
- [x] Fix build: replace non-existent `Layer1ApiSettingsPanelProvider` with `Layer1CustomPanelsGetter`
- [x] Fix price display: `onTrade` price is already in level/tick units (do NOT divide by pips)
- [x] Per-instrument settings via `getCustomGuiFor(alias, title)` - Bookmap handles instrument selection
- [x] "Enable" StrategyPanel with per-instrument checkbox
- [x] "DOM Pro Settings" StrategyPanel with form (Auto Recenter, Depth Levels, etc.)
- [x] Clean settings layout matching Bookmap's standard add-on design

---

## Planned

### Phase 3: Visual Enhancements
- [ ] Heatmap coloring for depth levels (gradient based on size relative to average)
- [ ] Depth highlighting when size exceeds configurable threshold (`minDepthHighlight`, `depthHighlightPercent`)
- [ ] Alternating row shading for readability
- [ ] Configurable color themes (dark/light, custom bid/ask colors)
- [ ] Font size adjustment via settings panel
- [ ] "Show Previous Center" price marker rendering

### Phase 4: Keyboard & Mouse Controls
- [ ] Scroll wheel support for manual price scrolling
- [ ] Keyboard shortcuts (Up/Down to scroll, Home to recenter)
- [ ] Right-click context menu (Reset session, Copy price, etc.)
- [ ] Drag-to-scroll support

### Phase 5: Data Features
- [ ] Session reset button (clear all footprint/reload/velocity data)
- [ ] Configurable rolling window durations (1m, 5m, 15m, custom)
- [ ] Configurable velocity window duration
- [ ] Delta column (ask vol - bid vol per price)
- [ ] Cumulative delta tracker
- [ ] Large order detection/alerting (size > threshold)
- [ ] POC (Point of Control) marker for highest volume price

### Phase 6: Persistence & State
- [ ] Save/restore settings per instrument across Bookmap sessions
- [ ] Save window size and position
- [ ] Export DOM snapshot to CSV/clipboard
- [ ] Import/export settings profiles

### Phase 7: Advanced Analysis
- [ ] Volume profile histogram overlay
- [ ] VWAP line calculation and display
- [ ] Absorption detection (large orders consumed without price move)
- [ ] Iceberg order detection heuristics
- [ ] Time & Sales tape integration column
- [ ] Multi-timeframe footprint comparison

### Phase 8: Testing & Reliability
- [ ] Unit tests for DomModel (footprint, velocity, reload logic)
- [ ] Simulated data source for offline testing
- [ ] Thread-safety stress tests
- [ ] Memory profiling for long-running sessions
- [ ] Error handling and logging improvements
