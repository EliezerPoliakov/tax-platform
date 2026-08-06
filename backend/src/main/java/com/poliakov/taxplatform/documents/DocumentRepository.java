package com.poliakov.taxplatform.documents;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByCompanyId(Long companyId);
    Optional<Document> findByIdAndCompanyId(Long id, Long companyId);
}
