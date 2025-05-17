package com.example.userservicemodule.Service;

import com.example.userservicemodule.DTO.Slices.SliceSummaryDTO;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SliceService {

    @Autowired
    private EntityManager entityManager;

    public List<SliceSummaryDTO> obtenerResumenSlices() {
        String query = """
            SELECT s.id, s.name, u.name AS propietario, s.description AS topologia, 
                   SUM(f.vcpus) AS totalVcpu, SUM(f.ram) AS totalRam, 
                   s.status
            FROM cloud_v3.slice s
            JOIN cloud_v3.property p ON s.id = p.slice
            JOIN cloud_v3.user u ON p.user = u.id
            JOIN cloud_v3.virtual_machine vm ON vm.slice = s.id
            JOIN cloud_v3.flavor f ON vm.flavor = f.id
            GROUP BY s.id, u.name, s.name, s.description, s.status
        """;

        List<Object[]> resultados = entityManager.createNativeQuery(query).getResultList();

        List<SliceSummaryDTO> lista = new ArrayList<>();
        for (Object[] fila : resultados) {
            SliceSummaryDTO dto = new SliceSummaryDTO();
            dto.setId(String.valueOf(fila[0]));
            dto.setNombre((String) fila[1]);
            dto.setPropietario((String) fila[2]);
            dto.setTopologia((String) fila[3]);
            dto.setRecursos(fila[4] + " vCPU, " + fila[5] + "GB RAM");
            dto.setEstado((String) fila[6]);
            lista.add(dto);
        }

        return lista;
    }
}

