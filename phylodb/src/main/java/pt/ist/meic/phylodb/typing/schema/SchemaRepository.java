package pt.ist.meic.phylodb.typing.schema;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.MapAccessor;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Repository;
import pt.ist.meic.phylodb.phylogeny.locus.model.Locus;
import pt.ist.meic.phylodb.phylogeny.taxon.model.Taxon;
import pt.ist.meic.phylodb.typing.Method;
import pt.ist.meic.phylodb.typing.dataset.model.Dataset;
import pt.ist.meic.phylodb.typing.schema.model.Schema;
import pt.ist.meic.phylodb.utils.db.Query;
import pt.ist.meic.phylodb.utils.db.VersionedRepository;
import pt.ist.meic.phylodb.utils.service.VersionedEntity;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class that contains the implementation of the {@link VersionedRepository} for schemas
 */
@Repository
public class SchemaRepository extends VersionedRepository<Schema, Schema.PrimaryKey> {

    public SchemaRepository(Driver driver, Neo4jTemplate template) {
        super(driver.session(), template);
    }

    @Override
    protected Result getAllEntities(int page, int limit, Object... filters) {
        if (filters == null || filters.length == 0)
            return null;
        String statement = "MATCH (t:Taxon {id: $})-[:CONTAINS]->(l:Locus)<-[h:HAS]-(sd:SchemaDetails)<-[r:CONTAINS_DETAILS]-(s:Schema)\n" +
                "WHERE s.deprecated = false AND NOT EXISTS(r.to)\n" +
                "WITH t, s, r, sd, collect(DISTINCT {taxon: t.id, id: l.id, deprecated: l.deprecated, version: h.version}) as lociIds\n" +
                "RETURN t.id as taxonId, s.id as id, s.type as type, s.deprecated as deprecated, r.version as version\n" +
                "ORDER BY t.id, size(s.id), s.id SKIP $ LIMIT $";
        return query(new Query(statement, filters[0], page, limit));
    }

    @Override
    protected Result get(Schema.PrimaryKey key, long version) {
        String where = version == CURRENT_VERSION_VALUE ? "NOT EXISTS(r.to)" : "r.version = $";
        String statement = "MATCH (t:Taxon {id: $})-[:CONTAINS]->(l:Locus)<-[h:HAS]-(sd:SchemaDetails)<-[r:CONTAINS_DETAILS]-(s:Schema {id: $})\n" +
                "WHERE " + where + " WITH t, s, r, sd, l, h\n" +
                "ORDER BY h.part\n" +
                "WITH t, s, r, sd, collect(DISTINCT {id: l.id, deprecated: l.deprecated, version: h.version}) as lociIds\n" +
                "RETURN t.id as taxonId, s.id as id, s.type as type, s.deprecated as deprecated, r.version as version, " +
                "sd.description as description, lociIds";
        return query(new Query(statement, key.getTaxonId(), key.getId(), version));
    }

    @Override
    protected VersionedEntity<Schema.PrimaryKey> parseVersionedEntity(Map<String, Object> row) {
        return new VersionedEntity<>(new Schema.PrimaryKey((String) row.get("taxonId"), (String) row.get("id")),
                (long) row.get("version"),
                (boolean) row.get("deprecated"));
    }

    @Override
    protected Schema parse(Map<String, Object> row) {
        String taxonId = (String) row.get("taxonId");
        List<VersionedEntity<Locus.PrimaryKey>> lociIds = ((List<Map<String, Object>>) row.get("lociIds"))
                .stream()
                .map(m -> new VersionedEntity<>(new Locus.PrimaryKey(taxonId, (String) m.get("id")), (long) m.get("version"), (boolean) m.get("deprecated")))
                .collect(Collectors.toList());
        return new Schema(taxonId,
                (String) row.get("id"),
                (long) row.get("version"),
                (boolean) row.get("deprecated"),
                Method.valueOf(((String) row.get("type")).toUpperCase()),
                (String) row.get("description"),
                lociIds);
    }

    @Override
    protected boolean isPresent(Schema.PrimaryKey key) {
        String statement = "OPTIONAL MATCH (t:Taxon {id: $})-[:CONTAINS]->(l:Locus)<-[h:HAS]-(sd:SchemaDetails)<-[r:CONTAINS_DETAILS]-(s:Schema {id: $})\n" +
                "WITH s, collect(l) as loci\n" +
                "RETURN COALESCE(s.deprecated = false, false)";
        return query(Boolean.class, new Query(statement, key.getTaxonId(), key.getId()));
    }

    @Override
    protected void store(Schema schema) {
        if (isPresent(schema.getPrimaryKey())) {
            put(schema);
            return;
        }
        post(schema);
    }

    @Override
    protected void delete(Schema.PrimaryKey key) {
        String statement = "MATCH (t:Taxon {id: $})-[:CONTAINS]->(l:Locus)<-[h:HAS]-(sd:SchemaDetails)<-[r:CONTAINS_DETAILS]-(s:Schema {id: $})\n" +
                "SET s.deprecated = true\n";
        execute(new Query(statement, key.getTaxonId(), key.getId()));
    }

