package com.example.userservicemodule.Repository;

import com.example.userservicemodule.Entity.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SliceRepository extends JpaRepository<Slice, Integer> {


}
