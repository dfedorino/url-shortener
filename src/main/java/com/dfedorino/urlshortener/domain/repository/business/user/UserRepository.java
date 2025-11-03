package com.dfedorino.urlshortener.domain.repository.business.user;

import com.dfedorino.urlshortener.domain.model.user.User;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByUuid(String uuid);
}
