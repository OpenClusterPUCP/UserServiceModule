package com.example.userservicemodule.Repository;

import com.example.userservicemodule.Entity.Flavor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlavorRepository extends JpaRepository<Flavor, Integer> {
    @Query("SELECT f FROM Flavor f WHERE f.user.id = :userId OR f.user IS NULL")
    List<Flavor> findFlavorsByUserId(@Param("userId") Integer userId);

}