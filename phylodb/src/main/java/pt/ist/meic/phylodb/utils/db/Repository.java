package pt.ist.meic.phylodb.utils.db;


import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.data.neo4j.core.Neo4jTemplate;

/**
 * A Repository allows to perform queries to a database through a {@link Session}
 */
public abstract class Repository {

    private final Session session;
    private final Neo4jTemplate template;

    protected Repository(Session session, Neo4jTemplate template) {
        this.session = session;
        this.template = template;
    }

    /**
     * Performs a read query to the database which returns an object of type <code>_class<code/>
     *
     * @param _class type of the object to return
     * @param query  query to execute
     * @param <T>    type of the object to return
     * @return an object instance of type {@link T}
     */
    protected final <T> T query(Class<T> _class, Query query) {
        return template.findOne(query.getExpression(), query.getParameters(), _class).orElseThrow();
    }

    /**
     * Performs a read query to the database and returns a {@link Result}
     *
     * @param query query to be executed
     * @return a {@link Result} with the result of the query
     */
    protected final Result query(Query query) {
        return session.run(query.getExpression(), query.getParameters());
    }

    /**
     * Performs an update query to the database and returns a {@link Result}, and clears the session so the changes can be seen immediately
     *
     * @param query query to be executed
     * @return a {@link Result} with the result of the query
     */
    public final Result execute(Query query) {
        Result result = session.run(query.getExpression(), query.getParameters());
//		session.clear(); TODO???
        return result;
    }

}
