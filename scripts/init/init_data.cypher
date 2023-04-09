CALL apoc.cypher.runMany("MATCH(n) DETACH DELETE n;
CREATE (:User {provider: 'phylodb', id: 'admin@gmail.com', deprecated: false})-[:CONTAINS_DETAILS {from: datetime(), version: 1}]->(:UserDetails {role: 'admin'})
CREATE (:User {provider: 'google', id: 'crjvaz@gmail.com', deprecated: false})-[:CONTAINS_DETAILS {from: datetime(), version: 1}]->(:UserDetails {role: 'admin'})
CREATE (:User {provider: 'google', id: 'bruno.m.lourenco97@gmail.com', deprecated: false})-[:CONTAINS_DETAILS {from: datetime(), version: 1}]->(:UserDetails {role: 'admin'});", {});