package com.example.revly.service;

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

    // TODO: Create this service (or interface) with your actual embedding provider
    // Example: OpenAI, HuggingFace, Vertex AI, local model, etc.
    // It must return List<Float> of exactly 1536 dimensions.
    @Autowired
    private TextEmbeddingService textEmbeddingService;

    /**
     * Runs every 5 seconds.
     * Processes ONE job per execution to keep it simple and safe.
     * (You can change fixedRate or make it process up to N jobs if you want.)
     */
    @Scheduled(fixedRate = 5000)   // 5 seconds – adjust as needed
    @Transactional
    public void processPendingEmbeddingJobs() {
        // 1. Find the next eligible pending job (oldest first)
        Optional<PostEmbeddingJob> optJob = postEmbeddingJobRepository.findNextPendingJob(Instant.now());

        if (optJob.isEmpty()) {
            return; // nothing to do
        }

        PostEmbeddingJob job = optJob.get();

        // 2. Try to claim the job (prevents race conditions in multi-instance deployments)
        boolean claimed = postEmbeddingJobRepository.claimJob(job.getJobId()) > 0;
        if (!claimed) {
            return;
        }

        try {
            processSingleJob(job);
        } catch (Exception e) {
            handleJobFailure(job, e);
        }
    }

    /**
     * Core logic for one job – exactly matches your version-aware spec
     */
    private void processSingleJob(PostEmbeddingJob job) {
        // Load the current search document (we need its version + embedding field)
        PostSearchDocument doc = postSearchDocumentRepository.findById(job.getPost().getPostId())
                .orElseThrow(() -> new IllegalStateException("PostSearchDocument missing for post " + job.getPost().getPostId()));

        // Version check – this is the heart of "version-aware"
        if (!job.getDescriptionVersion().equals(doc.getDescriptionVersion())) {
            // Job is stale (description was updated again)
            markJobObsolete(job);
            return;
        }

        // Version is still current → generate embedding
        List<Float> embedding = textEmbeddingService.embed(job.getPost().getDescription());

        // Write the vector
        doc.setDescriptionEmbedding(embedding);
        doc.setEmbeddingStatus("ready");
        doc.setEmbeddingUpdatedAt(Instant.now());

        postSearchDocumentRepository.save(doc);

        // Mark job completed
        job.setStatus("completed");
        job.setLockedAt(null);   // release lock
        postEmbeddingJobRepository.save(job);
    }

    private void markJobObsolete(PostEmbeddingJob job) {
        job.setStatus("obsolete");
        job.setLockedAt(null);
        postEmbeddingJobRepository.save(job);
    }

    private void handleJobFailure(PostEmbeddingJob job, Exception e) {
        job.setAttemptCount(job.getAttemptCount() + 1);
        job.setLastError(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());

        if (job.getAttemptCount() >= 5) {
            // Permanent failure after max retries
            job.setStatus("failed");
            job.setLockedAt(null);
        } else {
            // Transient failure → exponential backoff (30s * 2^attempt)
            long backoffSeconds = 30L * (1L << (job.getAttemptCount() - 1)); // 30s, 60s, 120s, ...
            job.setNextAttemptAt(Instant.now().plusSeconds(backoffSeconds));
            job.setStatus("pending");
            job.setLockedAt(null);
        }

        postEmbeddingJobRepository.save(job);
    }
}