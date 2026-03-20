package com.example.revly.service;

import com.example.revly.exception.NonRetryableEmbeddingException;
import com.example.revly.exception.RetryableEmbeddingException;
import com.example.revly.model.PostEmbeddingJob;
import com.example.revly.model.PostSearchDocument;
import com.example.revly.repository.PostEmbeddingJobRepository;
import com.example.revly.repository.PostSearchDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class EmbeddingWorkerService {

    @Autowired
    private PostEmbeddingJobRepository postEmbeddingJobRepository;

    @Autowired
    private PostSearchDocumentRepository postSearchDocumentRepository;

    @Autowired
    private TextEmbeddingService textEmbeddingService;

    /**
     * Runs every 5 seconds.
     * Processes ONE job per execution to keep it simple and safe.
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processPendingEmbeddingJobs() {
        // 1) Find the next eligible pending job (oldest first)
        Optional<PostEmbeddingJob> optJob = postEmbeddingJobRepository.findNextPendingJob(Instant.now());
        if (optJob.isEmpty()) {
            return;
        }

        PostEmbeddingJob job = optJob.get();

        // 2) Try to claim the job (prevents race conditions in multi-instance deployments)
        boolean claimed = postEmbeddingJobRepository.claimJob(job.getJobId()) > 0;
        if (!claimed) {
            return;
        }

        try {
            processSingleJob(job);
        } catch (NonRetryableEmbeddingException e) {
            handleNonRetryableFailure(job, e);
        } catch (RetryableEmbeddingException e) {
            handleRetryableFailure(job, e);
        } catch (Exception e) {
            // Unknown errors: treat as retryable
            handleRetryableFailure(job, e);
        }
    }

    /**
     * Core logic for one job – version-aware
     */
    private void processSingleJob(PostEmbeddingJob job) {
        PostSearchDocument doc = postSearchDocumentRepository.findById(job.getPost().getPostId())
                .orElseThrow(() -> new IllegalStateException(
                        "PostSearchDocument missing for post " + job.getPost().getPostId()
                ));

        // Version check (stale job protection)
        if (!job.getDescriptionVersion().equals(doc.getDescriptionVersion())) {
            markJobObsolete(job);
            return;
        }

        // Generate embedding (may throw RetryableEmbeddingException / NonRetryableEmbeddingException)
        List<Float> embedding = textEmbeddingService.embed(job.getPost().getDescription());

        // Write embedding + mark ready
        doc.setDescriptionEmbedding(embedding);
        doc.setEmbeddingStatus("ready");
        doc.setEmbeddingUpdatedAt(Instant.now());
        postSearchDocumentRepository.save(doc);

        // Mark job completed
        job.setStatus("completed");
        job.setLockedAt(null);
        postEmbeddingJobRepository.save(job);
    }

    private void markJobObsolete(PostEmbeddingJob job) {
        job.setStatus("obsolete");
        job.setLockedAt(null);
        postEmbeddingJobRepository.save(job);
    }

    /**
     * Permanent failure: do NOT retry (bad key, invalid request, dimension mismatch, etc.)
     */
    private void handleNonRetryableFailure(PostEmbeddingJob job, Exception e) {
        job.setAttemptCount(job.getAttemptCount() + 1);
        job.setLastError(safeError(e));
        job.setStatus("failed");
        job.setLockedAt(null);
        postEmbeddingJobRepository.save(job);

        // Mark the search document failed only if this job is still current
        postSearchDocumentRepository.findById(job.getPost().getPostId()).ifPresent(doc -> {
            if (job.getDescriptionVersion().equals(doc.getDescriptionVersion())) {
                doc.setEmbeddingStatus("failed");
                postSearchDocumentRepository.save(doc);
            } else {
                // If the post changed again, this job is stale
                markJobObsolete(job);
            }
        });
    }

    /**
     * Retryable failure: rate limits, timeouts, 5xx, transient network issues.
     * Retries with exponential backoff up to 5 attempts.
     */
    private void handleRetryableFailure(PostEmbeddingJob job, Exception e) {
        job.setAttemptCount(job.getAttemptCount() + 1);
        job.setLastError(safeError(e));

        if (job.getAttemptCount() >= 5) {
            job.setStatus("failed");
            job.setLockedAt(null);
            postEmbeddingJobRepository.save(job);

            postSearchDocumentRepository.findById(job.getPost().getPostId()).ifPresent(doc -> {
                if (job.getDescriptionVersion().equals(doc.getDescriptionVersion())) {
                    doc.setEmbeddingStatus("failed");
                    postSearchDocumentRepository.save(doc);
                } else {
                    markJobObsolete(job);
                }
            });
            return;
        }

        // Exponential backoff: 30s, 60s, 120s, 240s...
        long backoffSeconds = 30L * (1L << (job.getAttemptCount() - 1));
        job.setNextAttemptAt(Instant.now().plusSeconds(backoffSeconds));
        job.setStatus("pending");
        job.setLockedAt(null);
        postEmbeddingJobRepository.save(job);

        // Keep search document in pending while retrying (only if still current)
        postSearchDocumentRepository.findById(job.getPost().getPostId()).ifPresent(doc -> {
            if (job.getDescriptionVersion().equals(doc.getDescriptionVersion())) {
                doc.setEmbeddingStatus("pending");
                postSearchDocumentRepository.save(doc);
            } else {
                markJobObsolete(job);
            }
        });
    }

    private String safeError(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = e.getClass().getSimpleName();
        }
        return msg.length() > 1000 ? msg.substring(0, 1000) + "…" : msg;
    }
}