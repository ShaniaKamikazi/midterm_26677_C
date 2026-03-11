package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.exception.BadRequestException;
import auca.ac.rw.FinanceTracker.DTO.SearchRequest;
import auca.ac.rw.FinanceTracker.repository.ICategoryRepository;
import auca.ac.rw.FinanceTracker.repository.ITransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final ICategoryRepository categoryRepository;
    private final ITransactionRepository transactionRepository;

    public SearchService(ICategoryRepository categoryRepository,
                         ITransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<Object> searchEntities(UUID userId, SearchRequest searchRequest) {
        List<Object> results = new ArrayList<>();
        String term = searchRequest.getTerm();
        String entityType = searchRequest.getEntityType();

        if (term == null || term.isBlank()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        switch (entityType.toLowerCase()) {
            case "category":
                results.addAll(categoryRepository.searchCategories(userId, term));
                break;
            case "transaction":
                results.addAll(transactionRepository.searchByDescription(userId, term));
                break;
            default:
                throw new BadRequestException("Invalid entity type: " + entityType + ". Valid types: category, transaction");
        }

        log.debug("Search for '{}' in '{}' returned {} results", term, entityType, results.size());
        return results;
    }
}
