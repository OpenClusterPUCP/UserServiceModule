package com.example.userservicemodule.Repository;

import com.example.userservicemodule.Entity.VirtualMachine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VirtualMachineRepository extends JpaRepository<VirtualMachine, Integer> {
}
