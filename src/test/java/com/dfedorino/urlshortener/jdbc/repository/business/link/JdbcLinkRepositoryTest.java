package com.dfedorino.urlshortener.jdbc.repository.business.link;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.domain.model.link.LinkStatus;
import com.dfedorino.urlshortener.domain.model.user.User;
import com.dfedorino.urlshortener.domain.repository.business.link.LinkRepository;
import com.dfedorino.urlshortener.domain.repository.business.user.UserRepository;
import com.dfedorino.urlshortener.jdbc.repository.AbstractJdbcRepositoryTestSkeleton;
import com.dfedorino.urlshortener.jdbc.repository.TestDataFactory;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcLinkRepositoryTest extends AbstractJdbcRepositoryTestSkeleton {

    private UserRepository userRepository;
    private LinkRepository linkRepository;

    @BeforeEach
    void setUp() {
        userRepository = ctx.getBean(UserRepository.class);
        linkRepository = ctx.getBean(LinkRepository.class);
    }

    @Test
    void save() {
        var user = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode = TestDataFactory.randomLinkCode();
        Link createdLink = tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setUserId(user.getId());
            link.setCode(randomCode);
        })));
        assertThat(createdLink).satisfies(link -> {
            assertThat(link.getId()).isOne();
            assertThat(link.getUserId()).isEqualTo(user.getId());
            assertThat(link.getStatusId()).isEqualTo(LinkStatus.ACTIVE.getId());
            assertThat(link.getCode()).isEqualTo(randomCode);
            assertThat(link.getOriginalUrl()).isEqualTo(TestDataFactory.ORIGINAL_URL);
            assertThat(link.getRedirectLimit()).isEqualTo(TestDataFactory.REDIRECT_LIMIT);
            assertThat(link.getCreatedAt()).isNotNull();
        });
    }

    @Test
    void findByUserIdAndCode() {
        var user1 = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode1 = TestDataFactory.randomLinkCode();
        var user2 = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode2 = TestDataFactory.randomLinkCode();

        Link link1 = tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setCode(randomCode1);
            link.setUserId(user1.getId());
        })));
        Link link2 = tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setCode(randomCode2);
            link.setUserId(user2.getId());
        })));

        assertThat(tx(() -> linkRepository.findByUserIdAndCode(user1.getId(), randomCode1)))
                .contains(link1);
        assertThat(tx(() -> linkRepository.findByUserIdAndCode(user1.getId(), randomCode2)))
                .isEmpty();

        assertThat(tx(() -> linkRepository.findByUserIdAndCode(user2.getId(), randomCode2)))
                .contains(link2);
        assertThat(tx(() -> linkRepository.findByUserIdAndCode(user2.getId(), randomCode1)))
                .isEmpty();
    }

    @Test
    void findByUserId() {
        var user1 = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode1 = TestDataFactory.randomLinkCode();
        var user2 = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode2 = TestDataFactory.randomLinkCode();

        Link link1 = tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setCode(randomCode1);
            link.setUserId(user1.getId());
        })));
        Link link2 = tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setCode(randomCode2);
            link.setUserId(user2.getId());
        })));

        tx(() -> {
            assertThat(linkRepository.findByUserId(user1.getId()))
                    .containsExactly(link1);
            assertThat(linkRepository.findByUserId(user2.getId()))
                    .containsExactly(link2);
        });
    }

    @Test
    void findByUserIdAndStatusId() {
        var user1 = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode1 = TestDataFactory.randomLinkCode();
        var user2 = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode2 = TestDataFactory.randomLinkCode();

        Link link1 = tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setCode(randomCode1);
            link.setUserId(user1.getId());
        })));
        Link link2 = tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setCode(randomCode2);
            link.setUserId(user2.getId());
        })));

        tx(() -> {
            assertThat(linkRepository.findByUserIdAndStatusId(user1.getId(),
                                                              LinkStatus.ACTIVE.getId()))
                    .contains(link1);
            assertThat(linkRepository.findByUserIdAndStatusId(user1.getId(),
                                                              LinkStatus.INVALID.getId()))
                    .isEmpty();
            assertThat(linkRepository.findByUserIdAndStatusId(user1.getId(),
                                                              LinkStatus.DELETED.getId()))
                    .isEmpty();

            assertThat(linkRepository.findByUserIdAndStatusId(user2.getId(),
                                                              LinkStatus.ACTIVE.getId()))
                    .contains(link2);
            assertThat(linkRepository.findByUserIdAndStatusId(user2.getId(),
                                                              LinkStatus.INVALID.getId()))
                    .isEmpty();
            assertThat(linkRepository.findByUserIdAndStatusId(user2.getId(),
                                                              LinkStatus.DELETED.getId()))
                    .isEmpty();
        });
    }

    @Test
    void updateOriginalUrlByUserIdAndCode() {
        var user = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode = TestDataFactory.randomLinkCode();
        tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setUserId(user.getId());
            link.setCode(randomCode);
        })));

        String updatedOriginalUrl = "updatedOriginalUrl";

        tx(() -> {
            assertThat(linkRepository.updateOriginalUrlByUserIdAndCode(user.getId(), randomCode,
                                                                       updatedOriginalUrl))
                    .isTrue();
            assertThat(linkRepository.findByUserIdAndCode(user.getId(), randomCode))
                    .get()
                    .satisfies(link -> assertThat(link.getOriginalUrl()).isEqualTo(
                            updatedOriginalUrl));
        });
    }

    @Test
    void updateRedirectLimitByUserIdAndCode() {
        var user = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode = TestDataFactory.randomLinkCode();
        tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setUserId(user.getId());
            link.setCode(randomCode);
        })));

        int newRedirectLimit = Integer.MAX_VALUE;
        assertThat(
                tx(() -> linkRepository.updateRedirectLimitByUserIdAndCode(user.getId(), randomCode,
                                                                           newRedirectLimit)))
                .isTrue();
        assertThat(tx(() -> linkRepository.findByUserIdAndCode(user.getId(), randomCode)))
                .get()
                .satisfies(link -> assertThat(link.getRedirectLimit()).isEqualTo(newRedirectLimit));
    }

    @Test
    void updateStatusIdByUserIdAndCode() {
        var user = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode = TestDataFactory.randomLinkCode();
        tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setUserId(user.getId());
            link.setCode(randomCode);
        })));

        tx(() -> {
            assertThat(linkRepository.updateStatusIdByUserIdAndCode(user.getId(), randomCode,
                                                                    LinkStatus.INVALID.getId()))
                    .isTrue();
            assertThat(linkRepository.findByUserIdAndCode(user.getId(), randomCode))
                    .get()
                    .satisfies(link -> assertThat(link.getStatusId()).isEqualTo(
                            LinkStatus.INVALID.getId()));
        });
    }

    @Test
    void findByStatusId() {
        var user = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode = TestDataFactory.randomLinkCode();
        Link createdLink = tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setUserId(user.getId());
            link.setCode(randomCode);
        })));

        tx(() -> {
            assertThat(linkRepository.findByStatusId(LinkStatus.ACTIVE.getId()))
                    .containsExactly(createdLink);
        });
    }

    @Test
    void findByUserIdAndOriginalUrl() {
        var user = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        var randomCode = TestDataFactory.randomLinkCode();
        Link createdLink = tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setUserId(user.getId());
            link.setCode(randomCode);
        })));

        tx(() -> {
            assertThat(linkRepository.findByUserIdAndOriginalUrl(user.getId(),
                                                                 createdLink.getOriginalUrl()))
                    .contains(createdLink);
        });
    }
}