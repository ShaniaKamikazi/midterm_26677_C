package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.InsightsDTO;

import java.util.UUID;

public interface IInsightsService {

    InsightsDTO getMonthlyInsights(UUID userId);
}
