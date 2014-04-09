(ns dbpedia-importer.core
    (:require [clojure.java.io :as io]
              [clojurewerkz.neocons.rest :as nr]
              [clojurewerkz.neocons.rest.nodes :as nn]
              [clojurewerkz.neocons.rest.relationships :as nrl]
              [clojurewerkz.neocons.rest.batch :as b]
              [clojure.data.json :as json]
               [clojurewerkz.neocons.rest.records :as records]
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
(def batch (atom []))
 
(defn get-to-string [resource-1]
    (if-let [ids (get @id-map resource-1)]
       (if (> (:neo4id  ids) 0) (str "node/" (:neo4id  ids) "/relationships")  (str "{" (:id ids) "}/relationships"))))

(defn get-to-body-string [resource-2]
     (if-let [ids (get @id-map resource-2)]
       (if (> (:neo4id  ids) 0) (str (:neo4id  ids))  (str "{" (:id ids) "}"))))

 (defn get-relation-statement [resource-1 label resource-2]
      (let [to-string (get-to-string resource-1)
              to-body-string (get-to-body-string resource-2)]
       {:method "POST" :to to-string  :body   {:to  to-body-string :data {} :type label} :id @id-counter}))

(defn check-and-get-id [res]
  (if-let [ids (get @id-map res)]
    ; If the resource has aleady been added, just return the id.
    (if (> (:neo4id  ids) 0) (:neo4id  ids) (:id ids))
    ; Otherwise, add the node for the node, and remember its id for later.
    (let [id @id-counter]
       (swap! id-map assoc res {:id id :neo4id -1})
       (swap! id-counter inc)
       (swap! batch conj {:method "POST" :to  "/node" :body   {:resource res} :id  id})
      id) ; Return the counter id
  )
)

(defn prepare-batch-entry [tuple]
  ; Get the resource and label names out of the tuple.
  (let [[resource-1 label resource-2 & _ ] tuple]
        (check-and-get-id resource-2)
        (check-and-get-id resource-1)
        (swap! batch conj (get-relation-statement resource-1 label resource-2))
        (swap! id-counter inc)
        ))

(defn update-resource-ids [lazyresponse]
  (let [datamap (:data lazyresponse)]
         (if (contains? datamap :resource)
            (let [ids (get @id-map (:resource datamap))]
              (swap! id-map assoc (:resource datamap) {:id (:id ids) :neo4id (:id lazyresponse)})))))


(defn commit-batch []
  ;(println (json/write-str @batch))
  (doseq [lazyresponse (b/perform @batch)]
               (update-resource-ids lazyresponse))
  (swap! batch empty))


(defn -main [url & files]
  (nr/connect! url)
  (doseq [file files]
    (println (str "Loading file: " file))
    (let [c (atom 0)]
      (doseq [tuple (parse-file file)]
        (if (= (mod @c 50000) 0)
          (commit-batch))
        (swap! c inc)
        (prepare-batch-entry tuple)
        )
      )
    )
  )
