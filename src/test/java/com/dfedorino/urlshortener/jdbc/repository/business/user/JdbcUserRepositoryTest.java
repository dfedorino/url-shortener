package com.dfedorino.urlshortener.jdbc.repository.business.user;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.urlshortener.domain.model.user.User;
import com.dfedorino.urlshortener.domain.repository.business.user.UserRepository;
import com.dfedorino.urlshortener.jdbc.repository.AbstractJdbcRepositoryTestSkeleton;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcUserRepositoryTest extends AbstractJdbcRepositoryTestSkeleton {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = ctx.getBean(UserRepository.class);
    }

    @Test
    void save() {
        String randomIdentifier = UUID.randomUUID().toString();
        User created = tx(() -> userRepository.save(new User(randomIdentifier)));
        assertThat(created.getId()).isOne();
        assertThat(created.getUuid()).isEqualTo(randomIdentifier);
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    void findByUuid() {
        String randomIdentifier = UUID.randomUUID().toString();
        User created = tx(() -> userRepository.save(new User(randomIdentifier)));

        tx(() -> {
            assertThat(userRepository.findByUuid(randomIdentifier))
                    .contains(created);
            assertThat(userRepository.findByUuid(UUID.randomUUID().toString()))
                    .isEmpty();
        });
    }
}