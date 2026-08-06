package com.poliakov.taxplatform.companies;

import com.poliakov.taxplatform.identity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final MembershipService membershipService;

    public CompanyService(
            CompanyRepository companyRepository,
            CompanyMemberRepository companyMemberRepository,
            MembershipService membershipService
    ) {
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.membershipService = membershipService;
    }

    @Transactional
    public CompanyResponse createCompany(User user, CreateCompanyRequest request) {
        Instant now = Instant.now();
        
        Company company = new Company(request.name(), now, now);
        company = companyRepository.save(company);

        CompanyMember member = new CompanyMember(user, company, CompanyRole.OWNER, now, now);
        companyMemberRepository.save(member);

        return new CompanyResponse(company.getId(), company.getName(), CompanyRole.OWNER, company.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getUserCompanies(User user) {
        return companyMemberRepository.findAllByUserWithCompany(user).stream()
                .map(member -> new CompanyResponse(
                        member.getCompany().getId(),
                        member.getCompany().getName(),
                        member.getRole(),
                        member.getCompany().getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CompanyResponse> getCompanyForUser(User user, Long companyId) {
        return companyMemberRepository.findByUserAndCompanyId(user, companyId)
                .map(member -> new CompanyResponse(
                        member.getCompany().getId(),
                        member.getCompany().getName(),
                        member.getRole(),
                        member.getCompany().getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public boolean isMember(User user, Long companyId) {
        return membershipService.isMember(user, companyId);
    }
}
