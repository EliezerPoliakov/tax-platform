package com.poliakov.taxplatform.companies;

import com.poliakov.taxplatform.identity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reusable server-side membership check suitable for later document,
 * processing-job, and incident operations.
 */
@Service
public class MembershipService {

    private final CompanyMemberRepository companyMemberRepository;

    public MembershipService(CompanyMemberRepository companyMemberRepository) {
        this.companyMemberRepository = companyMemberRepository;
    }

    /**
     * Verifies if the user is a member of the specified company.
     * Rejects access even when the resource identifier is known.
     */
    @Transactional(readOnly = true)
    public boolean isMember(User user, Long companyId) {
        if (user == null || companyId == null) {
            return false;
        }
        return companyMemberRepository.existsByUserAndCompanyId(user, companyId);
    }
}
