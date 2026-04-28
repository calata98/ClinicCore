package com.cliniccore.patient.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientJpaRepository extends JpaRepository<PatientEntity, UUID> {

	@Query("""
			select p
			from PatientEntity p
			where p.clinicId = :clinicId
			  and p.active = true
			  and (
			    lower(p.firstName) like lower(concat('%', :search, '%'))
			    or lower(p.lastName) like lower(concat('%', :search, '%'))
			    or lower(coalesce(p.email, '')) like lower(concat('%', :search, '%'))
			    or lower(coalesce(p.phone, '')) like lower(concat('%', :search, '%'))
			  )
			order by p.lastName asc, p.firstName asc
			""")
	List<PatientEntity> searchActive(@Param("clinicId") UUID clinicId, @Param("search") String search);

	List<PatientEntity> findTop25ByClinicIdAndActiveTrueOrderByLastNameAscFirstNameAsc(UUID clinicId);

	long countByClinicIdAndActiveTrue(UUID clinicId);
}
