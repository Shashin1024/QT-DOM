package com.shashin.bookmap.dom;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public record DomSnapshot(
        ConcurrentSkipListMap<Integer, Integer> bids,
        ConcurrentSkipListMap<Integer, Integer> asks,
        ConcurrentSkipListMap<Integer, Integer> bidReloads,
        ConcurrentSkipListMap<Integer, Integer> askReloads,
        ConcurrentSkipListMap<Integer, DomModel.FootprintData> sessionFp,
        ConcurrentSkipListMap<Integer, DomModel.FootprintData> rollingFp,
        ConcurrentSkipListMap<Integer, Integer> priceRecordedVelocity,
        ConcurrentSkipListMap<Integer, Integer> bidIcebergChunks,
        ConcurrentSkipListMap<Integer, Integer> askIcebergChunks,
        int lastTradePrice,
        int lastTradeSize,
        int bestBid,
        int bestAsk
) {}