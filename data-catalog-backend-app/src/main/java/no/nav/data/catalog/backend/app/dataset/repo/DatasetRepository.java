package no.nav.data.catalog.backend.app.dataset.repo;

import no.nav.data.catalog.backend.app.dataset.Dataset;
import no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;

public interface DatasetRepository extends JpaRepository<Dataset, UUID> {

    List<Dataset> findByElasticsearchStatus(ElasticsearchStatus status);

    @Query("select d from Dataset d where not exists(select dr from DatasetRelation dr where dr.relation.parentOfId = d.id)")
    Page<Dataset> findAllRootDatasets(Pageable pageable);

    @Query(value = "select * from dataset where json_property->>'title' = ?1", nativeQuery = true)
    Optional<Dataset> findByTitle(String name);

    @Query(value = "select * from dataset where json_property->>'title' in (?1)", nativeQuery = true)
    List<Dataset> findAllByTitle(List<String> title);

    @Modifying
    @Transactional
    @Query("update Dataset set elasticsearchStatus = 'TO_BE_UPDATED' where id in ?1")
    int setSyncForDatasets(List<UUID> datasetIds);

    @Modifying
    @Transactional
    @Query("update Dataset set elasticsearchStatus = ?2 where id = ?1")
    void updateStatusForDataset(UUID datasetId, ElasticsearchStatus elasticsearchStatus);
}
