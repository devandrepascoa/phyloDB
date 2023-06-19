package pt.ist.meic.phylodb;

import org.neo4j.ogm.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.context.annotation.Bean;

/**
 * Application entry point
 */
@SpringBootApplication
public class PhylodbApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhylodbApplication.class, args);
    }

    @Autowired
    Neo4jProperties properties;

    @Bean
    public org.neo4j.ogm.config.Configuration configuration() {
        Configuration.Builder builder = new org.neo4j.ogm.config.Configuration.Builder();
        String uri = this.properties.getUri();
        String username = this.properties.getUsername();
        String password = this.properties.getPassword();

        builder.uri(uri);
        builder.credentials(username, password);

		return builder
                .connectionLivenessCheckTimeout(30)
                .build();
    }
}
