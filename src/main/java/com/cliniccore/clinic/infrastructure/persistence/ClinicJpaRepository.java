package com.cliniccore.clinic.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicJpaRepository extends JpaRepository<ClinicEntity, UUID> {
}
