package com.dfedorino.urlshortener.jdbc.repository.business.link;

import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.domain.repository.business.link.LinkRepository;
import com.dfedorino.urlshortener.jdbc.util.KeyHolderUtil;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@RequiredArgsConstructor
public class JdbcLinkRepository implements LinkRepository {

  public static final String INSERT_INTO_LINK = "INSERT INTO \"link\"(user_id, status_id, code, original_url, redirect_limit) VALUES (:userId, :statusId, :code, :originalUrl, :redirectLimit) ";
  public static final String SELECT_BY_USER_ID_AND_CODE = "SELECT * FROM \"link\" WHERE user_id = :userId AND code = :code";
  public static final String SELECT_BY_USER_ID = "SELECT * FROM \"link\" WHERE user_id = :userId";
  public static final String SELECT_BY_USER_ID_AND_STATUS_ID = "SELECT * FROM \"link\" WHERE user_id = :userId AND status_id = :statusId";
  public static final String UPDATE_ORIGINAL_URL_BY_USER_ID_AND_CODE = "UPDATE \"link\" SET original_url = :originalUrl WHERE user_id = :userId AND code = :code";
  public static final String UPDATE_REDIRECT_LIMIT_BY_USER_ID_AND_CODE = "UPDATE \"link\" SET redirect_limit = :redirectLimit WHERE user_id = :userId AND code = :code";
  public static final String UPDATE_STATUS_ID_BY_USER_ID_AND_CODE = "UPDATE \"link\" SET status_id = :statusId WHERE user_id = :userId AND code = :code";

  private final JdbcClient jdbcClient;

  @Override
  public Link save(Link link) {
    var keyHolder = new GeneratedKeyHolder();

    jdbcClient.sql(INSERT_INTO_LINK)
        .param("userId", link.getUserId())
        .param("statusId", link.getStatusId())
        .param("code", link.getCode())
        .param("originalUrl", link.getOriginalUrl())
        .param("redirectLimit", link.getRedirectLimit())
        .update(keyHolder);

    link.setId(KeyHolderUtil.getId(keyHolder));
    link.setCreatedAt(KeyHolderUtil.getCreatedAt(keyHolder));

    return link;
  }

  @Override
  public Optional<Link> findByUserIdAndCode(Long userId, String code) {
    return jdbcClient.sql(SELECT_BY_USER_ID_AND_CODE)
        .param("userId", userId)
        .param("code", code)
        .query(Link.class)
        .optional();
  }

  @Override
  public List<Link> findByUserId(Long userId) {
    return jdbcClient.sql(SELECT_BY_USER_ID)
        .param("userId", userId)
        .query(Link.class)
        .list();
  }

  @Override
  public List<Link> findByUserIdAndStatusId(Long userId, Long statusId) {
    return jdbcClient.sql(SELECT_BY_USER_ID_AND_STATUS_ID)
        .param("userId", userId)
        .param("statusId", statusId)
        .query(Link.class)
        .list();
  }

  @Override
  public boolean updateOriginalUrlByUserIdAndCode(Long userId, String code, String newOriginalUrl) {
    return jdbcClient.sql(UPDATE_ORIGINAL_URL_BY_USER_ID_AND_CODE)
        .param("originalUrl", newOriginalUrl)
        .param("userId", userId)
        .param("code", code)
        .update() == 1;
  }

  @Override
  public boolean updateRedirectLimitByUserIdAndCode(Long userId, String code,
      Integer newRedirectLimit) {
    return jdbcClient.sql(UPDATE_REDIRECT_LIMIT_BY_USER_ID_AND_CODE)
        .param("redirectLimit", newRedirectLimit)
        .param("userId", userId)
        .param("code", code)
        .update() == 1;
  }

  @Override
  public boolean updateStatusIdByUserIdAndCode(Long userId, String code, Long newStatusId) {
    return jdbcClient.sql(UPDATE_STATUS_ID_BY_USER_ID_AND_CODE)
        .param("statusId", newStatusId)
        .param("userId", userId)
        .param("code", code)
        .update() == 1;
  }

  // TODO: write test!
  @Override
  public List<Link> findByStatusId(Long statusId) {
    return jdbcClient.sql("SELECT * FROM \"link\" WHERE status_id = :statusId")
        .param("statusId", statusId)
        .query(Link.class)
        .list();
  }
}
