(ns dbpedia-importer.core
  (:use [clojure.tools.logging :only [log]])
    (:require [clojure.java.io :as io]
              [clojurewerkz.neocons.rest :as nr]
              [clojurewerkz.neocons.rest.nodes :as nn]
              [clojurewerkz.neocons.rest.relationships :as nrl]
    )
  (:import [uk.ac.manchester.cs.owl.owlapi.turtle.parser TurtleParser]
           [org.neo4j.unsafe.batchinsert BatchInserters]
           [org.neo4j.index.lucene.unsafe.batchinsert LuceneBatchInserterIndexProvider]
           [org.neo4j.graphdb DynamicRelationshipType]))
 
;; PARSING METHODS
 
(defn get-next-tuple
  [parser]
  (let [last-item (atom nil)
        tuple (atom [])]
    (while (and (not= "." @last-item)
                (not= "" @last-item))
      (reset! last-item
              (-> parser
                (.getNextToken)
                (.toString)))
      (swap! tuple conj @last-item))
    (when-not (empty? (first @tuple)) ; .getNextToken returns "" once you are out of data
      @tuple)))
 
(defn seq-of-parser
  [parser]
  (if-let [next-tuple (get-next-tuple parser)]
    (lazy-cat [next-tuple]
              (seq-of-parser parser))))
 
(defn parse-file
  [filename]
  (seq-of-parser
    (TurtleParser.
      (io/input-stream filename))))
 
;; BATCH UPSERT METHODS
 
(def id-map (atom (transient {})))
 
(defn insert-resource-node! [res]
  (if-let [id (get @id-map res)]
    ; If the resource has aleady been added, just return the id.
    id
    ; Otherwise, add the node for the node, and remember its id for later.
    (let [a_node (nn/create {"resource" res})
          id (:id a_node)]
      (swap! id-map #(assoc! % res id))
      id)))

(defn insert-tuple! [tuple]
  ; Get the resource and label names out of the tuple.
  (let [[resource-1 label resource-2 & _ ] tuple
        ; Upsert the resource nodes.
        node-1 (insert-resource-node! resource-1)
        node-2 (insert-resource-node! resource-2)]
    ; Connect the nodes with an edge. 
     (nrl/create node-1 node-2 :rel_type {:source label})
    ))

(defn connect []
  (nr/connect! "http://localhost:7474/db/data/"))

(defn -main [graph-path & files]
  (connect)  
  (doseq [file files]
    (log :debug (str "Loading file: " file))
    (let [c (atom 0)]
      (doseq [tuple (parse-file file)]
        (if (= (mod @c 10000) 0)
          (log :debug (str file ": " @c)))
        (swap! c inc)
        (insert-tuple! tuple)))
    (log :debug "Loading complete.")
    (log :debug "Shutting down.")
    (log :debug "Shutdown complete!")))
