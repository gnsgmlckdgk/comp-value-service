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
    private final RecommendHistoryService recommendHistoryService;


    /**
     * <pre>
     * 추천 해외기업 조회 (프로파일별)
     * - Redis 우선 조회, miss 시 DB 최신 스냅샷으로 폴백 (서버 재시작에도 추천 목록 유지)
     * </pre>
     * @param profileName 프로파일명
     * @return
     */
    public List<RecommendedStocksProcessor.RecommendedStockData> getAbroadCompanyByProfile(String profileName) {

        List<RecommendedStocksProcessor.RecommendedStockData> response = new LinkedList<>();

        // 프로파일별 Redis 키로 조회
        String redisKey = RedisKeyGenerator.genRecommendedStocksByProfile(profileName);
        String value = redisComponent.getValue(redisKey);

        if (value == null || value.isEmpty()) {
            // Redis miss → DB 최신 스냅샷으로 폴백 (Redis 소실/서버 재시작 대비)
            List<RecommendedStocksProcessor.RecommendedStockData> fallback =
                    recommendHistoryService.loadLatestSnapshot(profileName);
            if (!fallback.isEmpty()) {
                log.info("프로파일 '{}' Redis miss → DB 스냅샷 폴백 {}건", profileName, fallback.size());
            } else {
                log.debug("프로파일 '{}' 에 대한 추천 종목 데이터가 없습니다. Redis Key: {}", profileName, redisKey);
            }
            return fallback;
        }

        Map<String, Object> dataMap = ConvertUtil.parseObject(value, Map.class);

        if (dataMap == null || dataMap.isEmpty()) {
            log.debug("프로파일 '{}' 데이터 파싱 실패 또는 빈 데이터. Redis Key: {}", profileName, redisKey);
            return response;
        }

        for(Map.Entry<String, Object> ent : dataMap.entrySet()) {

            String key = ent.getKey();
            Object data = ent.getValue();

            if(log.isDebugEnabled()) log.debug("프로파일 '{}' 추천 기업 응답 세팅 [{}]", profileName, key);
            response.add(ConvertUtil.parseObject(data, RecommendedStocksProcessor.RecommendedStockData.class));
        }

        log.debug("프로파일 '{}' 추천 종목 조회 완료: {}건", profileName, response.size());

        return response;
    }

    /**
     * <pre>
     * 추천 해외기업 조회 (Deprecated)
     * - Redis 저장값
     * @deprecated Use {@link #getAbroadCompanyByProfile(String)} instead
     * </pre>
     * @return
     */
    @Deprecated
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
