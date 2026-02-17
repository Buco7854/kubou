package com.kubou.application.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class QuestionProviderFactory {

    private final Map<String, QuestionProvider> providerMap;

    public QuestionProviderFactory(List<QuestionProvider> providers) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(
                        QuestionProvider::getSourceIdentifier,
                        Function.identity()));
    }

    public QuestionProvider getProvider(String source) {
        QuestionProvider provider = providerMap.get(source);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown question source: " + source);
        }
        return provider;
    }

    public Set<String> getAvailableSources() {
        return providerMap.keySet();
    }
}