    /**
     * Retrieves the schema that has the specified loci
     *
     * @param taxonId identifier of the {@link Taxon taxon}
     * @param type    schema {@link Method methodology}
     * @param lociIds identifier of the  {@link Locus loci} composing the schema
     * @return the schema of the specified by the loci
     */
    public Optional<Schema> find(String taxonId, Method type, String[] lociIds) {
        if (taxonId == null || lociIds == null || lociIds.length == 0)
            return Optional.empty();
        String statement = "MATCH (t:Taxon {id: $})-[:CONTAINS]->(l:Locus)<-[:HAS]-(sd:SchemaDetails)<-[r:CONTAINS_DETAILS]-(s:Schema {type: $})\n" +
                "WHERE s.deprecated = false AND NOT EXISTS(r.to)\n" +
                "WITH t, s, r, sd\n";
        Query query = new Query(statement, taxonId, type.getName());
        for (int i = 0; i < lociIds.length; i++) {
            query.appendQuery("MATCH (sd)-[:HAS {part: %s}]->(l%s:Locus {id: $}) WITH t, s, r, sd\n", i + 1, i);
            query.addParameter(lociIds[i]);
        }
        query.appendQuery("MATCH (sd)-[h:HAS]->(l:Locus) WITH t, s, r, sd, h, l\n" +
                "ORDER BY h.part\n" +
                "WITH t, s, r, sd, collect(DISTINCT {id: l.id, deprecated: l.deprecated, version: h.version}) as lociIds\n" +
                "RETURN t.id as taxonId, s.id as id, s.deprecated as deprecated, r.version as version,\n" +
                "s.type as type, sd.description as description, lociIds");
        Result result = query(query);
        Iterator<Map<String, Object>> iterator = result.stream().map(MapAccessor::asMap).iterator();
        if (!iterator.hasNext())
            return Optional.empty();
        return Optional.of(parse(iterator.next()));
    }

    /**
     * Retrieves the schema of the specified {@link Dataset dataset}
     *
     * @param key dataset {@link Dataset.PrimaryKey primary key}
     * @return the schema of the specified dataset
     */
    public Optional<Schema> find(Dataset.PrimaryKey key) {
        if (key == null)
            return Optional.empty();
        String statement = "MATCH (p:Project {id: $})-[:CONTAINS]->(d:Dataset {id: $})-[r1:CONTAINS_DETAILS]->(dd:DatasetDetails)-[h1:HAS]->(s:Schema)-[r2:CONTAINS_DETAILS]->(sd:SchemaDetails)\n" +
                "WHERE NOT EXISTS(r1.to) AND r2.version = h1.version\n" +
                "MATCH (sd)-[h2:HAS]->(l:Locus)<-[:CONTAINS]-(t:Taxon)  WITH t, s, r2, sd, h2, l\n" +
                "ORDER BY h2.part\n" +
                "WITH t, s, r2, sd, collect(DISTINCT {id: l.id, deprecated: l.deprecated, version: h2.version}) as lociIds\n" +
                "RETURN t.id as taxonId, s.id as id, s.deprecated as deprecated, r2.version as version,\n" +
                "s.type as type, sd.description as description, lociIds";
        Result result = query(new Query(statement, key.getProjectId(), key.getId()));
        Iterator<Map<String, Object>> iterator = result.stream().map(MapAccessor::asMap).iterator();
        if (!iterator.hasNext())
            return Optional.empty();
        return Optional.of(parse(iterator.next()));
    }

    private void post(Schema schema) {
        String statement = "CREATE (s:Schema {id: $, type: $, deprecated: false})-[:CONTAINS_DETAILS {from: datetime(), version: 1}]->(sd:SchemaDetails {description: $}) WITH sd\n " +
                "MATCH (t:Taxon {id: $}) WHERE t.deprecated = false\n" +
                "WITH t, sd\n" +
                "UNWIND $ as param\n" +
                "MATCH (t)-[:CONTAINS]->(l:Locus {id: param.id})-[r:CONTAINS_DETAILS]->(:LocusDetails)\n" +
                "WHERE l.deprecated = false AND NOT EXISTS(r.to)\n" +
                "CREATE (sd)-[:HAS {part: param.part, version: r.version}]->(l)";
        List<VersionedEntity<Locus.PrimaryKey>> loci = schema.getLociReferences();
        Query query = new Query(statement, schema.getPrimaryKey().getId(), schema.getType().getName(), schema.getDescription(), schema.getPrimaryKey().getTaxonId(),
                IntStream.range(0, loci.size())
                        .mapToObj(i -> new Object() {
                            public final String id = loci.get(i).getPrimaryKey().getId();
                            public final int part = i + 1;
                        })
                        .toArray()
        );
        execute(query);
    }

    private void put(Schema schema) {
        String statement = "MATCH (t:Taxon {id: $})-[:CONTAINS]->(l:Locus)<-[h:HAS]-(sd:SchemaDetails)<-[r:CONTAINS_DETAILS]-(s:Schema {id: $})\n" +
                "WHERE NOT EXISTS(r.to)\n" +
                "WITH t, s, r, sd, collect(l.id) as loci\n" +
                "SET s.deprecated = false, r.to = datetime() WITH t, s, r.version + 1 as v\n" +
                "CREATE (s)-[:CONTAINS_DETAILS {from: datetime(), version: v}]->(sd:SchemaDetails {description: $})\n" +
                "WITH t, sd\n" +
                "UNWIND $ as param\n" +
                "MATCH (t)-[:CONTAINS]->(l:Locus {id: param.id})-[r:CONTAINS_DETAILS]->(:LocusDetails)\n" +
                "WHERE l.deprecated = false AND NOT EXISTS(r.to)\n" +
                "CREATE (sd)-[:HAS {part: param.part, version: r.version}]->(l)";
        List<VersionedEntity<Locus.PrimaryKey>> loci = schema.getLociReferences();
        Query query = new Query(statement, schema.getPrimaryKey().getTaxonId(), schema.getPrimaryKey().getId(), schema.getDescription(),
                IntStream.range(0, loci.size())
                        .mapToObj(i -> new Object() {
                            public final String id = loci.get(i).getPrimaryKey().getId();
                            public final int part = i + 1;
                        })
                        .toArray()
        );
        execute(query);
    }

}
