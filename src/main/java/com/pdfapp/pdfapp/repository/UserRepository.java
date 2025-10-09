package com.pdfapp.pdfapp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.pdfapp.pdfapp.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username); // ✅ needed for default admin creation
}
