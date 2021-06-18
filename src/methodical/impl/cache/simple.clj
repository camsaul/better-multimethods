(ns methodical.impl.cache.simple
  "A basic, dumb cache. `SimpleCache` stores cached methods in a simple map of dispatch-value -> effective method; it
  offers no facilities to deduplicate identical methods for the same dispatch value. This behaves similarly to the
  caching mechanism in vanilla Clojure."
  (:require methodical.interface
            [potemkin.types :as p.types]
            [pretty.core :as pretty])
  (:import methodical.interface.Cache))

(comment methodical.interface/keep-me)

(p.types/deftype+ SimpleCache [atomm]
  pretty/PrettyPrintable
  (pretty [_]
    '(simple-cache))

  Cache
  (cached-method [_ dispatch-value]
    (get @atomm dispatch-value))

  (cache-method! [_ dispatch-value method]
    (swap! atomm assoc dispatch-value method))

  (clear-cache! [this]
    (reset! atomm {})
    this)

  (empty-copy [_]
    (SimpleCache. (atom {}))))
