package com.example.userservicemodule.Repository;

import com.example.userservicemodule.Entity.Resource;
import com.example.userservicemodule.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Integer> {
    Optional<Resource> findByUserId(Integer userId);
    List<Resource> findAllByUser(User user);
}