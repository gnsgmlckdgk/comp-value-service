package com.finance.dart.api.domestic.component;

import com.finance.dart.api.common.entity.StockValuationResultEntity;
import com.finance.dart.api.common.repository.StockValueRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
@Component
public class StockValueComponent {

    private final StockValueRepository stockValueRepository;

    /**
     * <pre>
     * 기업주식가치 테이블 등록
     * stock_value_result
     * </pre>
     * @param param
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createCompStockValue(StockValuationResultEntity param) {
        try {
            stockValueRepository.save(param);
            return true;
        } catch (Exception e) {
            log.error("[DB] DB 등록 중 에러발생 = {}", e.getMessage());
            return false;
        }
    }

    /**
     * 지정한날짜 이전데이터는 전부 지움
     * @param baseDate
     * @return
     */
    public long deleteAllBeforeOrOn(String baseDate) {
        long deleteCnt = stockValueRepository.deleteByBaseDateLessThanEqual(baseDate);
        return deleteCnt;
    }

}
