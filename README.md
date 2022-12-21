# README
### IMPETUS(Why another database framework?)
- I want the convenience of Spring JPA without the awkward one-to-many relations 
and tricky entity manager caching.
- I need to minimize database overhead(as it is hardest to scale), so the efficiency
of the generated queries is the primary concern. Conversely, microservices are 
highly scalable and so computational overhead is considered a worthwhile tradeoff.
### INTENDED USE CASE
Argo was designed to be used in a Micronaut microservice to pull batched data from
a Kafka topic and process inserts into a database in "Micro-batches". The idea is
that sometimes a Consumer falls behind, as this happens the batch size pulled
from kafka naturally grows, as this happens the data is sent in larger batches and 
processed more and more efficiently by the database.
### IMPLEMENTATION
#### IMPORTANT ANNOTATIONS
1) javax.persistence.Id;
2) javax.persistence.GeneratedValue
3) javax.persistence.Table
4) io.github.jamyspencer.annotations.ChildEntity
4) io.github.jamyspencer.annotations.RelationMapping