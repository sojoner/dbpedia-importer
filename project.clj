(defproject dbpedia-importer "0.1.0-SNAPSHOT"
  :jvm-opts ["-Xmx14g" "-XX:+UseConcMarkSweepGC"]
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                            [org.neo4j/neo4j "2.1.0-M01"]
                            [org.clojure/tools.logging "0.2.6"]
                            [net.sourceforge.owlapi/owlapi-turtle "3.3"]]
  :main dbpedia-importer.core)
