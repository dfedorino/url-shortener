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

    public static final String USER_ID = "userId";
    public static final String STATUS_ID = "statusId";
    public static final String CODE = "code";
    public static final String ORIGINAL_URL = "originalUrl";
    public static final String REDIRECT_LIMIT = "redirectLimit";

    public static final String INSERT_INTO_LINK =
            "INSERT INTO \"link\"(user_id, status_id, code, original_url, redirect_limit) "
                    + "VALUES (:" + USER_ID + ", :" + STATUS_ID + ", :" + CODE + ", :"
                    + ORIGINAL_URL + ", :" + REDIRECT_LIMIT + ")";
    public static final String SELECT_BY_USER_ID_AND_CODE =
            "SELECT * FROM \"link\" WHERE user_id = :" + USER_ID + " AND code = :" + CODE;
    public static final String SELECT_BY_USER_ID =
            "SELECT * FROM \"link\" WHERE user_id = :" + USER_ID;
    public static final String SELECT_BY_USER_ID_AND_STATUS_ID =
            "SELECT * FROM \"link\""
                    + " WHERE user_id = :" + USER_ID + " AND status_id = :" + STATUS_ID;
    public static final String UPDATE_ORIGINAL_URL_BY_USER_ID_AND_CODE =
            "UPDATE \"link\" SET original_url = :" + ORIGINAL_URL
                    + " WHERE user_id = :" + USER_ID + " AND code = :" + CODE;
    public static final String UPDATE_REDIRECT_LIMIT_BY_USER_ID_AND_CODE =
            "UPDATE \"link\" SET redirect_limit = :" + REDIRECT_LIMIT
                    + " WHERE user_id = :" + USER_ID + " AND code = :" + CODE;
    public static final String UPDATE_STATUS_ID_BY_USER_ID_AND_CODE =
            "UPDATE \"link\" SET status_id = :statusId"
                    + " WHERE user_id = :" + USER_ID + " AND code = :" + CODE;
    public static final String SELECT_BY_STATUS_ID = "SELECT * FROM \"link\""
            + " WHERE status_id = :" + STATUS_ID;

    public static final String SELECT_BY_USER_ID_AND_ORIGINAL_URL = "SELECT * FROM \"link\""
            + " WHERE user_id = :" + USER_ID + " AND original_url = :" + ORIGINAL_URL;

    private final JdbcClient jdbcClient;

    @Override
    public Link save(Link link) {
        var keyHolder = new GeneratedKeyHolder();

        jdbcClient.sql(INSERT_INTO_LINK)
                .param(USER_ID, link.getUserId())
                .param(STATUS_ID, link.getStatusId())
                .param(CODE, link.getCode())
                .param(ORIGINAL_URL, link.getOriginalUrl())
                .param(REDIRECT_LIMIT, link.getRedirectLimit())
                .update(keyHolder);

        link.setId(KeyHolderUtil.getId(keyHolder));
        link.setCreatedAt(KeyHolderUtil.getCreatedAt(keyHolder));

        return link;
    }

    @Override
    public Optional<Link> findByUserIdAndCode(Long userId, String code) {
        return jdbcClient.sql(SELECT_BY_USER_ID_AND_CODE)
                .param(USER_ID, userId)
                .param(CODE, code)
                .query(Link.class)
                .optional();
    }

    @Override
    public List<Link> findByUserId(Long userId) {
        return jdbcClient.sql(SELECT_BY_USER_ID)
                .param(USER_ID, userId)
                .query(Link.class)
                .list();
    }

    @Override
    public List<Link> findByUserIdAndStatusId(Long userId, Long statusId) {
        return jdbcClient.sql(SELECT_BY_USER_ID_AND_STATUS_ID)
                .param(USER_ID, userId)
                .param("statusId", statusId)
                .query(Link.class)
                .list();
    }

    @Override
    public boolean updateOriginalUrlByUserIdAndCode(Long userId, String code,
                                                    String newOriginalUrl) {
        return jdbcClient.sql(UPDATE_ORIGINAL_URL_BY_USER_ID_AND_CODE)
                .param(ORIGINAL_URL, newOriginalUrl)
                .param(USER_ID, userId)
                .param(CODE, code)
                .update() == 1;
    }

    @Override
    public boolean updateRedirectLimitByUserIdAndCode(Long userId, String code,
                                                      Integer newRedirectLimit) {
        return jdbcClient.sql(UPDATE_REDIRECT_LIMIT_BY_USER_ID_AND_CODE)
                .param(REDIRECT_LIMIT, newRedirectLimit)
                .param(USER_ID, userId)
                .param(CODE, code)
                .update() == 1;
    }

    @Override
    public boolean updateStatusIdByUserIdAndCode(Long userId, String code, Long newStatusId) {
        return jdbcClient.sql(UPDATE_STATUS_ID_BY_USER_ID_AND_CODE)
                .param(STATUS_ID, newStatusId)
                .param(USER_ID, userId)
                .param("code", code)
                .update() == 1;
    }

    @Override
    public List<Link> findByStatusId(Long statusId) {
        return jdbcClient.sql(SELECT_BY_STATUS_ID)
                .param(STATUS_ID, statusId)
                .query(Link.class)
                .list();
    }

    @Override
    public Optional<Link> findByUserIdAndOriginalUrl(Long userId, String originalUrl) {
        return jdbcClient.sql(SELECT_BY_USER_ID_AND_ORIGINAL_URL)
                .param(USER_ID, userId)
                .param(ORIGINAL_URL, originalUrl)
                .query(Link.class)
                .optional();
    }
}
