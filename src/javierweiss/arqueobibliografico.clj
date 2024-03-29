(ns javierweiss.arqueobibliografico
  (:gen-class))

(defn greet
  "Callable entry point to the application."
  [data]
  (println 
   (str "Hola " 
        (or 
         (:name data)
         "World") 
        "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (greet {:name (first args)}))
