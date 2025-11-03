package com.dfedorino.urlshortener.jdbc.repository.business.user;

import com.dfedorino.urlshortener.domain.model.user.User;
import com.dfedorino.urlshortener.domain.repository.business.user.UserRepository;
import com.dfedorino.urlshortener.jdbc.util.KeyHolderUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@RequiredArgsConstructor
public class JdbcUserRepository implements UserRepository {

  public static final String INSERT_INTO_USER = "INSERT INTO \"user\"(uuid) VALUES (:uuid)";
  public static final String SELECT_BY_UUID = "SELECT id, uuid, created_at FROM \"user\" WHERE uuid = :uuid";

  private final JdbcClient jdbcClient;

  @Override
  public User save(User user) {
    var keyHolder = new GeneratedKeyHolder();

    jdbcClient.sql(INSERT_INTO_USER)
        .param("uuid", user.getUuid())
        .update(keyHolder);

    user.setId(KeyHolderUtil.getId(keyHolder));
    user.setCreatedAt(KeyHolderUtil.getCreatedAt(keyHolder));

    return user;
  }

  @Override
  public Optional<User> findByUuid(String uuid) {
    return jdbcClient.sql(SELECT_BY_UUID)
        .param("uuid", uuid)
        .query(User.class)
        .optional();
  }
}
