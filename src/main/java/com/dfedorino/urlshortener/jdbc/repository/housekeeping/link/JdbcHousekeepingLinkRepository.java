package com.dfedorino.urlshortener.jdbc.repository.housekeeping.link;

import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.domain.repository.housekeeping.HousekeepingLinkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;

@RequiredArgsConstructor
public class JdbcHousekeepingLinkRepository implements HousekeepingLinkRepository {

  public static final String SELECT_BY_STATUS_ID = "SELECT * FROM \"link\" WHERE status_id = :statusId";
  public static final String UPDATE_STATUS_ID_BY_ID = "UPDATE \"link\" SET status_id = :statusId WHERE id = :id";
  private final JdbcClient jdbcClient;

  @Override
  public List<Link> findByStatusId(Long statusId) {
    return jdbcClient.sql(SELECT_BY_STATUS_ID)
        .param("statusId", statusId)
        .query(Link.class)
        .list();
  }

  @Override
  public void updateStatusId(Long linkId, Long newStatusId) {
    jdbcClient.sql(UPDATE_STATUS_ID_BY_ID)
        .param("statusId", newStatusId)
        .param("id", linkId)
        .update();
  }
}
