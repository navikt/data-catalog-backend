package no.nav.data.catalog.backend.app.codelist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodelistRepository extends JpaRepository<Codelist, Integer> {
	Optional<Codelist> findByListAndCode(@Param("list") ListName list, @Param("code") String code);

    @Query(value = "SELECT * FROM CODELIST WHERE TRIM(UPPER(list_name)) = TRIM(UPPER(:list)) AND TRIM(UPPER(code)) = TRIM(UPPER(:code))", nativeQuery = true)
    Optional<Codelist> findByListNameAndCodeAsStrings(@Param("listName") String listName, @Param("code") String code);
}
