package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.TagDTO;
import auca.ac.rw.FinanceTracker.DTO.TagRequest;
import auca.ac.rw.FinanceTracker.exception.DuplicateResourceException;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.exception.UnauthorizedException;
import auca.ac.rw.FinanceTracker.model.Tag;
import auca.ac.rw.FinanceTracker.model.Transaction;
import auca.ac.rw.FinanceTracker.model.User;
import auca.ac.rw.FinanceTracker.repository.ITagRepository;
import auca.ac.rw.FinanceTracker.repository.ITransactionRepository;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TagService implements ITagService {

    private static final Logger log = LoggerFactory.getLogger(TagService.class);

    private final ITagRepository tagRepository;
    private final IUserRepository userRepository;
    private final ITransactionRepository transactionRepository;

    public TagService(ITagRepository tagRepository,
                      IUserRepository userRepository,
                      ITransactionRepository transactionRepository) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TagDTO createTag(UUID userId, TagRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check for duplicate tag name
        if (tagRepository.existsByNameAndUserUserIdAndDeletedFalse(request.getName(), userId)) {
            throw new DuplicateResourceException("Tag with this name already exists");
        }

        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName(request.getName());
        tag.setColor(request.getColor() != null ? request.getColor() : "#6366f1");
        tag.setDescription(request.getDescription());

        tag = tagRepository.save(tag);
        log.info("Tag created: {} for user {}", request.getName(), userId);
        return toDTO(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDTO getTagById(UUID tagId, UUID userId) {
        Tag tag = tagRepository.findByTagIdAndDeletedFalse(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        verifyOwnership(tag, userId);
        return toDTO(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getTagsByUser(UUID userId) {
        return tagRepository.findByUserUserIdAndDeletedFalse(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDTO> getTagsByUserPaginated(UUID userId, Pageable pageable) {
        return tagRepository.findByUserUserIdAndDeletedFalse(userId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public TagDTO updateTag(UUID tagId, UUID userId, TagRequest request) {
        Tag tag = tagRepository.findByTagIdAndDeletedFalse(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        verifyOwnership(tag, userId);

        // Check for duplicate name (excluding current tag)
        if (tagRepository.existsByNameAndUserUserIdAndDeletedFalse(request.getName(), userId)) {
            Tag existing = tagRepository.findByNameAndUserUserIdAndDeletedFalse(request.getName(), userId).orElse(null);
            if (existing != null && !existing.getTagId().equals(tagId)) {
                throw new DuplicateResourceException("Tag with this name already exists");
            }
        }

        tag.setName(request.getName());
        if (request.getColor() != null) tag.setColor(request.getColor());
        tag.setDescription(request.getDescription());

        tag = tagRepository.save(tag);
        log.info("Tag updated: {}", tagId);
        return toDTO(tag);
    }

    @Override
    @Transactional
    public void deleteTag(UUID tagId, UUID userId) {
        Tag tag = tagRepository.findByTagIdAndDeletedFalse(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        verifyOwnership(tag, userId);

        // Remove tag from all transactions first
        tag.getTransactions().forEach(t -> t.getTags().remove(tag));

        tag.softDelete();
        tagRepository.save(tag);
        log.info("Tag deleted: {}", tagId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<TagDTO> getTagsForTransaction(UUID transactionId) {
        return tagRepository.findByTransactionId(transactionId).stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void addTagsToTransaction(UUID transactionId, Set<UUID> tagIds, UUID userId) {
        Transaction transaction = transactionRepository.findByTransactionIdAndDeletedFalse(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this transaction");
        }

        Set<Tag> tags = tagRepository.findByTagIdInAndUserUserId(tagIds, userId);
        transaction.getTags().addAll(tags);
        transactionRepository.save(transaction);
        log.info("Added {} tags to transaction {}", tags.size(), transactionId);
    }

    @Override
    @Transactional
    public void removeTagFromTransaction(UUID transactionId, UUID tagId, UUID userId) {
        Transaction transaction = transactionRepository.findByTransactionIdAndDeletedFalse(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this transaction");
        }

        Tag tag = tagRepository.findByTagIdAndDeletedFalse(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        verifyOwnership(tag, userId);

        transaction.getTags().remove(tag);
        transactionRepository.save(transaction);
        log.info("Removed tag {} from transaction {}", tagId, transactionId);
    }

    @Override
    @Transactional
    public void setTransactionTags(UUID transactionId, Set<UUID> tagIds, UUID userId) {
        Transaction transaction = transactionRepository.findByTransactionIdAndDeletedFalse(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this transaction");
        }

        Set<Tag> tags = tagIds != null && !tagIds.isEmpty()
                ? tagRepository.findByTagIdInAndUserUserId(tagIds, userId)
                : new HashSet<>();

        transaction.setTags(tags);
        transactionRepository.save(transaction);
        log.info("Set {} tags on transaction {}", tags.size(), transactionId);
    }

    private void verifyOwnership(Tag tag, UUID userId) {
        if (!tag.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this tag");
        }
    }

    private TagDTO toDTO(Tag tag) {
        return new TagDTO(
                tag.getTagId(),
                tag.getName(),
                tag.getColor(),
                tag.getDescription(),
                tag.getTransactions() != null ? tag.getTransactions().size() : 0
        );
    }
}
