package com.dfedorino.urlshortener.jdbc.repository.housekeeping.link;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.domain.model.link.LinkStatus;
import com.dfedorino.urlshortener.domain.model.user.User;
import com.dfedorino.urlshortener.domain.repository.business.link.LinkRepository;
import com.dfedorino.urlshortener.domain.repository.business.user.UserRepository;
import com.dfedorino.urlshortener.domain.repository.housekeeping.HousekeepingLinkRepository;
import com.dfedorino.urlshortener.jdbc.repository.AbstractJdbcRepositoryTestSkeleton;
import com.dfedorino.urlshortener.jdbc.repository.TestDataFactory;
import java.util.UUID;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcHousekeepingLinkRepositoryTest extends AbstractJdbcRepositoryTestSkeleton {

    private UserRepository userRepository;
    private LinkRepository linkRepository;
    private HousekeepingLinkRepository housekeepingLinkRepository;

    @BeforeEach
    void setUp() {
        userRepository = ctx.getBean(UserRepository.class);
        linkRepository = ctx.getBean(LinkRepository.class);
        housekeepingLinkRepository = ctx.getBean(HousekeepingLinkRepository.class);
    }

    @Test
    void findByStatusId() {
        User user = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        Link activeLink1 = tx(() -> linkRepository.save(TestDataFactory.newLink(link ->
                                                                                        link.setUserId(
                                                                                                user.getId()))
        ));
        Link activeLink2 = tx(() -> linkRepository.save(TestDataFactory.newLink(link ->
                                                                                        link.setUserId(
                                                                                                user.getId()))
        ));
        Link invalidLink = tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setUserId(user.getId());
            link.setStatusId(LinkStatus.INVALID.getId());
        })));
        Link deletedLink = tx(() -> linkRepository.save(TestDataFactory.newLink(link -> {
            link.setUserId(user.getId());
            link.setStatusId(LinkStatus.DELETED.getId());
        })));

        tx(() -> {
            assertThat(housekeepingLinkRepository.findByStatusId(LinkStatus.ACTIVE.getId()))
                    .containsExactlyInAnyOrder(activeLink1, activeLink2);

            assertThat(housekeepingLinkRepository.findByStatusId(LinkStatus.INVALID.getId()))
                    .containsExactly(invalidLink);

            assertThat(housekeepingLinkRepository.findByStatusId(LinkStatus.DELETED.getId()))
                    .containsExactly(deletedLink);
        });
    }

    @Test
    void updateStatusId() {
        User user = tx(() -> userRepository.save(new User(UUID.randomUUID().toString())));
        Link link = tx(
                () -> linkRepository.save(TestDataFactory.newLink(l -> l.setUserId(user.getId()))));

        tx(() -> housekeepingLinkRepository.updateStatusId(link.getId(),
                                                           LinkStatus.INVALID.getId()));

        tx(() -> {
            assertThat(housekeepingLinkRepository.findByStatusId(LinkStatus.ACTIVE.getId()))
                    .isEmpty();
            assertThat(housekeepingLinkRepository.findByStatusId(LinkStatus.INVALID.getId()))
                    .hasSize(1)
                    .first()
                    .satisfies(invalidLink -> assertThat(invalidLink.getStatusId()).isEqualTo(
                            LinkStatus.INVALID.getId()))
                    .usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
                                                      .withIgnoredFields("statusId")
                                                      .build())
                    .isEqualTo(link);
        });
    }
}