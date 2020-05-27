package pt.ist.meic.phylodb.analysis.inference.model;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.ist.meic.phylodb.io.output.OutputModel;
import pt.ist.meic.phylodb.utils.service.Entity;

import java.util.List;
import java.util.stream.Collectors;

public class GetInferencesOutputModel implements OutputModel {

	private final List<InferenceOutputModel.Resumed> analyses;

	public GetInferencesOutputModel(List<Entity<Inference.PrimaryKey>> analyses) {
		this.analyses = analyses.stream()
				.map(InferenceOutputModel.Resumed::new)
				.collect(Collectors.toList());
	}

	@Override
	public ResponseEntity<List<InferenceOutputModel.Resumed>> toResponseEntity() {
		return ResponseEntity.status(HttpStatus.OK).body(analyses);
	}

}
