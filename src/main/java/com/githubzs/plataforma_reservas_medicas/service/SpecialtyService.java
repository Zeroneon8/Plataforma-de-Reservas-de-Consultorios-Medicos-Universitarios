package  com.githubzs.plataforma_reservas_medicas.service;

import java.util.List;

import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtySummaryResponse;

public interface SpecialtyService {

    SpecialtyResponse create(SpecialtyCreateRequest request); 

    List<SpecialtySummaryResponse> findAll(); 

    SpecialtyResponse findByName(String name);

    boolean existsByName(String name);

}