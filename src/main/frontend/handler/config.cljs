(ns frontend.handler.config
  (:require [frontend.state :as state]
            [frontend.handler.repo :as repo-handler]
            [borkdude.rewrite-edn :as rewrite]
            [frontend.config :as config]
            [frontend.db :as db]))

(defn set-config!
  [k v]
  (when-let [repo (state/get-current-repo)]
    (let [path (str config/app-name "/" config/config-file)]
      (when-let [config (db/get-file-no-sub path)]
        (let [config (try
                       (rewrite/parse-string config)
                       (catch js/Error e
                         (println "Parsing config file failed: ")
                         (js/console.dir e)
                         {}))
              ks (if (vector? k) k [k])
              new-config (rewrite/assoc-in config ks v)]
          (state/set-config! repo new-config)
          (let [new-content (str new-config)]
            (repo-handler/set-config-content! repo path new-content)))))))

(defn toggle-ui-show-brackets! []
  (let [show-brackets? (state/show-brackets?)]
    (set-config! :ui/show-brackets? (not show-brackets?))))
