package no.nav.data.catalog.backend.app.distributionchannel;

import no.nav.data.catalog.backend.app.common.exceptions.DataCatalogBackendNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DistributionChannelService {

	private final DistributionChannelRepository repository;

	public DistributionChannelService(DistributionChannelRepository repository) {
		this.repository = repository;
	}

	public Optional<DistributionChannel> findDistributionChannelById(UUID id) {
		return repository.findById(id);
	}

	public Page<DistributionChannelResponse> getAllDistributionChannels(Pageable pageable) {
		return repository.findAll(pageable).map(DistributionChannel::convertToResponse);
	}

	public List<DistributionChannelResponse> createDistributionChannels(List<DistributionChannelRequest> requests) {
		List<DistributionChannel> distributionChannels = requests.stream()
				.map(request -> new DistributionChannel().convertFromRequest(request, false))
				.collect(Collectors.toList());

		//TODO: Her må alle berørte datasett og system updateres samtidig
		return repository.saveAll(distributionChannels).stream()
				.map(DistributionChannel::convertToResponse)
				.collect(Collectors.toList());
	}

	public List<DistributionChannelResponse> updateDistributionChannels(List<DistributionChannelRequest> requests) {
		List<DistributionChannel> distributionChannels = updateAndReturnAllDistributionChannelsIfAllExists(requests);

		//TODO: Her må alle berørte datasett og system updateres samtidig -> if System_id update -> set ElasticsearchStatus to TO_BE_UDPATED?
		return repository.saveAll(distributionChannels).stream()
				.map(DistributionChannel::convertToResponse)
				.collect(Collectors.toList());
	}

	private List<DistributionChannel> updateAndReturnAllDistributionChannelsIfAllExists(List<DistributionChannelRequest> requests) {
		List<DistributionChannel> distributionChannels = new ArrayList<>();
		requests.forEach(request -> {
			Optional<DistributionChannel> optionalDistributionChannels = repository.findByName(request.getName());
			if (optionalDistributionChannels.isEmpty()) {
				throw new DataCatalogBackendNotFoundException(String.format("Cannot find distributionChannel with name: %s",
						request.getName()));
			}
			distributionChannels.add(optionalDistributionChannels.get().convertFromRequest(request, true));
		});
		return distributionChannels;
	}

	public DistributionChannel deleteDistributionChannel(DistributionChannel distributionChannel) {
		//TODO: Her må alle berørte datasett og system fjernes samtidig
		return repository.save(distributionChannel);
	}
}