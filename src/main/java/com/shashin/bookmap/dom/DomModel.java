package com.shashin.bookmap.dom;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;

public class DomModel {

    private final DomSettings settings;

    public DomModel(DomSettings settings) {
        this.settings = settings;
        this.lastResetTime = System.currentTimeMillis();
    }

    // --- DATA STRUCTURES ---
    private final ConcurrentSkipListMap<Integer, Integer> bids = new ConcurrentSkipListMap<>(
            (p1, p2) -> p2.compareTo(p1));
    private final ConcurrentSkipListMap<Integer, Integer> asks = new ConcurrentSkipListMap<>();

    private final ConcurrentSkipListMap<Integer, Integer> bidReloads = new ConcurrentSkipListMap<>(
            (p1, p2) -> p2.compareTo(p1));
    private final ConcurrentSkipListMap<Integer, Integer> askReloads = new ConcurrentSkipListMap<>();

    // --- ICEBERG CHUNK DETECTION ---
    private final ConcurrentSkipListMap<Integer, Integer> bidIcebergChunks = new ConcurrentSkipListMap<>(
            (p1, p2) -> p2.compareTo(p1));
    private final ConcurrentSkipListMap<Integer, Integer> askIcebergChunks = new ConcurrentSkipListMap<>();

    private final ConcurrentSkipListMap<Integer, FootprintData> sessionFp = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<Integer, FootprintData> rollingFp = new ConcurrentSkipListMap<>();

    // --- VELOCITY DATA (15s Rolling, GLOBAL) ---
    private final ConcurrentLinkedDeque<TradeRecord> velocityHistory = new ConcurrentLinkedDeque<>();
    private volatile int globalVelocityVolume = 0; // Tracks the rolling sum
    private static final long VELOCITY_WINDOW_MS = 15 * 1000;

    // NEW: Stores the Global Velocity value *at the time* a trade occurred at a
    // specific price
    private final ConcurrentSkipListMap<Integer, Integer> priceRecordedVelocity = new ConcurrentSkipListMap<>();

    private final ConcurrentLinkedDeque<TradeRecord> tradeHistory = new ConcurrentLinkedDeque<>();
    // REMOVED ROLLING_WINDOW_MS
    private volatile long lastResetTime;

    private static final int CLEANUP_DISTANCE = 15;

    private volatile int lastTradePrice = 0;
    private volatile int lastTradeSize = 0;

    private volatile int bestBid = Integer.MIN_VALUE;
    private volatile int bestAsk = Integer.MAX_VALUE;

    // --- ACTIONS ---

    public void onDepth(boolean isBid, int price, int newSize) {
        var book = isBid ? bids : asks;
        var reloadMap = isBid ? bidReloads : askReloads;
        int oldSize = book.getOrDefault(price, 0);
        int delta = newSize - oldSize;

        // 1. Detect Stacking (+) or Pulling (-)
        boolean inRange = false;
        if (isBid) {
            if (bestBid != Integer.MIN_VALUE && price >= (bestBid - 20))
                inRange = true;
            if (bestBid == Integer.MIN_VALUE)
                inRange = true;
        } else {
            if (bestAsk != Integer.MAX_VALUE && price <= (bestAsk + 20))
                inRange = true;
            if (bestAsk == Integer.MAX_VALUE)
                inRange = true;
        }

        if (delta != 0 && inRange && oldSize > 0) {
            reloadMap.compute(price, (k, v) -> {
                int current = (v == null) ? 0 : v;
                int result = current + delta;
                return (result == 0) ? null : result;
            });
        }

        // 2. Update Book & BBO
        if (newSize == 0) {
            book.remove(price);
            (isBid ? bidIcebergChunks : askIcebergChunks).remove(price);
            if (isBid && price == bestBid)
                bestBid = bids.isEmpty() ? Integer.MIN_VALUE : bids.firstKey();
            if (!isBid && price == bestAsk)
                bestAsk = asks.isEmpty() ? Integer.MAX_VALUE : asks.firstKey();
        } else {
            book.put(price, newSize);
            if (isBid && price > bestBid)
                bestBid = price;
            if (!isBid && price < bestAsk)
                bestAsk = price;

            // 3. Iceberg detection: flag levels where total size >= threshold
            if (settings.icebergDetectionEnabled) {
                var chunkMap = isBid ? bidIcebergChunks : askIcebergChunks;
                if (newSize >= settings.minIcebergChunkSize) {
                    chunkMap.put(price, newSize);
                } else {
                    chunkMap.remove(price);
                }
            }
        }
    }

