package pt.ist.meic.phylodb.unit.typing.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.MapAccessor;
import pt.ist.meic.phylodb.unit.RepositoryTestsContext;
import pt.ist.meic.phylodb.phylogeny.allele.model.Allele;
import pt.ist.meic.phylodb.typing.profile.model.Profile;
import pt.ist.meic.phylodb.utils.db.VersionedRepository;
import pt.ist.meic.phylodb.utils.db.Query;
import pt.ist.meic.phylodb.utils.service.VersionedEntity;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileRepositoryTests extends RepositoryTestsContext {

	private static final int LIMIT = 2;
	private static final String MISSING = "-";
	private static final Profile[] STATE = new Profile[]{PROFILE1, PROFILE2};

	private static Stream<Arguments> findAllEntities_params() {
		List<VersionedEntity<Allele.PrimaryKey>> alleles1 = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()), null);
		List<VersionedEntity<Allele.PrimaryKey>> alleles1Changed = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()),
				new VersionedEntity<>(ALLELE21.getPrimaryKey(), ALLELE21.getVersion(), ALLELE21.isDeprecated()));
		List<VersionedEntity<Allele.PrimaryKey>> alleles2 = Arrays.asList(null, new VersionedEntity<>(ALLELE21.getPrimaryKey(), ALLELE21.getVersion(), ALLELE21.isDeprecated()));
		List<VersionedEntity<Allele.PrimaryKey>> alleles3 = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()),
				new VersionedEntity<>(ALLELE22.getPrimaryKey(), ALLELE22.getVersion(), ALLELE22.isDeprecated()));
		Profile firstE = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "3", 1, false, null, alleles1),
				firstChangedE = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "3", 2, false, null, alleles1Changed),
				secondE = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "4", 1, false, "aka4", alleles2),
				thirdE = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "5", 1, false, "aka5", alleles3),
				thirdChangedE = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "5", 2, false, "aka4changed", alleles3),
				fourthE = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "6", 1, false, "aka6", alleles1);
		VersionedEntity<Profile.PrimaryKey> first = new VersionedEntity<>(new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "3"), 1, false),
				firstChanged = new VersionedEntity<>(new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "3"), 2, false),
				second = new VersionedEntity<>(new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "4"), 1, false),
				third = new VersionedEntity<>(new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "5"), 1, false),
				thirdChanged = new VersionedEntity<>(new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "5"), 2, false),
				fourth = new VersionedEntity<>(new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "6"), 1, false),
				state0 = new VersionedEntity<>(STATE[0].getPrimaryKey(), STATE[0].getVersion(), STATE[0].isDeprecated()),
				state1 = new VersionedEntity<>(STATE[1].getPrimaryKey(), STATE[1].getVersion(), STATE[1].isDeprecated());

		return Stream.of(Arguments.of(0, new Profile[0], Collections.emptyList()),
				Arguments.of(0, new Profile[]{STATE[0]}, Collections.singletonList(state0)),
				Arguments.of(0, new Profile[]{firstE, firstChangedE}, Collections.singletonList(firstChanged)),
				Arguments.of(0, new Profile[]{STATE[0], STATE[1], firstE}, Arrays.asList(state0, state1)),
				Arguments.of(0, new Profile[]{STATE[0], STATE[1], firstE, firstChangedE}, Arrays.asList(state0, state1)),
				Arguments.of(1, new Profile[0], Collections.emptyList()),
				Arguments.of(1, new Profile[]{STATE[0]}, Collections.emptyList()),
				Arguments.of(1, new Profile[]{firstE, firstChangedE}, Collections.emptyList()),
				Arguments.of(1, new Profile[]{STATE[0], STATE[1], firstE}, Collections.singletonList(first)),
				Arguments.of(1, new Profile[]{STATE[0], STATE[1], firstE, firstChangedE}, Collections.singletonList(firstChanged)),
				Arguments.of(1, new Profile[]{STATE[0], STATE[1], firstE, secondE}, Arrays.asList(first, second)),
				Arguments.of(1, new Profile[]{STATE[0], STATE[1], firstE, firstChangedE, secondE}, Arrays.asList(firstChanged, second)),
				Arguments.of(1, new Profile[]{STATE[0], STATE[1], firstE, firstChangedE}, Collections.singletonList(firstChanged)),
				Arguments.of(2, new Profile[0], Collections.emptyList()),
				Arguments.of(2, new Profile[]{STATE[0]}, Collections.emptyList()),
				Arguments.of(2, new Profile[]{firstE, firstChangedE}, Collections.emptyList()),
				Arguments.of(2, new Profile[]{STATE[0], STATE[1], firstE, secondE, thirdE}, Collections.singletonList(third)),
				Arguments.of(2, new Profile[]{STATE[0], STATE[1], firstE, secondE, thirdE, thirdChangedE}, Collections.singletonList(thirdChanged)),
				Arguments.of(2, new Profile[]{STATE[0], STATE[1], firstE, secondE, thirdE, fourthE}, Arrays.asList(third, fourth)),
				Arguments.of(2, new Profile[]{STATE[0], STATE[1], firstE, secondE, thirdE, thirdChangedE, fourthE}, Arrays.asList(thirdChanged, fourth)),
				Arguments.of(-1, new Profile[0], Collections.emptyList()));
	}

	private static Stream<Arguments> findAll_params() {
		List<VersionedEntity<Allele.PrimaryKey>> alleles1 = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()), null);
		List<VersionedEntity<Allele.PrimaryKey>> alleles1Changed = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()),
				new VersionedEntity<>(ALLELE21.getPrimaryKey(), ALLELE21.getVersion(), ALLELE21.isDeprecated()));
		List<VersionedEntity<Allele.PrimaryKey>> alleles2 = Arrays.asList(null, new VersionedEntity<>(ALLELE21.getPrimaryKey(), ALLELE21.getVersion(), ALLELE21.isDeprecated()));
		List<VersionedEntity<Allele.PrimaryKey>> alleles3 = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()),
				new VersionedEntity<>(ALLELE22.getPrimaryKey(), ALLELE22.getVersion(), ALLELE22.isDeprecated()));
		Profile first = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "3", 1, false, null, alleles1),
				firstChanged = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "3", 2, false, null, alleles1Changed),
				second = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "4", 1, false, "aka4", alleles2),
				third = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "5", 1, false, "aka5", alleles3),
				thirdChanged = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "5", 2, false, "aka4changed", alleles3),
				fourth = new Profile(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "6", 1, false, "aka6", alleles1);
		return Stream.of(Arguments.of(0, new Profile[0], new Profile[0]),
				Arguments.of(0, new Profile[]{STATE[0]}, new Profile[]{STATE[0]}),
				Arguments.of(0, new Profile[]{first, firstChanged}, new Profile[]{firstChanged}),
				Arguments.of(0, new Profile[]{STATE[0], STATE[1], first}, STATE),
				Arguments.of(0, new Profile[]{STATE[0], STATE[1], first, firstChanged}, STATE),
				Arguments.of(1, new Profile[0], new Profile[0]),
				Arguments.of(1, new Profile[]{STATE[0]}, new Profile[0]),
				Arguments.of(1, new Profile[]{first, firstChanged}, new Profile[0]),
				Arguments.of(1, new Profile[]{STATE[0], STATE[1], first}, new Profile[]{first}),
				Arguments.of(1, new Profile[]{STATE[0], STATE[1], first, firstChanged}, new Profile[]{firstChanged}),
				Arguments.of(1, new Profile[]{STATE[0], STATE[1], first, second}, new Profile[]{first, second}),
				Arguments.of(1, new Profile[]{STATE[0], STATE[1], first, firstChanged, second}, new Profile[]{firstChanged, second}),
				Arguments.of(1, new Profile[]{STATE[0], STATE[1], first, firstChanged}, new Profile[]{firstChanged}),
				Arguments.of(2, new Profile[0], new Profile[0]),
				Arguments.of(2, new Profile[]{STATE[0]}, new Profile[0]),
				Arguments.of(2, new Profile[]{first, firstChanged}, new Profile[0]),
				Arguments.of(2, new Profile[]{STATE[0], STATE[1], first, second, third}, new Profile[]{third}),
				Arguments.of(2, new Profile[]{STATE[0], STATE[1], first, second, third, thirdChanged}, new Profile[]{thirdChanged}),
				Arguments.of(2, new Profile[]{STATE[0], STATE[1], first, second, third, fourth}, new Profile[]{third, fourth}),
				Arguments.of(2, new Profile[]{STATE[0], STATE[1], first, second, third, thirdChanged, fourth}, new Profile[]{thirdChanged, fourth}),
				Arguments.of(-1, new Profile[0], new Profile[0]));
	}

	private static Stream<Arguments> find_params() {
		Profile.PrimaryKey key = new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "3");
		List<VersionedEntity<Allele.PrimaryKey>> allelesAll = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()), new VersionedEntity<>(ALLELE22.getPrimaryKey(), ALLELE22.getVersion(), ALLELE22.isDeprecated())),
				allelesChangedAll = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()), new VersionedEntity<>(ALLELE21.getPrimaryKey(), ALLELE21.getVersion(), ALLELE21.isDeprecated())),
				allelesMissing = Arrays.asList(null, new VersionedEntity<>(ALLELE21.getPrimaryKey(), ALLELE21.getVersion(), ALLELE21.isDeprecated())),
				allelesChangedMissing = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()), null);
		Profile first = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, null, allelesAll),
				firstChanged = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 2, false, null, allelesChangedAll),
				second = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, "aka5", allelesMissing),
				secondChanged = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 2, false, "aka4changed", allelesChangedMissing);
		return Stream.of(Arguments.of(key, 1, new Profile[0], null),
				Arguments.of(key, 1, new Profile[]{first}, first),
				Arguments.of(key, 1, new Profile[]{second}, second),
				Arguments.of(key, 2, new Profile[]{first, firstChanged}, firstChanged),
				Arguments.of(key, 2, new Profile[]{second, secondChanged}, secondChanged),
				Arguments.of(key, -3, new Profile[0], null),
				Arguments.of(key, -11, new Profile[]{first}, null),
				Arguments.of(key, -16, new Profile[]{second}, null),
				Arguments.of(key, 20, new Profile[]{first, firstChanged}, null),
				Arguments.of(key, 30, new Profile[]{second, secondChanged}, null),
				Arguments.of(null, 1, new Profile[0], null));
	}

	private static Stream<Arguments> exists_params() {
		Profile.PrimaryKey key = new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "3");
		List<VersionedEntity<Allele.PrimaryKey>> allelesAll = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()), new VersionedEntity<>(ALLELE22.getPrimaryKey(), ALLELE22.getVersion(), ALLELE22.isDeprecated())),
				allelesMissing = Arrays.asList(null, new VersionedEntity<>(ALLELE21.getPrimaryKey(), ALLELE21.getVersion(), ALLELE21.isDeprecated()));
		Profile first = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, null, allelesAll),
				firstDeleted = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, true, null, allelesAll),
				second = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, null, allelesMissing),
				secondDeleted = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, true, null, allelesMissing);
		return Stream.of(Arguments.of(key, new Profile[0], false),
				Arguments.of(key, new Profile[]{first}, true),
				Arguments.of(key, new Profile[]{firstDeleted}, false),
				Arguments.of(key, new Profile[]{second}, true),
				Arguments.of(key, new Profile[]{secondDeleted}, false),
				Arguments.of(null, new Profile[0], false));
	}

	private static Stream<Arguments> save_params() {
		Profile.PrimaryKey key = new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "3");
		List<VersionedEntity<Allele.PrimaryKey>> allelesAllPublic = Arrays.asList(new VersionedEntity<>(ALLELE12.getPrimaryKey(), ALLELE12.getVersion(), ALLELE12.isDeprecated()), new VersionedEntity<>(ALLELE22.getPrimaryKey(), ALLELE22.getVersion(), ALLELE22.isDeprecated())),
				allelesAllPrivate = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()), new VersionedEntity<>(ALLELE21.getPrimaryKey(), ALLELE21.getVersion(), ALLELE21.isDeprecated())),
				allelesMissingPublic = Arrays.asList(null, new VersionedEntity<>(ALLELE22.getPrimaryKey(), ALLELE22.getVersion(), ALLELE22.isDeprecated())),
				allelesMissingPrivate = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()), null);
		Profile firstPublic = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, null, allelesAllPublic),
				firstPublicChanged = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 2, false, null, allelesAllPrivate),
				firstPrivate = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, null, allelesAllPrivate),
				firstPrivateChanged = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 2, false, null, allelesAllPublic),
				secondPublic = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, "aka5public", allelesMissingPublic),
				secondPublicChanged = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 2, false, "aka5publicChanged", allelesMissingPrivate),
				secondPrivate = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, "aka5private", allelesMissingPrivate),
				secondPrivateChanged = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 2, false, "aka5privateChanged", allelesMissingPublic);
		return Stream.of(Arguments.of(firstPublic, new Profile[0], new Profile[]{STATE[0], STATE[1], firstPublic}, true, 2, 4),
				Arguments.of(firstPrivate, new Profile[0], new Profile[]{STATE[0], STATE[1], firstPrivate}, true, 2, 4),
				Arguments.of(secondPublic, new Profile[0], new Profile[]{STATE[0], STATE[1], secondPublic}, true, 2, 3),
				Arguments.of(secondPrivate, new Profile[0], new Profile[]{STATE[0], STATE[1], secondPrivate}, true, 2, 3),
				Arguments.of(firstPublicChanged, new Profile[]{firstPublic}, new Profile[]{STATE[0], STATE[1], firstPublic, firstPublicChanged}, true, 1, 3),
				Arguments.of(firstPrivateChanged, new Profile[]{firstPrivate}, new Profile[]{STATE[0], STATE[1], firstPrivate, firstPrivateChanged}, true, 1, 3),
				Arguments.of(secondPublicChanged, new Profile[]{secondPublic}, new Profile[]{STATE[0], STATE[1], secondPublic, secondPublicChanged}, true, 1, 2),
				Arguments.of(secondPrivateChanged, new Profile[]{secondPrivate}, new Profile[]{STATE[0], STATE[1], secondPrivate, secondPrivateChanged}, true, 1, 2),
				Arguments.of(null, new Profile[0], STATE, false, 0, 0));
	}

	private static Stream<Arguments> remove_params() {
		Profile.PrimaryKey key = new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "3");
		List<VersionedEntity<Allele.PrimaryKey>> allelesAll = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()), new VersionedEntity<>(ALLELE22.getPrimaryKey(), ALLELE22.getVersion(), ALLELE22.isDeprecated()));
		Profile first = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, null, allelesAll),
				firstDeleted = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, true, null, allelesAll);
		return Stream.of(Arguments.of(key, new Profile[0], STATE, false),
				Arguments.of(key, new Profile[]{first}, new Profile[]{STATE[0], STATE[1], firstDeleted}, true),
				Arguments.of(null, new Profile[0], STATE, false));
	}

	private static Stream<Arguments> anyMissing_params() {
		List<VersionedEntity<Profile.PrimaryKey>> references1 = new ArrayList<>(), references2 = new ArrayList<>(),
				references3 = new ArrayList<>(), references4 = new ArrayList<>(), references5 = new ArrayList<>();
		VersionedEntity<Profile.PrimaryKey> reference1 = new VersionedEntity<>(new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), STATE[1].getPrimaryKey().getId()), VersionedRepository.CURRENT_VERSION_VALUE, false),
				reference2 = new VersionedEntity<>(new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), STATE[0].getPrimaryKey().getId()), VersionedRepository.CURRENT_VERSION_VALUE, false),
				notReference1 = new VersionedEntity<>(new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), "not"), VersionedRepository.CURRENT_VERSION_VALUE, false);
		references1.add(reference1);
		references2.add(notReference1);
		references3.add(reference1);
		references3.add(reference2);
		references4.add(reference2);
		references4.add(null);
		references5.add(notReference1);
		references5.add(reference1);
		references5.add(null);
		return Stream.of(Arguments.of(references1, false),
				Arguments.of(references2, true),
				Arguments.of(references3, false),
				Arguments.of(references4, false),
				Arguments.of(references5, true));
	}

	private static Stream<Arguments> saveAll_params() {
		Profile.PrimaryKey key = new Profile.PrimaryKey(PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId(), PROFILE1.getPrimaryKey().getId());
		List<VersionedEntity<Allele.PrimaryKey>> allelesAllPublic = Arrays.asList(new VersionedEntity<>(ALLELE12.getPrimaryKey(), ALLELE12.getVersion(), ALLELE12.isDeprecated()), new VersionedEntity<>(ALLELE22.getPrimaryKey(), ALLELE22.getVersion(), ALLELE22.isDeprecated())),
				allelesAllPrivate = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()), new VersionedEntity<>(ALLELE21.getPrimaryKey(), ALLELE21.getVersion(), ALLELE21.isDeprecated())),
				allelesMissingPublic = Arrays.asList(null, new VersionedEntity<>(ALLELE22.getPrimaryKey(), ALLELE22.getVersion(), ALLELE22.isDeprecated())),
				allelesMissingPrivate = Arrays.asList(new VersionedEntity<>(ALLELE11P.getPrimaryKey(), ALLELE11P.getVersion(), ALLELE11P.isDeprecated()), null);
		Profile firstPrivateExists = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, "test", allelesAllPrivate),
				firstPublicExists = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, "test", allelesAllPublic),
				firstPrivateExistsConflict = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 2, false, "aka", allelesAllPrivate),
				firstPublicExistsConflict = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 2, false, "aka", allelesAllPublic),
				firstPrivateMissing = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, "aka test", allelesMissingPrivate),
				firstPrivateMissingConflict = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 2, false, "teste aka", allelesMissingPrivate),
				firstPublicMissing = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 1, false, null, allelesMissingPublic),
				firstPublicMissingConflict = new Profile(key.getProjectId(), key.getDatasetId(), key.getId(), 2, false, null, allelesMissingPublic),
				secondPrivateExists = new Profile(key.getProjectId(), key.getDatasetId(), key.getId() + "1", 1, false, null, allelesAllPrivate),
				secondPublicExists = new Profile(key.getProjectId(), key.getDatasetId(), key.getId() + "1", 1, false, null, allelesAllPublic);
		return Stream.of(Arguments.of(Collections.emptyList(), new Profile[]{STATE[0], STATE[1]}, new Profile[]{STATE[0], STATE[1]}, false, 0, 0),
				Arguments.of(Collections.singletonList(firstPrivateExists), new Profile[]{STATE[1]}, new Profile[]{firstPrivateExists, STATE[1]}, true, 2, 4),
				Arguments.of(Collections.singletonList(firstPrivateExistsConflict), new Profile[]{STATE[0]}, new Profile[]{STATE[0], firstPrivateExistsConflict}, true, 1, 3),
				Arguments.of(Collections.singletonList(firstPublicExists), new Profile[]{STATE[1]}, new Profile[]{firstPublicExists, STATE[1]}, true, 2, 4),
				Arguments.of(Collections.singletonList(firstPublicExistsConflict), new Profile[]{STATE[0]}, new Profile[]{STATE[0], firstPublicExistsConflict}, true, 1, 3),
				Arguments.of(Collections.singletonList(firstPrivateMissing), new Profile[]{STATE[1]}, new Profile[]{firstPrivateMissing, STATE[1]}, true, 2, 3),
				Arguments.of(Collections.singletonList(firstPrivateMissingConflict), new Profile[]{STATE[0]}, new Profile[]{STATE[0], firstPrivateMissingConflict}, true, 1, 2),
				Arguments.of(Collections.singletonList(firstPublicMissing), new Profile[]{STATE[1]}, new Profile[]{firstPublicMissing, STATE[1]}, true, 2, 3),
				Arguments.of(Collections.singletonList(firstPublicMissingConflict), new Profile[]{STATE[0]}, new Profile[]{STATE[0], firstPublicMissingConflict}, true, 1, 2),
				Arguments.of(Arrays.asList(firstPrivateExists, secondPrivateExists), new Profile[0], new Profile[]{firstPrivateExists, secondPrivateExists}, true, 4, 8),
				Arguments.of(Arrays.asList(firstPrivateExistsConflict, secondPrivateExists), new Profile[]{STATE[0]}, new Profile[]{STATE[0], firstPrivateExistsConflict, secondPrivateExists}, true, 3, 7),
				Arguments.of(Arrays.asList(firstPublicExists, secondPublicExists), new Profile[0], new Profile[]{firstPublicExists, secondPublicExists}, true, 4, 8),
				Arguments.of(Arrays.asList(firstPublicExistsConflict, secondPublicExists), new Profile[]{STATE[0]}, new Profile[]{STATE[0], firstPublicExistsConflict, secondPublicExists}, true, 3, 7),
				Arguments.of(Arrays.asList(firstPrivateMissing, secondPrivateExists), new Profile[0], new Profile[]{firstPrivateMissing, secondPrivateExists}, true, 4, 7),
				Arguments.of(Arrays.asList(firstPrivateMissingConflict, secondPrivateExists), new Profile[]{STATE[0]}, new Profile[]{STATE[0], firstPrivateMissingConflict, secondPrivateExists}, true, 3, 6),
				Arguments.of(Arrays.asList(firstPublicMissing, secondPublicExists), new Profile[0], new Profile[]{firstPublicMissing, secondPublicExists}, true, 4, 7),
				Arguments.of(Arrays.asList(firstPublicMissingConflict, secondPublicExists), new Profile[]{STATE[0]}, new Profile[]{STATE[0], firstPublicMissingConflict, secondPublicExists}, true, 3, 6));
	}

	private void store(Profile[] profiles) {
		for (Profile profile : profiles) {
			Profile.PrimaryKey key = profile.getPrimaryKey();
			String statement = "MATCH (pj:Project {id: $})-[:CONTAINS]->(d:Dataset {id: $}) WHERE pj.deprecated = false AND d.deprecated = false\n" +
					"MERGE (d)-[:CONTAINS]->(p:Profile {id: $}) SET p.deprecated = $ WITH pj, d, p\n" +
					"OPTIONAL MATCH (p)-[r:CONTAINS_DETAILS]->(pd:ProfileDetails)\n" +
					"WHERE NOT EXISTS(r.to) SET r.to = datetime()\n" +
					"WITH pj, d, p, COALESCE(r.version, 0) + 1 as v\n" +
					"CREATE (p)-[:CONTAINS_DETAILS {from: datetime(), version: v}]->(pd:ProfileDetails {aka: $})\n" +
					"WITH pj, d, pd\n" +
					"MATCH (d)-[r1:CONTAINS_DETAILS]->(dd:DatasetDetails)-[h:HAS]->(s:Schema)-[r2:CONTAINS_DETAILS]->(sd:SchemaDetails)\n" +
					"WHERE NOT EXISTS(r1.to) AND r2.version = h.version\n" +
					"WITH pj, d, pd, sd\n";
			Query query = new Query(statement);
			query.addParameter(key.getProjectId(), key.getDatasetId(), key.getId(), profile.isDeprecated(), profile.getAka());
			statement = "MATCH (sd)-[:HAS {part: %s}]->(l:Locus)\n" +
					"MATCH (l)-[:CONTAINS]->(a:Allele {id: $})-[r:CONTAINS_DETAILS]->(ad:AlleleDetails)\n" +
					"WHERE NOT EXISTS(r.to) AND %s\n" +
					"CREATE (pd)-[:HAS {version: r.version, part: %s, total: %s}]->(a)\n" +
					"WITH pj, d, pd, sd\n";
			List<VersionedEntity<Allele.PrimaryKey>> allelesIds = profile.getAllelesReferences();
			for (int i = 0; i < allelesIds.size(); i++) {
				VersionedEntity<Allele.PrimaryKey> reference = allelesIds.get(i);
				if (reference == null || reference.getPrimaryKey().getId().matches(MISSING))
					continue;
				String referenceId = reference.getPrimaryKey().getId();
				String where = reference.getPrimaryKey().getProjectId() != null ? "(a)<-[:CONTAINS]-(pj)" : "NOT (a)<-[:CONTAINS]-(:Project)";
				query.appendQuery(statement, i + 1, where, i + 1, allelesIds.size()).addParameter(referenceId);
			}
			query.subQuery(query.length() - "WITH pj, d, pd, sd\n".length());
			execute(query);
		}
	}

	private Profile parse(Map<String, Object> row) {
		List<Map<String, Object>> alleles = (List<Map<String, Object>>) row.get("alleles");
		int size = Math.toIntExact((long) alleles.get(0).get("total"));
		List<VersionedEntity<Allele.PrimaryKey>> allelesReferences = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
			allelesReferences.add(null);
		for (Map<String, Object> a : alleles) {
			int position = Math.toIntExact((long) a.get("part"));
			Object projectId = a.get("project");
			Allele.PrimaryKey key = new Allele.PrimaryKey((String) a.get("taxon"), (String) a.get("locus"), (String) a.get("id"), (String) projectId);
			VersionedEntity<Allele.PrimaryKey> reference = new VersionedEntity<>(key, (long) a.get("version"), (boolean) a.get("deprecated"));
			allelesReferences.set(position - 1, reference);
		}
		return new Profile(row.get("projectId").toString(),
				row.get("datasetId").toString(),
				(String) row.get("id"),
				(long) row.get("version"),
				(boolean) row.get("deprecated"),
				(String) row.get("aka"),
				allelesReferences
		);
	}

	private Profile[] findAll() {
		String statement = "MATCH (pj:Project {id: $})-[:CONTAINS]->(d:Dataset {id: $})-[:CONTAINS]->(p:Profile)-[r:CONTAINS_DETAILS]->(pd:ProfileDetails)\n" +
				"MATCH (pd)-[h:HAS]->(a:Allele)<-[:CONTAINS]-(l:Locus)<-[:CONTAINS]-(t:Taxon)\n" +
				"OPTIONAL MATCH (a)<-[:CONTAINS]-(pj2:Project)\n" +
				"RETURN pj.id as projectId, d.id as datasetId, p.id as id, r.version as version, p.deprecated as deprecated,\n" +
				"pd.aka as aka, collect(DISTINCT {project: pj2.id, taxon: t.id, locus: l.id, id: a.id, version: h.version, deprecated: a.deprecated, part:h.part, total:h.total}) as alleles\n" +
				"ORDER BY pj.id, d.id, p.id, version";
		Result result = query(new Query(statement, PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId()));
		if (result == null) return new Profile[0];
		return StreamSupport.stream(result.stream().map(MapAccessor::asMap).spliterator(), false)
				.map(this::parse)
				.toArray(Profile[]::new);
	}

	@BeforeEach
	public void init() {
		taxonRepository.save(TAXON1);
		locusRepository.save(LOCUS1);
		locusRepository.save(LOCUS2);
		userRepository.save(USER1);
		projectRepository.save(PROJECT1);
		projectRepository.save(PROJECT2);
		schemaRepository.save(SCHEMA1);
		datasetRepository.save(DATASET1);
		alleleRepository.save(ALLELE11P);
		alleleRepository.save(ALLELE12);
		alleleRepository.save(ALLELE21);
		alleleRepository.save(ALLELE22);
	}

	@ParameterizedTest
	@MethodSource("findAllEntities_params")
	public void findAllEntities(int page, Profile[] state, List<VersionedEntity<Profile.PrimaryKey>> expected) {
		store(state);
		Optional<List<VersionedEntity<Profile.PrimaryKey>>> result = profileRepository.findAllEntities(page, LIMIT, PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId());
		if (expected.size() == 0 && !result.isPresent()) {
			assertTrue(true);
			return;
		}
		assertTrue(result.isPresent());
		List<VersionedEntity<Profile.PrimaryKey>> profiles = result.get();
		assertEquals(expected.size(), profiles.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).getPrimaryKey(), profiles.get(i).getPrimaryKey());
			assertEquals(expected.get(i).getVersion(), profiles.get(i).getVersion());
			assertEquals(expected.get(i).isDeprecated(), profiles.get(i).isDeprecated());
		}
	}

	@ParameterizedTest
	@MethodSource("findAll_params")
	public void findAll(int page, Profile[] state, Profile[] expected) {
		store(state);
		Optional<List<Profile>> result = profileRepository.findAll(page, LIMIT, PROJECT1.getPrimaryKey(), DATASET1.getPrimaryKey().getId());
		if (expected.length == 0 && !result.isPresent()) {
			assertTrue(true);
			return;
		}
		assertTrue(result.isPresent());
		List<Profile> schemas = result.get();
		assertEquals(expected.length, schemas.size());
		assertArrayEquals(expected, schemas.toArray());
	}

	@ParameterizedTest
	@MethodSource("find_params")
	public void find(Profile.PrimaryKey key, long version, Profile[] state, Profile expected) {
		store(ProfileRepositoryTests.STATE);
		store(state);
		Optional<Profile> result = profileRepository.find(key, version);
		assertTrue((expected == null && !result.isPresent()) || (expected != null && result.isPresent()));
		if (expected != null)
			assertEquals(expected, result.get());
	}

	@ParameterizedTest
	@MethodSource("exists_params")
	public void exists(Profile.PrimaryKey key, Profile[] state, boolean expected) {
		store(ProfileRepositoryTests.STATE);
		store(state);
		boolean result = profileRepository.exists(key);
		assertEquals(expected, result);
	}

	@ParameterizedTest
	@MethodSource("save_params")
	public void save(Profile profile, Profile[] state, Profile[] expectedState, boolean executed, int nodesCreated, int relationshipsCreated) {
		store(ProfileRepositoryTests.STATE);
		store(state);
		int nodes = countNodes();
		int relationships = countRelationships();
		boolean result = profileRepository.save(profile);
		if (executed) {
			assertTrue(result);
			assertEquals(nodes + nodesCreated, countNodes());
			assertEquals(relationships + relationshipsCreated, countRelationships());
		} else
			assertFalse(result);
		Profile[] stateResult = findAll();
		assertArrayEquals(expectedState, stateResult);
	}

	@ParameterizedTest
	@MethodSource("remove_params")
	public void remove(Profile.PrimaryKey key, Profile[] state, Profile[] expectedState, boolean expectedResult) {
		store(ProfileRepositoryTests.STATE);
		store(state);
		boolean result = profileRepository.remove(key);
		Profile[] stateResult = findAll();
		assertEquals(expectedResult, result);
		assertArrayEquals(expectedState, stateResult);
	}


	@ParameterizedTest
	@MethodSource("anyMissing_params")
	public void anyMissing(List<VersionedEntity<Profile.PrimaryKey>> references, boolean expected) {
		store(STATE);
		boolean result = profileRepository.anyMissing(references);
		assertEquals(expected, result);
	}

	@ParameterizedTest
	@MethodSource("saveAll_params")
	public void saveAll(List<Profile> profiles, Profile[] state, Profile[] expectedState, boolean executed, int nodesCreated, int relationshipsCreated) {
		store(state);
		int nodes = countNodes();
		int relationships = countRelationships();
		boolean result = profileRepository.saveAll(profiles);
		if (executed) {
			assertTrue(result);
			assertEquals(nodes + nodesCreated, countNodes());
			assertEquals(relationships + relationshipsCreated, countRelationships());
		} else
			assertFalse(result);

		Profile[] stateResult = findAll();
		assertArrayEquals(expectedState, stateResult);
	}

}
