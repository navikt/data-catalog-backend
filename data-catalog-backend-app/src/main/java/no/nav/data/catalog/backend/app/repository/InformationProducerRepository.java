package no.nav.data.catalog.backend.app.repository;

import no.nav.data.catalog.backend.app.model.InformationProducer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InformationProducerRepository extends JpaRepository<InformationProducer, Long> {
}