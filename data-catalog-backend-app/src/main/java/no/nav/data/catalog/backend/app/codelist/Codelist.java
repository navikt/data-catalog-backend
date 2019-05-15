package no.nav.data.catalog.backend.app.codelist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.data.catalog.backend.app.common.auditing.Auditable;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "CODELIST", schema = "BACKEND_SCHEMA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Codelist extends Auditable<String> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_codelist")
	@GenericGenerator(name = "seq_codelist", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_CODELIST")})
	@NotNull
	@Column(name = "CODELIST_ID", nullable = false, updatable = false, unique = true)
	private Long id;

	@Column(name = "LIST_NAME")
	@Enumerated(EnumType.STRING)
	private ListName list;

	@Column(name = "CODE")
	private String code;

	@Column(name = "DESCRIPTION")
	private String description;
}
