package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.SearchRequest;
import auca.ac.rw.FinanceTracker.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/search")
public class GlobalSearchController {

    private final SearchService searchService;

    public GlobalSearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<Object>>> globalSearch(
            @RequestBody SearchRequest searchRequest,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<Object> results = searchService.searchEntities(userId, searchRequest);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}



