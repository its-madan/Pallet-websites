package com.saravanatimbers.palletbuilderbackend.security;

import com.saravanatimbers.palletbuilderbackend.repositories.QuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("quoteSecurity")
public class QuoteSecurity {
    public boolean isOwner(String quoteId, Authentication authentication) {
        if (authentication == null) return false;
        String authName = authentication.getName();
        return quoteRepository.findById(quoteId)
            .map(q -> {
                System.out.println("[DEBUG] isOwner check: authName=" + authName + ", quote.userId=" + q.getUserId());
                return authName.equals(q.getUserId());
            })
            .orElse(false);
    }
    @Autowired
    private QuoteRepository quoteRepository;

    public boolean isOwnerOrAdmin(String quoteId, Authentication authentication) {
        if (authentication == null) return false;
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        return quoteRepository.findById(quoteId)
            .map(q -> authentication.getName().equals(q.getUserId()))
            .orElse(false);
    }
} 