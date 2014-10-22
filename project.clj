(defproject dbpedia-importer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                            [net.sourceforge.owlapi/owlapi-turtle "3.3"]
                            ;[clojurewerkz/neocons "2.0.1"]
                            [clojurewerkz/neocons "3.0.0"]                            
                            [digest "1.4.4"]
                            [org.clojure/data.json "0.2.4"]]
  :main dbpedia-importer.core)
