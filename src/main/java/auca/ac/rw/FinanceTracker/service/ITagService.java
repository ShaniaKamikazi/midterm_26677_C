package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.TagDTO;
import auca.ac.rw.FinanceTracker.DTO.TagRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ITagService {

    TagDTO createTag(UUID userId, TagRequest request);

    TagDTO getTagById(UUID tagId, UUID userId);

    List<TagDTO> getTagsByUser(UUID userId);

    Page<TagDTO> getTagsByUserPaginated(UUID userId, Pageable pageable);

    TagDTO updateTag(UUID tagId, UUID userId, TagRequest request);

    void deleteTag(UUID tagId, UUID userId);

    // Transaction tag management
    Set<TagDTO> getTagsForTransaction(UUID transactionId);

    void addTagsToTransaction(UUID transactionId, Set<UUID> tagIds, UUID userId);

    void removeTagFromTransaction(UUID transactionId, UUID tagId, UUID userId);

    void setTransactionTags(UUID transactionId, Set<UUID> tagIds, UUID userId);
}
