(ns dbpedia-importer.core
    (:require [clojure.java.io :as io]
              [clojurewerkz.neocons.rest :as nr]
              [clojurewerkz.neocons.rest.nodes :as nn]
              [clojurewerkz.neocons.rest.relationships :as nrl]
              [clojurewerkz.neocons.rest.batch :as b]
              [clojure.data.json :as json]
    )
  (:import [uk.ac.manchester.cs.owl.owlapi.turtle.parser TurtleParser]))
 

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
(def id-map (atom {}))
(def id-counter(atom 0))
(def batch (atom '()))
 
(defn check-and-get-id [res]
  (if-let [id (get @id-map res)]
    ; If the resource has aleady been added, just return the id.
    id
    ; Otherwise, add the node for the node, and remember its id for later.
    (let [id @id-counter]
      (swap! id-map assoc res id)
       (swap! id-counter inc)
      id)))

(defn create-batch-entry [id1 resource-1 id2 resource-2 label]
    (let [ node1 {:method "POST" :to  "/node" :body   {:resource resource-1} :id  id1}
             node2 {:method "POST" :to  "/node" :body   {:resource resource-2} :id  id2}
             rel {:method "POST" :to  (str "{" id1 "}/relationships") :body   {:to  (str "{" id2 "}") :data {} :type label} :id  (check-and-get-id label)}
            ]
       (list node1 node2 rel)))

(defn prepare-batch-entry [tuple]
  ; Get the resource and label names out of the tuple.
  (let [[resource-1 label resource-2 & _ ] tuple
        ; Upsert the resource nodes.
        node-id-1 (check-and-get-id resource-1)
        node-id-2 (check-and-get-id resource-2)]
     (create-batch-entry node-id-1 resource-1 node-id-2 resource-2 label)))

(defn connect [url]
  (nr/connect! url))

(defn commit-batch []
  (doall (b/perform @batch))
  (swap! batch empty))

(defn -main [url & files]
  (connect url)  
  (doseq [file files]
    (println (str "Loading file: " file))
    (let [c (atom 0)]
      (doseq [tuple (parse-file file)]
        (if (= (mod @c 1000) 0)
          (commit-batch))
        (swap! c inc)
        (swap! batch concat  (prepare-batch-entry tuple))))
    (println "Loading complete.")
    (println "Shutting down.")
    (println "Shutdown complete!")))
