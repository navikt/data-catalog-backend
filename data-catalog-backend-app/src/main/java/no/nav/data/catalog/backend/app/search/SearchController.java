package no.nav.data.catalog.backend.app.search;

import no.nav.data.catalog.backend.app.informationtype.InformationTypeService;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/backend/records/search")
public class SearchController {


	@Autowired
	private InformationTypeService informationTypeService;

	@GetMapping(value = "/field/{fieldName}/{fieldValue}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public SearchResult searchByField(@PathVariable String fieldName, @PathVariable String fieldValue) {

		SearchResponse searchResponse = informationTypeService.searchByField(fieldName, fieldValue);

		return SearchResult.builder()
				.searchResponse(searchResponse)
				.totalElements(searchResponse.getHits().getTotalHits())
				.results(searchResponse.getHits())
				.totalTimeInMillis(searchResponse.getTook().getMillis())
				.build();
	}
}
