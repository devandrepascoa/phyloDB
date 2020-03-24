package pt.ist.meic.phylodb.typing.dataset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ist.meic.phylodb.typing.dataset.model.Dataset;
import pt.ist.meic.phylodb.typing.schema.SchemaRepository;
import pt.ist.meic.phylodb.typing.schema.model.Schema;
import pt.ist.meic.phylodb.utils.service.StatusResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static pt.ist.meic.phylodb.utils.db.Status.UNCHANGED;

@Service
public class DatasetService {

	private DatasetRepository datasetRepository;
	private SchemaRepository schemaRepository;

	public DatasetService(DatasetRepository datasetRepository, SchemaRepository schemaRepository) {
		this.datasetRepository = datasetRepository;
		this.schemaRepository = schemaRepository;
	}

	@Transactional(readOnly = true)
	public Optional<List<Dataset>> getDatasets(int page, int limit) {
		return Optional.ofNullable(datasetRepository.findAll(page, limit));
	}

	@Transactional(readOnly = true)
	public Optional<Dataset> getDataset(UUID id) {
		return Optional.ofNullable(datasetRepository.find(id));
	}

	@Transactional
	public StatusResult createDataset(Dataset dataset) {
		if (schemaRepository.find(new Schema.PrimaryKey(dataset.getTaxonId(), dataset.getSchemaId())) == null)
			return new StatusResult(UNCHANGED);
		return new StatusResult(datasetRepository.save(dataset));
	}

	@Transactional
	public StatusResult updateDataset(Dataset dataset) {
		if (!getDataset(dataset.getId()).isPresent())
			return new StatusResult(UNCHANGED);
		return new StatusResult(datasetRepository.save(dataset));
	}

	@Transactional
	public StatusResult deleteDataset(UUID id) {
		if (!getDataset(id).isPresent())
			return new StatusResult(UNCHANGED);
		return new StatusResult(datasetRepository.remove(id));
	}

}
