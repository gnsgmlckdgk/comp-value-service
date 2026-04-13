package com.finance.dart.api.abroad.dto.fmp.stocknews;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StockNewsResDto {

    /** 기사 제목 */
    private String title;

    /** 기사 본문 스니펫 */
    private String text;

    /** 원문 URL */
    private String url;

    /** 발행일 (ISO 형식) */
    private String publishedDate;

    /** 출처 사이트명 */
    private String site;

    /** 관련 종목 심볼 */
    private String symbol;
}
