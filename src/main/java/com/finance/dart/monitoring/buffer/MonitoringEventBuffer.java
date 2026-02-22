package com.finance.dart.monitoring.buffer;

import com.finance.dart.monitoring.dto.MonitoringSnapshotDto;
import com.finance.dart.monitoring.dto.TradeEventDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 모니터링 이벤트 버퍼 — CircularLogBuffer 패턴 복제
 * snapshot/trade/traffic 이벤트 구독자 관리
 */
@Component
public class MonitoringEventBuffer {

    private final List<Consumer<MonitoringSnapshotDto>> snapshotSubscribers = new CopyOnWriteArrayList<>();
    private final List<Consumer<TradeEventDto>> tradeSubscribers = new CopyOnWriteArrayList<>();
    private final List<Consumer<Map<String, Integer>>> trafficSubscribers = new CopyOnWriteArrayList<>();

    private volatile MonitoringSnapshotDto latestSnapshot;

    // === Snapshot ===

    public void publishSnapshot(MonitoringSnapshotDto snapshot) {
        this.latestSnapshot = snapshot;
        for (Consumer<MonitoringSnapshotDto> subscriber : snapshotSubscribers) {
            try {
                subscriber.accept(snapshot);
            } catch (Exception e) {
                // 구독자 전송 실패 시 무시 (연결 끊김 등)
            }
        }
    }

    public MonitoringSnapshotDto getLatestSnapshot() {
        return latestSnapshot;
    }

    public void subscribeSnapshot(Consumer<MonitoringSnapshotDto> subscriber) {
        snapshotSubscribers.add(subscriber);
    }

    public void unsubscribeSnapshot(Consumer<MonitoringSnapshotDto> subscriber) {
        snapshotSubscribers.remove(subscriber);
    }

    // === Trade ===

    public void publishTrade(TradeEventDto trade) {
        for (Consumer<TradeEventDto> subscriber : tradeSubscribers) {
            try {
                subscriber.accept(trade);
            } catch (Exception e) {
                // 구독자 전송 실패 시 무시
            }
        }
    }

    public void subscribeTrade(Consumer<TradeEventDto> subscriber) {
        tradeSubscribers.add(subscriber);
    }

    public void unsubscribeTrade(Consumer<TradeEventDto> subscriber) {
        tradeSubscribers.remove(subscriber);
    }

    // === Traffic (per-service) ===

    public void publishTraffic(Map<String, Integer> trafficData) {
        for (Consumer<Map<String, Integer>> subscriber : trafficSubscribers) {
            try {
                subscriber.accept(trafficData);
            } catch (Exception e) {
                // 구독자 전송 실패 시 무시
            }
        }
    }

    public void subscribeTraffic(Consumer<Map<String, Integer>> subscriber) {
        trafficSubscribers.add(subscriber);
    }

    public void unsubscribeTraffic(Consumer<Map<String, Integer>> subscriber) {
        trafficSubscribers.remove(subscriber);
    }
}
