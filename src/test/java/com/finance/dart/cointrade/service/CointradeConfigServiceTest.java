package com.finance.dart.cointrade.service;

import com.finance.dart.cointrade.dto.upbit.TradingParisDto;
import com.finance.dart.cointrade.entity.CointradeTargetCoinEntity;
import com.finance.dart.cointrade.repository.CointradeConfigRepository;
import com.finance.dart.cointrade.repository.CointradeHoldingRepository;
import com.finance.dart.cointrade.repository.CointradeTargetCoinRepository;
import com.finance.dart.cointrade.repository.CointradeTradeHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CointradeConfigServiceTest {

    @Mock
    private CointradeConfigRepository configRepository;

    @Mock
    private CointradeTargetCoinRepository targetCoinRepository;

    @Mock
    private CointradeHoldingRepository holdingRepository;

    @Mock
    private CointradeTradeHistoryRepository tradeHistoryRepository;

    @Mock
    private UpbitService upbitService;

    @InjectMocks
    private CointradeConfigService cointradeConfigService;

    @Test
    void updateTargetCoins_ShouldSyncWithUpbitAndSetActiveCoins() {
        // Arrange
        TradingParisDto btc = new TradingParisDto();
        btc.setMarket("KRW-BTC");
        btc.setKoreanName("비트코인");

        TradingParisDto eth = new TradingParisDto();
        eth.setMarket("KRW-ETH");
        eth.setKoreanName("이더리움");

        TradingParisDto xrp = new TradingParisDto();
        xrp.setMarket("KRW-XRP");
        xrp.setKoreanName("리플");

        List<TradingParisDto> upbitMarkets = Arrays.asList(btc, eth, xrp);

        when(upbitService.getTradingPairs()).thenReturn(upbitMarkets);

        // Mock repository behavior
        // Assume BTC exists, others do not
        CointradeTargetCoinEntity existingBtc = new CointradeTargetCoinEntity();
        existingBtc.setCoinCode("KRW-BTC");
        existingBtc.setIsActive(false);

        when(targetCoinRepository.findByCoinCode("KRW-BTC")).thenReturn(Optional.of(existingBtc));
        when(targetCoinRepository.findByCoinCode("KRW-ETH")).thenReturn(Optional.empty());
        when(targetCoinRepository.findByCoinCode("KRW-XRP")).thenReturn(Optional.empty());

        // We want to activate BTC and ETH, but not XRP
        List<String> activeCoins = Arrays.asList("KRW-BTC", "KRW-ETH");

        // Act
        cointradeConfigService.updateTargetCoins(activeCoins);

        // Assert
        // Verify 3 saves (one for each market)
        verify(targetCoinRepository, times(3)).save(any(CointradeTargetCoinEntity.class));
    }
}
