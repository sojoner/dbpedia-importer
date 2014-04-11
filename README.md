dbpedia-importer
================

A project  written in clojure to import a bunch of .ttl files from dbpedia into neo4j at once, using neo4J Batch REST api.

## Usage

* lein deps
* Start neo4J
* Open http://host:7474

Create neo4j Label index:

    CREATE INDEX ON :resource(resource)

Import One file:

    lein run http://localhost:7474 /Users/BLA/Data/mappingbased_properties_en_uris_ca.ttl

Import List of files (Recommended):

    lein run http://localhost:7474/db/data/  /Users/whoRock/Data/dbpedia_de/dewiki-20140320-labels.ttl /Users/whoRock/Data/dbpedia_de/dewiki-20140320-skos-categories.ttl /Users/whoRock/Data/dbpedia_de/dewiki-20140320-article-categories.ttl /Users/whoRock/Data/dbpedia_de/dewiki-20140320-category-labels.ttl

###Cypher to Try

    MATCH (n:resource) WHERE n.resource = "<http://de.dbpedia.org/resource/Dortmund>"

    MATCH
      (a:resource {resource:"<http://de.dbpedia.org/resource/Dortmund>"})-[:`<http://purl.org/dc/terms/subject>`]->(ca),
      (b:resource {resource:"<http://de.dbpedia.org/resource/Borussia_Dortmund>"})-[:`<http://purl.org/dc/terms/subject>`]->(cb),
      p=(ca)-[:`<http://www.w3.org/2004/02/skos/core#broader>` *..3]->(g),
      q=(cb)-[:`<http://www.w3.org/2004/02/skos/core#broader>` *..3]->(g)
    WITH p,q,g,a,b,ca,cb,
      shortestPath((ca)-->(g)) AS sp,
      shortestPath((cb)-->(g)) AS sq
    RETURN sp,sq,g,a,b LIMIT 10;

    MATCH
      (a:resource {resource:"<http://de.dbpedia.org/resource/Dortmund>"})-[r:`<http://purl.org/dc/terms/subject>`]->(ca)
    RETURN a, r, ca LIMIT 10

    MATCH 
      (n:resource)-[r:`<http://purl.org/dc/terms/subject>`]->(ca)
    WHERE n.resource = "<http://de.dbpedia.org/resource/Dortmund>" 
    RETURN n,r,ca LIMIT 1


## Thanks to

    * http://losangelesindustries.tumblr.com/post/41701508265/loading-dbpedia-into-neo4j-with-clojure
    * https://gist.github.com/nlacasse/4630592

## License

Copyright © 2014 sojoner

Distributed under the Eclipse Public License either version 1.0 or  any later version.
