package no.nav.data.catalog.backend.app.dataset;

import lombok.extern.slf4j.Slf4j;
import no.nav.data.catalog.backend.app.common.exceptions.DataCatalogBackendNotFoundException;
import no.nav.data.catalog.backend.app.common.exceptions.ValidationException;
import no.nav.data.catalog.backend.app.common.utils.StreamUtils;
import no.nav.data.catalog.backend.app.common.validator.RequestValidator;
import no.nav.data.catalog.backend.app.common.validator.ValidationError;
import no.nav.data.catalog.backend.app.dataset.repo.DatasetRelation;
import no.nav.data.catalog.backend.app.dataset.repo.DatasetRelationRepository;
import no.nav.data.catalog.backend.app.dataset.repo.DatasetRepository;
import no.nav.data.catalog.backend.app.distributionchannel.DistributionChannel;
import no.nav.data.catalog.backend.app.distributionchannel.DistributionChannelRepository;
import no.nav.data.catalog.backend.app.distributionchannel.DistributionChannelShort;
import no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static no.nav.data.catalog.backend.app.common.utils.StreamUtils.nullToEmptyList;
import static no.nav.data.catalog.backend.app.common.utils.StreamUtils.safeStream;

@Slf4j
@Service
public class DatasetService extends RequestValidator<DatasetRequest> {

    private final DatasetRelationRepository datasetRelationRepository;
    private final DatasetRepository datasetRepository;
    private final DistributionChannelRepository distributionChannelRepository;

    public DatasetService(DatasetRelationRepository datasetRelationRepository, DatasetRepository datasetRepository,
                          DistributionChannelRepository distributionChannelRepository) {
        this.datasetRelationRepository = datasetRelationRepository;
        this.datasetRepository = datasetRepository;
        this.distributionChannelRepository = distributionChannelRepository;
    }

    public DatasetResponse findDatasetWithAllDescendants(UUID uuid) {
        Optional<Dataset> datasetOptional = datasetRepository.findById(uuid);
        if (datasetOptional.isEmpty()) {
            return null;
        }
        Set<DatasetRelation> relations = datasetRelationRepository.findAllDescendantsOf(uuid);
        Dataset dataset = datasetOptional.get();
        if (relations.isEmpty()) {
            return new DatasetResponse(dataset, Collections.emptyMap(), relations, true);
        }

        Set<UUID> allIds = relations.stream()
                .map(DatasetRelation::getParentOfId)
                .collect(Collectors.toSet());
        Map<UUID, Dataset> allDatasets = datasetRepository.findAllById(allIds).stream()
                .collect(toMap(Dataset::getId, Function.identity()));

        return new DatasetResponse(dataset, allDatasets, relations, true);
    }

    public Page<DatasetResponse> findAllRootDatasets(boolean includeDescendants, Pageable pageable) {
        Page<Dataset> datasets = datasetRepository.findAllRootDatasets(pageable);
        if (includeDescendants) {
            return datasets
                    .map(Dataset::getId)
                    .map(this::findDatasetWithAllDescendants);
        }
        return datasets.map(Dataset::convertToResponse);
    }

    @Transactional
    public Dataset save(DatasetRequest request, DatacatalogMaster master) {
        return datasetRepository.save(convertNew(request, master));
    }

    @Transactional
    public List<Dataset> saveAll(List<DatasetRequest> requests, DatacatalogMaster master) {
        List<Dataset> datasets = requests.stream().map(request -> convertNew(request, master)).collect(toList());
        return datasetRepository.saveAll(datasets);
    }

    @Transactional
    public Dataset update(DatasetRequest request) {
        return updateAll(Collections.singletonList(request)).get(0);
    }

    @Transactional
    public List<Dataset> updateAll(List<DatasetRequest> requests) {
        List<Dataset> datasets = datasetRepository.findAllByTitle(requests.stream()
                .map(DatasetRequest::getTitle)
                .collect(Collectors.toList()));

        datasets.forEach(
                ds -> {
                    Optional<DatasetRequest> request = requests.stream()
                            .filter(r -> r.getTitle().equals(ds.getDatasetData().getTitle()))
                            .findFirst();
                    request.ifPresent(datasetRequest -> convertUpdate(datasetRequest, ds));
                });

        return datasetRepository.saveAll(datasets);
    }

    @Transactional
    public Dataset delete(DatasetRequest request) {
        Optional<Dataset> fromRepository = datasetRepository.findByTitle(request.getTitle());
        if (fromRepository.isEmpty()) {
            log.warn("Cannot find Dataset with title={} for deletion", request.getTitle());
            return null;
        }
        Dataset dataset = fromRepository.get();
        request.assertMaster(dataset);
        dataset.setElasticsearchStatus(ElasticsearchStatus.TO_BE_DELETED);
        return dataset;
    }

    @Transactional
    public void deleteAll(Collection<DatasetRequest> requests) {
        requests.forEach(this::delete);
    }

