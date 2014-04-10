dbpedia-importer
================

A lein project to import ttl files from dbpedia into neo4j


## Usage

<pre>lein deps</pre>

One file:

    <pre>lein run http://localhost:7474 /Users/BLA/Data/mappingbased_properties_en_uris_ca.ttl</pre>

List of files:

    <pre>lein run http://localhost:7474/db/data/  /Users/hagen/Data/dbpedia_de/dewiki-20140320-labels.ttl /Users/hagen/Data/dbpedia_de/dewiki-20140320-skos-categories.ttl /Users/hagen/Data/dbpedia_de/dewiki-20140320-article-categories.ttl /Users/hagen/Data/dbpedia_de/dewiki-20140320-category-labels.ttl</pre>

Create neo4j Label index:

    CREATE INDEX ON :resource(resource)

## Thanks to

    * http://losangelesindustries.tumblr.com/post/41701508265/loading-dbpedia-into-neo4j-with-clojure
    * https://gist.github.com/nlacasse/4630592

## License

Copyright © 2014 sojoner

Distributed under the Eclipse Public License either version 1.0 or  any later version.
