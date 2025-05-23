package com.example.userservicemodule.Repository;

import com.example.userservicemodule.Entity.Role;
import com.example.userservicemodule.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);


}