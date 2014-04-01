(ns web.calc
  (:require [compojure.core :refer [routes GET]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

(defn hello [name]
  (list
   [:h1 "Welcome"]
   [:p "Hello " name]))

(defn run-twice [handler]
  (fn [request]
    (when-let [response (handler request)]
      (cond (sequential? response) (list response response)
            (string? response) (str response response)
            :else response))))

(defn raw-handler [request]
  (when (= {:scheme :http, :uri "/test", :request-method :get}
           (select-keys request [:scheme :uri :request-method]))
    (let [{:keys [arg]} (:params request)]
      (hello arg))))

(defn wrap-html [handler]
  (fn [request]
    (when-let [response (handler request)]
      {:status 200, :body (html5 response)})))

(def handler (routes (run-twice raw-handler)
                     (GET "/hello" [name]
                       (hello name))))

(def app (-> (routes (wrap-html handler)
                     (constantly {:status 404 :body "Not found"}))
             (wrap-keyword-params)
             (wrap-params)))

(defonce jetty (atom nil))

(defn start []
  (reset! jetty (run-jetty #'app {:join? false :port 8088})))

(defn stop []
  (.stop @jetty)
  (reset! jetty nil))