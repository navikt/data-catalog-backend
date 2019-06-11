package no.nav.data.catalog.backend.app.codelist;

import static no.nav.data.catalog.backend.app.codelist.CodelistService.codelists;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import no.nav.data.catalog.backend.app.common.exceptions.CodelistNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/backend/codelist")
public class CodelistController {

	private static final Logger logger = LoggerFactory.getLogger(CodelistController.class);

	@Autowired
	private CodelistService service;

	@ApiOperation(value = "Get the entire Codelist", tags = {"Codelist"})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Entire Codelist fetched", response = Map.class),
			@ApiResponse(code = 500, message = "Internal server error")})
	@GetMapping
	public Map findAll() {
		logger.info("Received a request for and returned the entire Codelist");
		return codelists;
	}

	@ApiOperation(value = "Get codes and descriptions for listName", tags = {"Codelist"})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Fetched codes with description for listName", response = Map.class),
			@ApiResponse(code = 404, message = "ListName not found"),
			@ApiResponse(code = 500, message = "Internal server error")})
	@GetMapping("/{listName}")
	public Map getCodelistByListName(@PathVariable String listName) {
		logger.info("Received a request for the codelist with listName={}", listName);
		service.isListNamePresentInCodelist(listName);
		return codelists.get(ListName.valueOf(listName.toUpperCase()));
	}

	@ApiOperation(value = "Get description for code in listName", tags = {"Codelist"})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Description fetched", response = String.class),
			@ApiResponse(code = 404, message = "Code or listName not found"),
			@ApiResponse(code = 500, message = "Internal server error")})
	@GetMapping("/{listName}/{code}")
	public String getDescriptionByListNameAndCode(@PathVariable String listName, @PathVariable String code) {
		logger.info("Received a request for the description of code={} in list={}", code, listName);
		service.isListNamePresentInCodelist(listName);
		if (!codelists.get(ListName.valueOf(listName.toUpperCase())).containsKey(code.toUpperCase())) {
			logger.error("Could not find description for code={} in list={}", code, listName);
			throw new CodelistNotFoundException(String.format("Could not find description for code=%s in list=%s", code, listName));
		}
		return codelists.get(ListName.valueOf(listName.toUpperCase())).get(code.toUpperCase());
	}

	@ApiOperation(value = "Create Codelist", tags = {"Codelists"})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Codelist successfully created", response = Codelist.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Illegal arguments"),
			@ApiResponse(code = 500, message = "Internal server error")})
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public List<Codelist> save(@Valid @RequestBody List<CodelistRequest> requests) {
		logger.info("Received a requests to create codelists");
//		try {
//			service.validateRequest(request,false);
//		}
//		catch (ValidationException e) {
//			logger.info("Could not create a codelist due to invalid request");
//			return new ResponseEntity<>(e.get(), HttpStatus.BAD_REQUEST);
//		}
		return service.save(requests);
	}

	@ApiOperation(value = "Update Codelist", tags = {"Codelists"})
	@ApiResponses(value = {
			@ApiResponse(code = 202, message = "Codelist updated", response = Codelist.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Illegal arguments"),
			@ApiResponse(code = 500, message = "Internal server error")})
	@PutMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public List<Codelist> update(@Valid @RequestBody List<CodelistRequest> requests) {
		logger.info("Received a request to update codelists");
//		try {
//			service.validateRequest(request,true);
//		}
//		catch (ValidationException e) {
//			logger.info("Could not update the codelist due to invalid request");
//			return new ResponseEntity<>(e.get(), HttpStatus.BAD_REQUEST);
//		}
		return service.update(requests);
	}

	@ApiOperation(value = "Delete Codelist", tags = {"Codelist"})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Codelist deleted"),
			@ApiResponse(code = 400, message = "Illegal arguments"),
			@ApiResponse(code = 500, message = "Internal server error")})
	@DeleteMapping("/{listName}/{code}")
	@Transactional
	public void delete(@PathVariable String listName, @PathVariable String code) {
		logger.info("Received a request to delete code={} in the list={}", code, listName);
		service.isListNamePresentInCodelist(listName);
		service.delete(ListName.valueOf(listName), code);
		logger.info("Deleted code={} in the list={}", code, listName);
	}

	@ApiOperation(value = "Refresh Codelist", tags = {"Codelist"})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Codelist refreshed"),
			@ApiResponse(code = 500, message = "Internal server error")})
	@GetMapping("/refresh")
	public ResponseEntity refresh() {
		logger.info("Refreshed the codelists");
		service.refreshCache();
		return new ResponseEntity(HttpStatus.OK);
	}
}
