package com.finance.dart.api.common.service;

import com.finance.dart.api.common.service.schedule.RecommendedStocksProcessor;
import com.finance.dart.common.component.RedisComponent;
import com.finance.dart.common.component.RedisKeyGenerator;
import com.finance.dart.common.util.ConvertUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class RecommendedCompanyService {

    private final RedisComponent redisComponent;


    /**
     * <pre>
     * 추천 해외기업 조회
     * - Redis 저장값
     * </pre>
     * @return
     */
    public List<RecommendedStocksProcessor.RecommendedStockData> getAbroadCompany() {

        List<RecommendedStocksProcessor.RecommendedStockData> response = new LinkedList<>();

        String redisKey = RedisKeyGenerator.genRecommendedStocksAll();
        String value = redisComponent.getValue(redisKey);

        Map<String, Object> dataMap = ConvertUtil.parseObject(value, Map.class);

        for(Map.Entry<String, Object> ent : dataMap.entrySet()) {

            String key = ent.getKey();
            Object data = ent.getValue();

            if(log.isDebugEnabled()) log.debug("추천 기업 응답 세팅 [{}]", key);
            response.add(ConvertUtil.parseObject(data, RecommendedStocksProcessor.RecommendedStockData.class));
        }

        return response;
    }

}
