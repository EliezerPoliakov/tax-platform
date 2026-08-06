package com.poliakov.taxplatform.processing;

import com.poliakov.taxplatform.documents.Document;
import com.poliakov.taxplatform.documents.DocumentRepository;
import com.poliakov.taxplatform.documents.DocumentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProcessingJobService {

    private static final Logger log = LoggerFactory.getLogger(ProcessingJobService.class);

    private final ProcessingJobRepository processingJobRepository;
    private final CanonicalRecordRepository canonicalRecordRepository;
    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;

    public ProcessingJobService(
            ProcessingJobRepository processingJobRepository,
            CanonicalRecordRepository canonicalRecordRepository,
            DocumentRepository documentRepository,
            DocumentStorage documentStorage
    ) {
        this.processingJobRepository = processingJobRepository;
        this.canonicalRecordRepository = canonicalRecordRepository;
        this.documentRepository = documentRepository;
        this.documentStorage = documentStorage;
    }

    @Transactional
    public ProcessingJob createJob(Long companyId, Long documentId) {
        Document document = documentRepository.findByIdAndCompanyId(documentId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found or does not belong to company"));

        ProcessingJob job = new ProcessingJob(
                companyId,
                documentId,
                "SYNTHETIC_CSV",
                SyntheticCsvParser.VERSION,
                UUID.randomUUID().toString()
        );

        return processingJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessingJob> getJob(Long jobId, Long companyId) {
        return processingJobRepository.findByIdAndCompanyId(jobId, companyId);
    }

    @Transactional
    public void runJob(Long jobId) {
        ProcessingJob job = processingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));

        if (job.getStatus() != JobStatus.CREATED && job.getStatus() != JobStatus.FAILED) {
            log.warn("Job {} is in status {}, skipping run", jobId, job.getStatus());
            return;
        }

        job.setStatus(JobStatus.PROCESSING);
        job.setAttemptCount(job.getAttemptCount() + 1);
        processingJobRepository.save(job);

        Document document = documentRepository.findById(job.getDocumentId())
                .orElseThrow(() -> new IllegalStateException("Document not found: " + job.getDocumentId()));

        try (InputStream inputStream = documentStorage.load(document.getStorageKey())) {
            SyntheticCsvParser parser = new SyntheticCsvParser();
            List<CanonicalRecord> records = parser.parse(job.getId(), job.getCompanyId(), inputStream);

            // Persist canonical records
            canonicalRecordRepository.saveAll(records);

            // Create summary
            String summary = createSummary(records);
            job.setResultSummary(summary);
            job.setStatus(JobStatus.SUCCEEDED);
            job.setFailureCode(null);

        } catch (SyntheticCsvParser.ParseException e) {
            log.warn("Job {} failed with parse error: {}", jobId, e.getCode());
            job.setStatus(JobStatus.FAILED);
            job.setFailureCode(e.getCode());
            // Do not persist partial records: canonicalRecordRepository.saveAll was not called or we should ensure atomicity
            // Transactional ensures that if an exception is thrown, it rolls back.
            // But we WANT to persist the FAILED job status.
            // So we need to handle the exception inside the transaction or use a separate transaction for status update.
            // "Persist failed jobs rather than rolling the entire job record back."
            // "Do not persist partial canonical results after a failed parse."
        } catch (Exception e) {
            log.error("Job {} failed with unexpected error", jobId, e);
            job.setStatus(JobStatus.FAILED);
            job.setFailureCode("INTERNAL_ERROR");
        }

        processingJobRepository.save(job);
    }

    private String createSummary(List<CanonicalRecord> records) {
        int count = records.size();
        Map<String, BigDecimal> totals = records.stream()
                .collect(Collectors.groupingBy(
                        CanonicalRecord::getCurrency,
                        Collectors.reducing(BigDecimal.ZERO, CanonicalRecord::getAmount, BigDecimal::add)
                ));

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"schema_version\": \"v1\",");
        sb.append("\"record_count\": ").append(count).append(",");
        sb.append("\"totals\": {");
        String totalsStr = totals.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\": " + e.getValue())
                .collect(Collectors.joining(","));
        sb.append(totalsStr);
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }
}