    public void onTrade(int price, int size, boolean isBidAggressor) {
        lastTradePrice = price;
        lastTradeSize = size;
        long now = System.currentTimeMillis();
        boolean isBuy = isBidAggressor;

        // Standard Footprint Logic
        sessionFp.computeIfAbsent(price, k -> new FootprintData()).add(isBuy, size);
        rollingFp.computeIfAbsent(price, k -> new FootprintData()).add(isBuy, size);
        tradeHistory.add(new TradeRecord(now, price, size, isBuy));

        // --- GLOBAL VELOCITY LOGIC (15s) ---
        // 1. Add new trade to velocity history
        velocityHistory.add(new TradeRecord(now, price, size, isBuy));
        globalVelocityVolume += size;

        // 2. Prune old trades to ensure the calculation represents the exact last 15s
        pruneVelocity(now);

        // 3. STAMP the velocity at this price
        // We take the current global velocity and assign it to this price row
        priceRecordedVelocity.put(price, globalVelocityVolume);

        // Retroactively correct: the passive side's depth reduction was already counted
        // as pulling by onDepth, but it was an execution — not a cancellation.
        // isBidAggressor=true means a buy hit the ask, so correct askReloads.
        var reloadMap = isBidAggressor ? askReloads : bidReloads;
        reloadMap.compute(price, (k, v) -> {
            if (v == null) return null;
            int corrected = v + size;
            return (corrected == 0) ? null : corrected;
        });

        checkAndPerformReset(now);
    }

    public void checkAndPerformReset(long now) {
        long intervalMs = settings.footprintResetMinutes * 60 * 1000L;
        if (now - lastResetTime >= intervalMs) {
            rollingFp.clear();
            tradeHistory.clear();
            lastResetTime = now;
        }
    }

    private void pruneVelocity(long now) {
        while (!velocityHistory.isEmpty()) {
            TradeRecord rec = velocityHistory.peek();
            if (now - rec.timestamp > VELOCITY_WINDOW_MS) {
                velocityHistory.poll();
                globalVelocityVolume -= rec.size;
            } else {
                break;
            }
        }
        if (globalVelocityVolume < 0) {
            globalVelocityVolume = 0;
        }
    }

    private void pruneOutdatedReloads() {
        if (bestBid == Integer.MIN_VALUE || bids.isEmpty()) {
            bidReloads.clear();
            bidIcebergChunks.clear();
        } else {
            int minAllowed = bestBid - CLEANUP_DISTANCE;
            bidReloads.keySet().removeIf(price -> price < minAllowed || price > bestBid);
            bidIcebergChunks.keySet().removeIf(price -> price < minAllowed || price > bestBid);
        }

        if (bestAsk == Integer.MAX_VALUE || asks.isEmpty()) {
            askReloads.clear();
            askIcebergChunks.clear();
        } else {
            int maxAllowed = bestAsk + CLEANUP_DISTANCE;
            askReloads.keySet().removeIf(price -> price > maxAllowed || price < bestAsk);
            askIcebergChunks.keySet().removeIf(price -> price > maxAllowed || price < bestAsk);
        }
    }

    public DomSnapshot getSnapshot() {
        long now = System.currentTimeMillis();
        pruneOutdatedReloads();
        checkAndPerformReset(now); // Ensure we reset even if no trades come in
        pruneVelocity(now);

        return new DomSnapshot(
                bids.clone(), asks.clone(),
                bidReloads.clone(), askReloads.clone(),
                deepCopy(sessionFp), deepCopy(rollingFp),
                priceRecordedVelocity.clone(),
                bidIcebergChunks.clone(), askIcebergChunks.clone(),
                lastTradePrice, lastTradeSize, bestBid, bestAsk);
    }

    private ConcurrentSkipListMap<Integer, FootprintData> deepCopy(ConcurrentSkipListMap<Integer, FootprintData> src) {
        ConcurrentSkipListMap<Integer, FootprintData> copy = new ConcurrentSkipListMap<>();
        src.forEach((k, v) -> copy.put(k, new FootprintData(v)));
        return copy;
    }

    public static class FootprintData {
        public volatile long askVol = 0;
        public volatile long bidVol = 0;
        public volatile int askCnt = 0;
        public volatile int bidCnt = 0;

        public FootprintData() {
        }

        public FootprintData(FootprintData other) {
            this.askVol = other.askVol;
            this.bidVol = other.bidVol;
            this.askCnt = other.askCnt;
            this.bidCnt = other.bidCnt;
        }

        public void add(boolean isBuy, int size) {
            if (isBuy) {
                askVol += size;
                askCnt++;
            } else {
                bidVol += size;
                bidCnt++;
            }
        }

        public void subtract(boolean isBuy, int size) {
            if (isBuy) {
                askVol -= size;
                askCnt--;
            } else {
                bidVol -= size;
                bidCnt--;
            }
        }

        public boolean isEmpty() {
            return askVol <= 0 && bidVol <= 0 && askCnt <= 0 && bidCnt <= 0;
        }
    }

    private record TradeRecord(long timestamp, int price, int size, boolean isBuy) {
    }
}
