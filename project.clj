(defproject dbpedia-importer "0.1.0-SNAPSHOT"
  :jvm-opts ["-Xmx4g" "-XX:+UseConcMarkSweepGC"]
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                            [net.sourceforge.owlapi/owlapi-turtle "3.3"]
                            [clojurewerkz/neocons "2.0.1"]
                            [digest "1.4.4"]
                            [org.clojure/data.json "0.2.4"]]
  :main dbpedia-importer.core)
