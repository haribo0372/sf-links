package ru.osipov.sf.links.services;

import java.util.Optional;
import ru.osipov.sf.links.models.User;

public interface AuthService {

  Optional<User> register(String username, String password);

  Optional<User> login(String username, String password);
}