    private Dataset convertNew(DatasetRequest request, DatacatalogMaster master) {
        Dataset dataset = new Dataset().convertNewFromRequest(request, master);
        attachDependencies(dataset, request);
        return dataset;
    }

    private Dataset convertUpdate(DatasetRequest request, Dataset dataset) {
        request.assertMaster(dataset);
        dataset.convertUpdateFromRequest(request);
        attachDependencies(dataset, request);
        return dataset;
    }

    private void attachDependencies(Dataset dataset, DatasetRequest request) {
        var childTitles = nullToEmptyList(request.getHaspart());
        var distChannelNames = safeStream(request.getDistributionChannels()).map(DistributionChannelShort::getName).collect(toList());

        List<Dataset> children = childTitles.isEmpty() ? Collections.emptyList() : datasetRepository.findAllByTitle(childTitles);
        if (childTitles.size() != children.size()) {
            throw new DataCatalogBackendNotFoundException(String.format("Could not find all hasparts %s, found %s", childTitles, Dataset.titles(children)));
        }

        List<DistributionChannel> distChannels = distChannelNames.isEmpty() ? Collections.emptyList() : distributionChannelRepository.findAllByNameIn(distChannelNames);
        List<DistributionChannel> newChannels = Collections.emptyList();
        if (distChannelNames.size() != distChannels.size()) {
            newChannels = createDistributionChannels(request.getDistributionChannels(), distChannels);
        }

        dataset.replaceChildren(children);
        dataset.replaceDistributionChannels(StreamUtils.union(distChannels, newChannels));
    }

    private List<DistributionChannel> createDistributionChannels(List<DistributionChannelShort> requestedDistChannel, List<DistributionChannel> existingDistChannels) {
        List<String> existingNames = DistributionChannel.names(existingDistChannels);
        List<DistributionChannel> newChannels = safeStream(requestedDistChannel)
                .filter(distChannelRequest -> !existingNames.contains(distChannelRequest.getName()))
                .map(DistributionChannelShort::toRequest)
                .map(distChannelRequest -> new DistributionChannel().convertNewFromRequest(distChannelRequest))
                .collect(Collectors.toList());
        log.info("Creating new DistributionChannels {}", newChannels);
        return distributionChannelRepository.saveAll(newChannels);
    }

    public void validateRequest(List<DatasetRequest> requests) {
        List<ValidationError> validationErrors = validateRequestsAndReturnErrors(requests);

        if (!validationErrors.isEmpty()) {
            log.error("The request was not accepted. The following errors occurred during validation: {}", validationErrors);
            throw new ValidationException(validationErrors, "The request was not accepted. The following errors occurred during validation: ");
        }
    }

    public List<ValidationError> validateRequestsAndReturnErrors(List<DatasetRequest> requests) {
        requests = nullToEmptyList(requests);
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        if (requests.stream().anyMatch(r -> r.getDatacatalogMaster() == null)) {
            throw new IllegalStateException("missing DatacatalogMaster on request");
        }

        List<ValidationError> validationErrors = new ArrayList<>(validateNoDuplicates(requests));

        requests.forEach(request -> {
            validationErrors.addAll(validateFields(request));
            request.toUpperCaseAndTrim();
            validationErrors.addAll(validateDatasetRepositoryValues(request));
        });
        return validationErrors;
    }

    private List<ValidationError> validateDatasetRepositoryValues(DatasetRequest request) {
        Optional<Dataset> existingDataset = datasetRepository.findByTitle(request.getTitle());
        List<ValidationError> validationErrors = new ArrayList<>(validateRepositoryValues(request, existingDataset.isPresent()));

        if (updatingExistingElement(request.isUpdate(), existingDataset.isPresent())) {
            DatasetData existingDatasetData = existingDataset.get().getDatasetData();

            if (!existingDatasetData.hasDatacatalogMaster()) {
                validationErrors.add(new ValidationError(request.getReference(), "missingMasterInExistingDataset"
                        , String.format("The dataset %s has not defined where it is mastered", existingDatasetData.getTitle())));
            } else if (!correlatingMaster(existingDatasetData.getDatacatalogMaster(), request.getDatacatalogMaster())) {
                validationErrors.add(new ValidationError(request.getReference(), "nonCorrelatingMaster",
                        String.format("The dataset %s is mastered in %s and therefore cannot be updated from %s",
                                request.getTitle(), existingDatasetData.getDatacatalogMaster(), request.getDatacatalogMaster())));
            }
        }
        return validationErrors;
    }

    private boolean updatingExistingElement(boolean isUpdate, boolean existInRepository) {
        return isUpdate && existInRepository;
    }

    private boolean correlatingMaster(DatacatalogMaster existingMaster, DatacatalogMaster requestMaster) {
        return existingMaster.equals(requestMaster);
    }

    public void sync(List<UUID> ids) {
        int datasetsUpdated = datasetRepository.setSyncForDatasets(ids);
        log.info("marked {} datasets for sync", datasetsUpdated);
    }
}