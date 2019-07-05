package no.nav.data.catalog.backend.app.distributionchannel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import no.nav.data.catalog.backend.app.informationtype.RestResponsePage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/backend/distributionchannel")
@Api(value = "DistributionChannel", description = "REST API for DistributionChannel", tags = {"DistributionChannel"})
public class DistributionChannelController {

	@Autowired
	private DistributionChannelService service;

	@ApiOperation(value = "Get DistributionChannelById", tags = {"DistributionChannel"})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "DistributionChannel fetched", response = DistributionChannel.class),
			@ApiResponse(code = 404, message = "DistributionChannel  not found"),
			@ApiResponse(code = 500, message = "Internal server error")})
	@GetMapping("/id/{uuid}")
	public ResponseEntity getDistributionChannelById(@PathVariable UUID uuid) {
		log.info("Received request for DistributionChannel with the id={}", uuid);
		Optional<DistributionChannel> distributionChannel = service.findDistributionChannelById(uuid);
		if (distributionChannel.isEmpty()) {
			log.info("Cannot find the DistributionChannel with id={}", uuid);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		log.info("Returned DistributionChannel");
		return new ResponseEntity<>(distributionChannel.get(), HttpStatus.OK);
	}

	@ApiOperation(value = "Get all DistributionChannels", tags = {"DistributionChannel"})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "All distributionChannels fetched", response = DistributionChannel.class, responseContainer = "Page"),
			@ApiResponse(code = 404, message = "No DistributionChannel found in repository"),
			@ApiResponse(code = 500, message = "Internal server error")})
	@GetMapping
	public RestResponsePage<DistributionChannelResponse> getAllDistributionChannel(@RequestParam Map<String, String> queryMap) {
		if (queryMap.isEmpty()) {
			log.info("Received request for all DistributionChannels");
		} else {
			log.info("Received request for all DistributionChannel specified in the request{}", queryMap);
		}
		Page<DistributionChannelResponse> pagedResponse = service.getAllDistributionChannelsByQuery(queryMap);
		log.info("Returned DistributionChannels");
		return new RestResponsePage<>(pagedResponse.getContent(), pagedResponse.getPageable(), pagedResponse.getTotalElements());
	}

	@ApiOperation(value = "Count all DistributionChannels", tags = {"DistributionChannel"})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Count of distributionchannel fetched", response = Long.class),
			@ApiResponse(code = 500, message = "Internal server error")})
	@GetMapping("/count")
	public Long countAllDistributionChannels() {
		log.info("Received request for count all DistributionChannels");
		return service.getRepositoryCount();
	}

	@ApiOperation(value = "Create DistributionChannel", tags = {"DistributionChannel"})
	@ApiResponses(value = {
			@ApiResponse(code = 202, message = "DistributionChannels to be created successfully accepted", response = DistributionChannel.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Illegal arguments"),
			@ApiResponse(code = 500, message = "Internal server error")})
	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public List<DistributionChannelResponse> createDistributionChannels(@RequestBody List<DistributionChannelRequest> requests) {
		log.info("Received requests to create DistributionChannels");
		//TODO: ValidateRequest
//		service.validateRequests(requests, false);
		return service.createDistributionChannels(requests);
	}

	@ApiOperation(value = "Update DistributionChannel", tags = {"DistributionChannel"})
	@ApiResponses(value = {
			@ApiResponse(code = 202, message = "DistributionChannels to be updated successfully accepted", response = DistributionChannel.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Illegal arguments"),
			@ApiResponse(code = 500, message = "Internal server error")})
	@PutMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public List<DistributionChannelResponse> updateDistributionChannels(@RequestBody List<DistributionChannelRequest> requests) {
		log.info("Received requests to create DistributionChannels");
		//TODO: ValidateRequest
//		service.validateRequests(requests, true);
		return service.updateDistributionChannels(requests);
	}

	@ApiOperation(value = "Delete DistributionChannel", tags = {"DistributionChannel"})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "DistributionChannel deleted"),
			@ApiResponse(code = 404, message = "DistributionChannel not found"),
			@ApiResponse(code = 500, message = "Internal server error")})
	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity deleteDistributionChannelById(@PathVariable UUID id) {
		log.info("Received a request to delete DistributionChannel with id={}", id);
		Optional<DistributionChannel> fromRepository = service.findDistributionChannelById(id);
		if (fromRepository.isEmpty()) {
			log.info("Cannot find DistributionChannel with id={}", id);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		log.info("DistributionChannel with id={} has been set to be deleted during the next scheduled task", id);
		return new ResponseEntity<>(service.deleteDistributionChannel(fromRepository.get()), HttpStatus.ACCEPTED);
	}
}
