package com.finance.dart.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialStatementResDTO {
    private String status = "";
    private String message = "";
    private List<FinancialStatementDTO> list;
}
