package com.dfedorino.urlshortener.service.business;

import com.dfedorino.urlshortener.domain.model.user.User;
import com.dfedorino.urlshortener.domain.repository.business.user.UserRepository;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final TransactionTemplate tx;

  public User create(String uuid) {
    var user = new User(uuid);
    safeTx($ -> userRepository.save(user));
    return user;
  }

  public Optional<User> find(String uuid) {
    return safeTx($ -> userRepository.findByUuid(uuid));
  }

  private <T> T safeTx(TransactionCallback<T> callback) {
    return Objects.requireNonNull(tx.execute(callback));
  }
}
