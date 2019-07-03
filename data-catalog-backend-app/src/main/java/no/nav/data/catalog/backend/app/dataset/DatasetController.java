package no.nav.data.catalog.backend.app.dataset;

import java.util.Optional;
import java.util.UUID;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import no.nav.data.catalog.backend.app.informationtype.InformationType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/backend/dataset")
@Api(value = "Dataset", description = "REST API for Dataset", tags = {"Dataset"})
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @ApiOperation(value = "Get Dataset", tags = {"Dataset"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Dataset fetched", response = InformationType.class),
            @ApiResponse(code = 404, message = "Dataset not found"),
            @ApiResponse(code = 500, message = "Internal server error")})
    @GetMapping("/{id}")
    public ResponseEntity findForId(
            @PathVariable UUID id,
            @RequestParam(value = "includeChildren", defaultValue = "false") boolean includeChildren
    ) {
        log.info("Received request for Dataset with the id={}", id);
        Optional<DatasetResponse> datasetResponse = includeChildren ?
                datasetService.findDatasetWithChildren(id) :
                datasetService.findDataset(id);
        if (datasetResponse.isEmpty()) {
            log.info("Cannot find the Dataset with id={}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("Returned Dataset");
        return new ResponseEntity<>(datasetResponse.get(), HttpStatus.OK);
    }

}
