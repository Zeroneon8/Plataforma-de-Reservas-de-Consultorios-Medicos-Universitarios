package com.githubzs.plataforma_reservas_medicas.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.DoctorProductivityResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.NoShowPatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.OfficeOccupancyResponse;
import com.githubzs.plataforma_reservas_medicas.domine.dto.DoctorRankingStatsDto;
import com.githubzs.plataforma_reservas_medicas.domine.dto.OfficeOccupancyDto;
import com.githubzs.plataforma_reservas_medicas.domine.dto.PatientNoShowStatsDto;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.OfficeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.PatientRepository;
import com.githubzs.plataforma_reservas_medicas.services.ReportService;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError.FieldViolation;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OfficeRepository officeRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OfficeOccupancyResponse> getOfficeOccupancy(LocalDate from, LocalDate to) {
        if (from == null) {
            throw new ValidationException("From date is required",
                List.of(new FieldViolation("from", "is required")));
        }
        if (to == null) {
            throw new ValidationException("To date is required",
                List.of(new FieldViolation("to", "is required")));
        }

        if (from.isAfter(to)) {
            throw new ValidationException("From date must be before or equal to to date",
                List.of(new FieldViolation("from", "must be before or equal to to date")));
        }

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay().minusSeconds(1);

        List<OfficeOccupancyDto> dtos = officeRepository.calculateOfficeOccupancyBetween(fromDateTime, toDateTime);

        return dtos.stream()
                .map(dto -> new OfficeOccupancyResponse(
                        dto.getOfficeId(),
                        dto.getOfficeName(),
                        dto.getOfficeLocation(),
                        dto.getRoomNumber(),
                        dto.getAppointmentCount(),
                        dto.getMinutesOccupied(),
                        dto.getNoShowCount()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProductivityResponse> getDoctorProductivity() {
        
        List<DoctorRankingStatsDto> dtos = doctorRepository.rankDoctorsByCompletedAppointments();

        // Mappear a response y agregar posición de ranking
        List<DoctorProductivityResponse> responses = new java.util.ArrayList<>();
        long rankingPosition = 1;
        for (DoctorRankingStatsDto dto : dtos) {
            responses.add(new DoctorProductivityResponse(
                    rankingPosition,
                    dto.getDoctorId(),
                    dto.getDoctorName(),
                    dto.getCompletedAppointments()));
            rankingPosition++;
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoShowPatientResponse> getNoShowPatients(LocalDate from, LocalDate to) {
        if (from == null) {
            throw new ValidationException("From date is required",
                List.of(new FieldViolation("from", "is required")));
        }
        if (to == null) {
            throw new ValidationException("To date is required",
                List.of(new FieldViolation("to", "is required")));
        }

        if (from.isAfter(to)) {
            throw new ValidationException("From date must be before or equal to to date",
                List.of(new FieldViolation("from", "must be before or equal to to date")));
        }

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay().minusSeconds(1);

        List<PatientNoShowStatsDto> dtos = patientRepository.countPatientsNoShow(fromDateTime, toDateTime);

        return dtos.stream()
                .map(dto -> new NoShowPatientResponse(
                        dto.getPatientId(),
                        dto.getPatientName(),
                        dto.getNoShowCount()))
                .toList();
    }

}
