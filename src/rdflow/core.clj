(ns rdflow.core
  (:import (com.hp.hpl.jena.rdf.model ModelFactory ResourceFactory Model)
           (java.io StringReader)))


;; #RDFlow
;; RDFlow is a dataflow programming ontology that specifies how to pipe data between http servers to form complex applications. It'My main intention for it is to be used to build web-applications that keep application logic sepparate by actually running it on different servers. Eventually, it should allow for cool stuff like lazy-evaluation of routes and caching of intermediate results. For now, though, it's going to be a bare-bones way of aggregating multiple http servers under one domain.

;; The first thing we're going to need to do to run an RDFlow server is to actually grab the rdf describing the routes it's going to publish.
;; TODO For now, I'm going to hard-code this situation, but later this is going to have to be better defined. Likely, it will also use databinder to get the subgraph needed.

(declare exquery)
(declare get-configuration)
(declare http-handler)
(declare routes)
(declare rdf-seq)

(defn get-configuration []
  (let [model (. ModelFactory createDefaultModel)]
    (. model read (new StringReader routes) "http://localhost:3000" "TURTLE")
    model))

;; Here's the actual turtle data we'll use to deal with routes in our example application.
(def routes
  "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix flow: <http://logangilmour.com/rdflow#> .
@prefix data: <http://localhost:4000/> .
</test> flow:routes (data:test data:test2 data:test3) .
")

;; So now that we've sorted out our configuration, the next step is to use it to actually serve up some sweet, sweet urls. `http-handler` is hooked into the ring webserver configuration via project.clj. It gets called whenever we get an http request.


(defn http-handler [request]
  (let [model (get-configuration)
        property (. ResourceFactory createProperty "http://logangilmour.com/rdflow#routes")
        routes (iterator-seq
                (. model listObjectsOfProperty property))
        answers (map #(rdf-seq model %) routes)
        ]
    {:status 200
   :headers {"Content-Type" "text/html"}
     :body (map str answers)}))

;; If we're going to be able to use the rdf data we have to route things in an order, we're going to have to be able to turn the rdf in the Jena model into a sequence. We'll do this with a simple tail-recursive loop over the model.

(defn rdf-seq [mod top]
  (seq (loop [model mod
              head top
              list []]
         (let [first-p (. ResourceFactory createProperty "http://www.w3.org/1999/02/22-rdf-syntax-ns#first")
               rest-p (. ResourceFactory createProperty "http://www.w3.org/1999/02/22-rdf-syntax-ns#rest")]
           (if (= (str head) "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")
             list
             (recur model
                    (.asResource (.getObject (. model getProperty head rest-p)))
                    (conj list
                          (.asResource (.getObject (. model getProperty head first-p))))))))))
