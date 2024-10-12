package com.example.scriptmaster.util;

import com.example.scriptmaster.repository.ScriptRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.stereotype.Component;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

@Component
@RequiredArgsConstructor
public final class RandomHelper {
    private final ScriptRepository scriptRepository;
    private final RandomStringGenerator alphanumericGenerator = new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy(LETTERS, DIGITS)
            .get();

    public String generateUniqueString(String scriptName) {
        String processKey = scriptName.replaceAll("\\..*$", "") + "_" + alphanumericGenerator.generate(16);

        while (scriptRepository.findByProcessKey(processKey).isPresent())
            processKey = scriptName.replaceAll("\\..*$", "") + "_" + alphanumericGenerator.generate(16);

        return processKey;
    }
}
