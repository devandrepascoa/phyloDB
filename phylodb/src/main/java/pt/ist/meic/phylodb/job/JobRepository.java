package pt.ist.meic.phylodb.job;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.MapAccessor;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Repository;
import pt.ist.meic.phylodb.job.model.Job;
import pt.ist.meic.phylodb.utils.db.Query;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Class that contains the implementation of the {@link pt.ist.meic.phylodb.utils.db.Repository} for jobs
 */
@Repository
public class JobRepository extends pt.ist.meic.phylodb.utils.db.Repository {

	public static final String FULLY_QUALIFIED = "algorithms.%s";
	public static final int UUID_LENGTH = UUID.randomUUID().toString().length();

	protected JobRepository(Driver driver, Neo4jTemplate template) {
		super(driver.session(), template);
	}

	/**
	 * Retrieves a page of {@link Job} as a result
	 *
	 * @param page    number of the page to retrieve
	 * @param limit   number of {@link Job} to retrieve by page
	 * @param filters used to filter the query results
	 * @return page of {@link Job}
	 */
	public Optional<List<Job>> findAll(int page, int limit, Object... filters) {
		if (page < 0 || limit < 0) return Optional.empty();
		Result result = getAll(page * limit, limit, filters);
		if (result == null) return Optional.empty();
		return Optional.of(StreamSupport.stream(result.stream().map(MapAccessor::asMap).spliterator(), false)
				.map(this::parse)
				.collect(Collectors.toList()));
	}

	/**
	 * Verifies if an job identified by the {@link Job.PrimaryKey key} exists
	 *
	 * @param key job id
	 * @return {@code true} if it exists
	 */
	public boolean exists(Job.PrimaryKey key) {
		return key != null && isPresent(key);
	}

	/**
	 * Saves the {@link Job job}
	 *
	 * @param entity {@link Job} to be saved
	 */
	public boolean save(Job entity) {
		if (entity == null)
			return false;
		store(entity);
		return true;
	}

	/**
	 * Removes the {@link Job job} identified by the {@link Job.PrimaryKey key}
	 *
	 * @param key job id
	 */
	public boolean remove(Job.PrimaryKey key) {
		if (!exists(key))
			return false;
		delete(key);
		return true;
	}

	private Result getAll(int page, int limit, Object... filters) {
		String statement = "CALL apoc.periodic.list() YIELD name, done, cancelled\n" +
				"WHERE name STARTS WITH $\n" +
				"RETURN name as name, done as completed, cancelled as cancelled\n" +
				"ORDER BY size(name), name SKIP $ LIMIT $";
		return query(new Query(statement, filters[0], page, limit));
	}

	private Job parse(Map<String, Object> row) {
		String name = (String) row.get("name");
		String project = name.substring(0, UUID_LENGTH);
		String id = name.substring(UUID_LENGTH + 1);
		return new Job(project,
				id,
				(boolean) row.get("completed"),
				(boolean) row.get("cancelled")
		);
	}

	private boolean isPresent(Job.PrimaryKey key) {
		String statement = "CALL apoc.periodic.list() YIELD name, done, cancelled\n" +
				"WHERE name = $\n" +
				"RETURN COALESCE(name is not null, false)";
		Boolean result = query(Boolean.class, new Query(statement, name(key.getProjectId(), key.getId())));
		return result == null ? false : result;
	}

	private void store(Job job) {
		Job.PrimaryKey key = job.getPrimaryKey();
		String jobName = name(key.getProjectId(), key.getId());
		String params = Arrays.stream(job.getParams())
				.map(s -> s.getClass().equals(String.class) ? "'" + s + "'" : String.valueOf(s))
				.collect(Collectors.joining(","));
		params = "'" + key.getProjectId() + "'," + params + ",'" + job.getAnalysisId() + "'";
		String statement = "CALL apoc.periodic.submit($, 'CALL ' + $ + '(' + $ + ')') YIELD name, delay, rate, done, cancelled RETURN 0";
		execute(new Query(statement, jobName, String.format(FULLY_QUALIFIED, job.getAlgorithm()), params));
	}

	private void delete(Job.PrimaryKey key) {
		execute(new Query("CALL apoc.periodic.cancel($)", name(key.getProjectId(), key.getId())));
	}

	private String name(String projectId, String jobId) {
		return projectId + "-" + jobId;
	}

}
