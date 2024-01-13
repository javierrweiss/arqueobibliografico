(ns javierweiss.semanticscholar
  (:require [org.httpkit.client :as hk-client]
            [martian.core :as martian]
            [martian.httpkit :as kit]
            [clojure.edn :as edn]
            [cheshire.core :as json]
            [clojure.java.io :as io])
  (:import java.io.IOException))

(def semantic-scholar-api
  (kit/bootstrap-openapi "https://api.semanticscholar.org/graph/v1/swagger.json"))

(martian/explore semantic-scholar-api)


(def respuesta1 @(martian/response-for semantic-scholar-api :get-graph-paper-bulk-search {:query "democratic backsliding"
                                                                                          :fields "title,year,authors,abstract,openAccessPdf,publicationTypes,journal"
                                                                                          :fieldsOfStudy "Political Science"}))

(def respuesta2 @(martian/response-for semantic-scholar-api :get-graph-paper-bulk-search {:query "democratic backsliding"
                                                                                          :token "PCOKWVSKJJGM4TWNJNI3EUSQJIWVNUSRKBFEYK2JFUBHFI4V2LGAZUWMJTJCZTMSKKJSYDISZVJJEU4BHDG5JWGIFA25DQBCFUS5LQGQ2LJNIQRJWYLAACYHCUCQ"
                                                                                          :fields "title,year,authors,abstract,openAccessPdf,publicationTypes,journal"
                                                                                          :fieldsOfStudy "Political Science"}))

(def resultado (concat (get-in respuesta1 [:body :data])
                       (get-in respuesta2 [:body :data])))

(defn ->edn
  "Convierte una estructura de datos de Clojure a EDN"
  [datos]
  (prn-str datos))

(defn <-edn
  "Convierte un string en formato EDN a una estructura de datos de Clojure"
  [edn-str]
  (edn/read-string edn-str))

(defn to-file
  "Toma un string en formato edn o json y lo escribe a un archivo"
  [archivo datos]
  (try
    (spit archivo datos)
    (catch IOException e (prn (str "Hubo un error al generar el archivo: " (.getMessage e))))))

(defn from-edn-file
  "Lee un archivo .edn"
  [archivo]
  (try
    (slurp archivo)
    (catch IOException e (prn (str "Hubo un error al generar al leer el archivo edn: " (.getMessage e))))))

(->> resultado
     ->edn
     (to-file "democratic_backsliding_bib.edn"))

(tap> (-> (from-edn-file "democratic_backsliding_bib.edn")
          <-edn))

(defn ->json
  "Convierte una estructura de datos de Clojure a JSON"
  [datos]
  (json/generate-string datos))

(->> resultado
     ->json
     (to-file "democratic_backsliding_bib.json"))

(def urls-busqueda  (->> resultado
                         (map (juxt #(get-in % [:openAccessPdf :url]) #(:title %)))
                         (remove #(nil? (first %)))
                         (mapv (fn [[url titulo]] {:url url :titulo titulo}))))

(defn descargar-archivo
  [url]
  (try
    @(hk-client/get url)
    (catch IOException e (prn "Hubo un error al descargar el archivo " (.getMessage e)))))

(def url->inpustream (map (fn [m] (update m :url #(-> % descargar-archivo :body))) urls-busqueda))

(defn inputstream->file
  "Convierte un inputstream en un archivo"
  [^org.httpkit.BytesInputStream input ruta]
  (when (instance? org.httpkit.BytesInputStream input)
    (with-open [out (io/output-stream ruta)]
      (io/copy (.bytes input) out))))

(defn nombrar-archivo
  [titulo]
  (str titulo ".pdf"))
 
(doseq [{:keys [titulo url]} url->inpustream :let [archivo (nombrar-archivo titulo)]]
  (inputstream->file url (str "bibliografia/" archivo)))

 
(comment
 
  (tap> urls-busqueda)
  (tap>
   @(hk-client/get
     (str "https://api.semanticscholar.org/graph/v1/paper/search?"
          (hk-client/query-string {:query "Habermas"
                                   :offset 100}))))

  (tap> @(hk-client/get (str "https://api.semanticscholar.org/graph/v1/paper/"
                             "c129850b9bc323f116118eb1b9d4777226969012"
                             "?"
                             (hk-client/query-string {:fields "title,year,authors,abstract,openAccessPdf"}))))

  (count resultado)
  (def m {:resp "assas"
          :token "dsd sdmadmp ew elwme"
          :data {:data1 23332
                 :titulo "bsd smda sa"
                 :autor [{:nombre "sdd dffdsd"}
                         {:nombre "de3333"}]}})
  (get m :x)

  (:datos m)

  (get-in m [:data :autor 1 :nombre])

  (tap> @(martian/response-for semantic-scholar-api :get-graph-paper-relevance-search {:query "Habermas"
                                                                                       :offset 100}))

  (tap> @(martian/response-for semantic-scholar-api :get-graph-get-paper {:paper_id "c129850b9bc323f116118eb1b9d4777226969012"
                                                                          :fields "title,year,authors,abstract,openAccessPdf"}))

  (tap> @(martian/response-for semantic-scholar-api :get-graph-paper-bulk-search {:query "democratic backsliding"
                                                                                  :fields "title,year,authors,abstract,openAccessPdf,publicationTypes,journal"
                                                                                  :fieldsOfStudy "Political Science"}))


  (tap> @(martian/response-for semantic-scholar-api :get-graph-paper-bulk-search {:query "democratic backsliding"
                                                                                  :token "PCOKWVSKJJGM4TWNJNI3EUSQJIWVNUSRKBFEYK2JFUBHFI4V2LGAZUWMJTJCZTMSKKJSYDISZVJJEU4BHDG5JWGIFA25DQBCFUS5LQGQ2LJNIQRJWYLAACYHCUCQ"
                                                                                  :fields "title,year,authors,abstract,openAccessPdf,publicationTypes,journal"
                                                                                  :fieldsOfStudy "Political Science"}))
  (tap> (get-in respuesta1 [:body :data 0]))

  (-> respuesta1 :body :data first :authors)

  (tap> resultado)

  (edn/read-string (prn-str m))

  (->json resultado)

  (with-open [out (io/output-stream "prueba.pdf")]
    (io/copy (.bytes (first archivos)) out))

  (inputstream->file (first archivos) "prueba2.pdf")
 
  (def test (update (first urls-busqueda) :url #(->> % descargar-archivo :body)))
    (tap> (+ 32 3)) 
  (tap> url->inpustream) 
  ) 