package com.marketplace.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;

/**
 * SQLite TEXT columns: Hibernate's default Instant mapping can persist epoch millis as a bare
 * number, which sqlite-jdbc cannot parse as TIMESTAMP. Store ISO-8601; still read legacy numeric
 * strings.
 */
@Converter(autoApply = true)
public class InstantStringConverter implements AttributeConverter<Instant, String> {

    @Override
    public String convertToDatabaseColumn(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    @Override
    public Instant convertToEntityAttribute(String db) {
        if (db == null || db.isEmpty()) {
            return null;
        }
        String s = db.trim();
        if (!s.isEmpty() && s.chars().allMatch(Character::isDigit)) {
            return Instant.ofEpochMilli(Long.parseLong(s));
        }
        return Instant.parse(s);
    }
}
