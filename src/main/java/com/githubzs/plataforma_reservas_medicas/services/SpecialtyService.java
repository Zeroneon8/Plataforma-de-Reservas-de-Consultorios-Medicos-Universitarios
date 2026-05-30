package  com.githubzs.plataforma_reservas_medicas.services;

import java.util.List;

import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.*;

public interface SpecialtyService {

    SpecialtyResponse create(SpecialtyCreateRequest request); 

    List<SpecialtySummaryResponse> findAll(); 

    SpecialtyResponse findByName(String name);

    boolean existsByName(String name);

}