package com.finance.dart.cointrade.dto.upbit;

import com.finance.dart.common.util.ConvertUtil;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TickerDtoTest {

    @Test
    public void testTickerDtoParsing() {
        String json = "[{\"market\": \"KRW-BTC\",\"trade_date\": \"20240121\",\"trade_time\": \"100000\",\"trade_date_kst\": \"20240121\",\"trade_time_kst\": \"190000\",\"trade_timestamp\": 1705831200000,\"opening_price\": 50000000.0,\"high_price\": 51000000.0,\"low_price\": 49000000.0,\"trade_price\": 50500000.0,\"prev_closing_price\": 50000000.0,\"change\": \"RISE\",\"change_price\": 500000.0,\"change_rate\": 0.01,\"signed_change_price\": 500000.0,\"signed_change_rate\": 0.01,\"trade_volume\": 0.5,\"acc_trade_price\": 25000000.0,\"acc_trade_price_24h\": 100000000.0,\"acc_trade_volume\": 0.5,\"acc_trade_volume_24h\": 2.0,\"highest_52_week_price\": 60000000.0,\"highest_52_week_date\": \"2023-12-01\",\"lowest_52_week_price\": 30000000.0,\"lowest_52_week_date\": \"2023-01-01\",\"timestamp\": 1705831200000}]";

        List<TickerDto> result = ConvertUtil.parseObject(json, new TypeToken<List<TickerDto>>() {});

        assertNotNull(result, "Parsing result should not be null");
        assertEquals(1, result.size(), "Should contain 1 item");
        TickerDto ticker = result.get(0);
        
        assertEquals("KRW-BTC", ticker.getMarket());
        assertEquals(50500000.0, ticker.getTradePrice(), "Trade price should match");
        assertEquals(50000000.0, ticker.getOpeningPrice(), "Opening price should match");
    }
}
