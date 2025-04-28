package com.example.userservicemodule.Repository;

import com.example.userservicemodule.Entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    @Query("SELECT i FROM Image i WHERE i.user.id = :userId OR i.type = 'public'")
    List<Image> findImagesByUserId(@Param("userId") Integer userId);

    @Query("SELECT i FROM Image i WHERE i.type = 'public'")
    List<Image> findPublicImages();

    @Query("SELECT i FROM Image i WHERE i.user.id = :userId")
    List<Image> findPrivateImagesByUserId(@Param("userId") Integer userId);
}