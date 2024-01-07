(ns javierweiss.semanticscholar
  (:require [org.httpkit.client :as hk-client]
            [martian.core :as martian]
            [martian.httpkit :as kit]))


(tap>
 @(hk-client/get 
   (str "https://api.semanticscholar.org/graph/v1/paper/search?" 
        (hk-client/query-string {:query "Habermas"
                                 :offset 100})))) 
 
(tap> @(hk-client/get (str "https://api.semanticscholar.org/graph/v1/paper/" 
                           "c129850b9bc323f116118eb1b9d4777226969012"
                           "?"
                           (hk-client/query-string {:fields "title,year,authors,abstract,openAccessPdf"})))) 
 
(def semantic-scholar-api 
  (kit/bootstrap-openapi "https://api.semanticscholar.org/graph/v1/swagger.json"))
     
(martian/explore semantic-scholar-api)

(tap> @(martian/response-for semantic-scholar-api :get-graph-paper-relevance-search {:query "Habermas"
                                                                                     :offset 100}))

(tap> @(martian/response-for semantic-scholar-api :get-graph-get-paper {:paper_id "c129850b9bc323f116118eb1b9d4777226969012"
                                                                        :fields "title,year,authors,abstract,openAccessPdf"}))
