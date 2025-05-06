package com.example.userservicemodule.Repository;

import com.example.userservicemodule.Entity.PhysicalServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhysicalServerRepository extends JpaRepository<PhysicalServer, Integer> {

    List<PhysicalServer> findByAvailabilityZone_IdAndServerTypeIn(Integer zoneId, List<String> serverTypes);


}
