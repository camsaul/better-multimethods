(defproject methodical "0.9.5-alpha-SNAPSHOT"
  :description ""
  :url "https://github.com/camsaul/methodical"
  :min-lein-version "2.5.0"

  :license {:name "Eclipse Public License"
            :url  "https://raw.githubusercontent.com/camsaul/methodical/master/LICENSE"}

  :aliases
  {"repl"                      ["with-profile" "+repl" "repl"]
   ;; run lein deps with all dependencies from all the various profiles merged in. Useful for CI so we can cache
   ;; everything
   "deploy"                    ["with-profile" "+deploy" "deploy"]
   "all-deps"                  ["with-profiles" "-user,+all-profiles" "deps"]
   "test"                      ["with-profile" "+test" "test"]
   "cloverage"                 ["with-profile" "+cloverage" "cloverage"]
   "profile"                   ["with-profile" "+profile" "run"]
   "eastwood"                  ["with-profile" "+eastwood" "eastwood"]
   "bikeshed"                  ["with-profile" "+bikeshed" "bikeshed" "--max-line-length" "120"]
   "kibit"                     ["with-profile" "+kibit" "kibit"]
   "check-namespace-decls"     ["with-profile" "+check-namespace-decls" "check-namespace-decls"]
   "docstring-checker"         ["with-profile" "+docstring-checker" "docstring-checker"]
   "check-reflection-warnings" ["with-profile" "+reflection-warnings" "check"]
   ;; `lein lint` will run all linters. Except for reflecion warnings, use the script for that
   "lint"                      ["do" ["eastwood"] ["bikeshed"] ["kibit"] ["check-namespace-decls"] ["cloverage"]
                                ["docstring-checker"]]}

  :dependencies
  [[pretty "1.0.0"]
   [potemkin "0.4.5"]]

  :aot [methodical.interface methodical.impl.standard]

  :jvm-opts ["-Dclojure.compiler.direct-linking=true"]

  :profiles
  {:dev
   {:dependencies
    [[org.clojure/clojure "1.10.1"]
     [criterium "0.4.5"]
     [pjstadig/humane-test-output "0.9.0"]]

    :injections
    [(require 'pjstadig.humane-test-output)
     (pjstadig.humane-test-output/activate!)]

    :jvm-opts ["-Xverify:none"]

    :source-paths ["dev"]}

   :repl
   {:injections [(set! *warn-on-reflection* true)]}

   :test
   {}

   :cloverage
   {:dependencies
    ;; 1. Cloverage dependency is normally injected when the plugin is ran. By explicitly specifying it here we can
    ;; cache it in CI
    ;;
    ;; 2. I forked Cloverage to add support for `deftype`; when that's merged upstream we can stop using my fork
    [[camsaul/cloverage "1.1.2"]
     ;; Required by both Potemkin and Cloverage, but Potemkin uses an older version that breaks Cloverage's ablity to
     ;; understand certain forms. Explicitly specify newer version here.
     [riddley "0.1.14"]]

    :plugins
    [[camsaul/lein-cloverage "1.1.2"]]

    ;; don't count ./dev stuff for code coverage calcualations.
    :source-paths ^:replace ["src"]

    :cloverage
    {:fail-threshold 90}}

   :profile
   {:main ^:skip-aot methodical.profile}

   :eastwood
   {:plugins
    [[jonase/eastwood "0.3.5" :exclusions [org.clojure/clojure]]]

    :eastwood
    {:config-files
     ["./.eastwood-config.clj"]

     :exclude-namespaces [:test-paths]

     :remove-linters
     ;;disabled for now until I figure out how to disable it in the one place it's popping up
     [:unused-ret-vals]

     :add-linters
     [:unused-private-vars
      :unused-namespaces
      #_:unused-fn-args ; disabled for now since it gives false positives that can't be disabled
      :unused-locals]}}

   :bikeshed
   {:dependencies
    ;; use latest tools.namespace instead of older version so we only need to fetch it once for all plugins.
    [[org.clojure/tools.namespace "0.2.11"]]

    :plugins
    [[lein-bikeshed "0.5.2"
      :exclusions [org.clojure/tools.namespace]]]}

   :kibit
   {:plugins
    [[lein-kibit "0.1.7"
      :exclusions [org.clojure/clojure]]]}

   :check-namespace-decls
   {:plugins               [[lein-check-namespace-decls "1.0.2"
                             :exclusions [org.clojure/clojure]]]
    :source-paths          ["test"]
    :check-namespace-decls {:prefix-rewriting true}}

   :docstring-checker
   {:plugins
    [[docstring-checker "1.1.0"]]

    :docstring-checker
    {:include [#"^methodical"]
     :exclude [#"test" #"^methodical\.profile$"]}}

   ;; run `lein check-reflection-warnings` to check for reflection warnings
   :reflection-warnings
   {:global-vars {*warn-on-reflection* true}}

   :all-profiles
   [:test :cloverage :profile :eastwood :bikeshed :kibit :check-namespace-decls :docstring-checker :reflection-warnings
    {}]

   :deploy
   {:dependencies [[org.clojure/clojure "1.10.1"]]}}

  :deploy-repositories
  [["clojars"
    {:url           "https://clojars.org/repo"
     :username      :env/clojars_username
     :password      :env/clojars_password
     :sign-releases false}]])
