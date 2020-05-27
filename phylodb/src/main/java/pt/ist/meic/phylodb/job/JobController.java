package pt.ist.meic.phylodb.job;

import javafx.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.meic.phylodb.error.ErrorOutputModel;
import pt.ist.meic.phylodb.error.Problem;
import pt.ist.meic.phylodb.job.model.GetJobsOutputModel;
import pt.ist.meic.phylodb.job.model.JobAcceptedOutputModel;
import pt.ist.meic.phylodb.job.model.JobInputModel;
import pt.ist.meic.phylodb.job.model.JobRequest;
import pt.ist.meic.phylodb.security.authorization.Activity;
import pt.ist.meic.phylodb.security.authorization.Authorized;
import pt.ist.meic.phylodb.security.authorization.Operation;
import pt.ist.meic.phylodb.security.authorization.Role;
import pt.ist.meic.phylodb.utils.controller.Controller;

import java.util.Optional;

@RestController
@RequestMapping("projects/{project}/jobs")
public class JobController extends Controller {

	private JobService service;

	public JobController(JobService service) {
		this.service = service;
	}

	@Authorized(activity = Activity.ALGORITHMS, role = Role.USER, operation = Operation.READ)
	@GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getJobs(
			@PathVariable("project") String projectId,
			@RequestParam(value = "page", defaultValue = "0") int page
	) {
		return getAllJson(l -> service.getJobs(projectId, page, l), GetJobsOutputModel::new);
	}

	@Authorized(activity = Activity.ALGORITHMS, role = Role.USER, operation = Operation.WRITE)
	@PostMapping(path = "")
	public ResponseEntity<?> postJob(
			@PathVariable("project") String projectId,
			@RequestBody JobInputModel inputModel
	) {
		Optional<JobRequest> jobRequest = inputModel.toDomainEntity();
		if(!jobRequest.isPresent())
			return new ErrorOutputModel(Problem.BAD_REQUEST).toResponseEntity();
		Optional<Pair<String, String>> optional = service.createJob(projectId, jobRequest.get());
		return optional.isPresent() ?
				new JobAcceptedOutputModel(optional.get().getKey(), optional.get().getValue()).toResponseEntity() :
				new ErrorOutputModel(Problem.UNAUTHORIZED).toResponseEntity();
	}

	@Authorized(activity = Activity.ALGORITHMS, role = Role.USER, operation = Operation.WRITE)
	@DeleteMapping(path = "/{job}")
	public ResponseEntity<?> deleteJob(
			@PathVariable("project") String projectId,
			@PathVariable("job") String jobId
	) {
		return status(() -> service.deleteJob(projectId, jobId));
	}

}
