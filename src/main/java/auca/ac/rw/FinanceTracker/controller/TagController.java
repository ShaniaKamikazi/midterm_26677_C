package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.TagDTO;
import auca.ac.rw.FinanceTracker.DTO.TagRequest;
import auca.ac.rw.FinanceTracker.service.ITagService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final ITagService tagService;

    public TagController(ITagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagDTO>> createTag(
            @Valid @RequestBody TagRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        TagDTO tag = tagService.createTag(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tag created successfully", tag));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagDTO>> getTagById(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        TagDTO tag = tagService.getTagById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(tag));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagDTO>>> getMyTags(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<TagDTO> tags = tagService.getTagsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<TagDTO>>> getMyTagsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<TagDTO> tags = tagService.getTagsByUserPaginated(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TagDTO>> updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody TagRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        TagDTO tag = tagService.updateTag(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Tag updated successfully", tag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        tagService.deleteTag(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Tag deleted successfully"));
    }

    // Transaction tag management endpoints

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<Set<TagDTO>>> getTagsForTransaction(
            @PathVariable UUID transactionId) {
        Set<TagDTO> tags = tagService.getTagsForTransaction(transactionId);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    @PostMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<Void>> addTagsToTransaction(
            @PathVariable UUID transactionId,
            @RequestBody Set<UUID> tagIds,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        tagService.addTagsToTransaction(transactionId, tagIds, userId);
        return ResponseEntity.ok(ApiResponse.success("Tags added to transaction"));
    }

    @DeleteMapping("/transaction/{transactionId}/{tagId}")
    public ResponseEntity<ApiResponse<Void>> removeTagFromTransaction(
            @PathVariable UUID transactionId,
            @PathVariable UUID tagId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        tagService.removeTagFromTransaction(transactionId, tagId, userId);
        return ResponseEntity.ok(ApiResponse.success("Tag removed from transaction"));
    }

    @PutMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<Void>> setTransactionTags(
            @PathVariable UUID transactionId,
            @RequestBody Set<UUID> tagIds,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        tagService.setTransactionTags(transactionId, tagIds, userId);
        return ResponseEntity.ok(ApiResponse.success("Transaction tags updated"));
    }
}
