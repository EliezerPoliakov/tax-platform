package com.poliakov.taxplatform.companies;

import com.poliakov.taxplatform.identity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Long> {

    @Query("SELECT cm FROM CompanyMember cm JOIN FETCH cm.company WHERE cm.user = :user")
    List<CompanyMember> findAllByUserWithCompany(@Param("user") User user);

    Optional<CompanyMember> findByUserAndCompanyId(User user, Long companyId);

    boolean existsByUserAndCompanyId(User user, Long companyId);
}
