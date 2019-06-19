(ns sean.liquid.rebl
  (:require [dk.salza.liq.editor :as editor]))

(defn get-symbol
  "Clean up Liquid's 'context' string so it's just a symbol name."
  []
  (re-find #"^[^\)\}\]]+" (:value (editor/get-context))))

(defn rebl-sexp
  "Evaluate the 'sexp' and also submit the code and the value to REBL."
  [sexp]
  (editor/eval-sexp (str "
(let [v " sexp "]
  ((requiring-resolve 'cognitect.rebl/submit) '" sexp " v)
  v)
")))

(defn rebl-last-sexp
  "Evaluate the selection or current sexp in REBL."
  []
  (rebl-sexp (or (editor/get-selection) (editor/sexp-at-point))))

(defn rebl-namespace
  "Inspect the current namespace in REBL."
  []
  (rebl-sexp "*ns*"))

(defn rebl-var
  "Inspect the symbol at point as a Var in REBL."
  []
  (rebl-sexp (str "#'" (get-symbol))))

(defn rebl-docs
  "Display docs in REBL for the symvol at point (as a Var).

  Does not show specs or arglists."
  []
  (rebl-sexp (str "(-> #'" (get-symbol) " meta :doc)")))

(defn rebl-def
  "Turn the current selection into a 'def'. This is useful for taking local
  bindings in a 'let' and turning them into top-level definition.

  It assumes you've selected the sexp and the preceding symbol in the
  binding. For example:

  (let [a 42
        b (* a a)]
    ,,,)

  Select 'a 42' and run this command.
  Then select 'b (* a a)' and run this command.
  Both new Vars will show up in REBL for inspection.
  Then you can eval subsequent forms in the 'let' binding in isolation
  and it'll use these top-level definitions, instead of the local
  symbols (that wouldn't be defined for the isolated form)."
  []
  (rebl-sexp (str "(def " (editor/get-selection) ")")))

(defn run-tests
  "Run all tests in the current namespace. Any output is captured and added
  into the summary hash map that is returned. That is submitted to REBL so
  it's easy to see at a glance."
  []
  (rebl-sexp "
(let [s (java.io.StringWriter.)
      w (java.io.PrintWriter. s)]
  (-> (binding [clojure.test/*test-out* w]
        (clojure.test/run-tests))
      (assoc :output (.toString s))))
"))

(defn run-test-at-point
  "Treat the symbol at point as a Var and run it as a test. Any output is
  captured and made part of the (string) result. If no output is generated,
  the test is assumed to have passed. That result is submitted to REBL so
  it's easy to see at a glance."
  []
  (let [test-name (get-symbol)]
    (rebl-sexp (str "
(let [s (java.io.StringWriter.)
      w (java.io.PrintWriter. s)]
  (do (binding [clojure.test/*test-out* w]
        (clojure.test/test-vars [#'" test-name "]))
      (str \"Tested \" '" test-name "\" : \" (or (not-empty (.toString s)) \"OK\"))))
"))))

;; start REBL in background
(future ((requiring-resolve 'cognitect.rebl/ui)))

;; overwrite c p p to inspect in the REBL
(swap! editor/editor assoc-in [::editor/keymaps "dk.salza.liq.keymappings.normal" "c" "p" "p"] rebl-last-sexp)

;; c r ... REBL-specific prefix
(swap! editor/editor assoc-in [::editor/keymaps "dk.salza.liq.keymappings.normal" "c" "r" "d"] rebl-docs)
(swap! editor/editor assoc-in [::editor/keymaps "dk.salza.liq.keymappings.normal" "c" "r" "D"] rebl-def)
(swap! editor/editor assoc-in [::editor/keymaps "dk.salza.liq.keymappings.normal" "c" "r" "n"] rebl-namespace)
(swap! editor/editor assoc-in [::editor/keymaps "dk.salza.liq.keymappings.normal" "c" "r" "r"] rebl-last-sexp)
(swap! editor/editor assoc-in [::editor/keymaps "dk.salza.liq.keymappings.normal" "c" "r" "v"] rebl-var)

;; c t ... test-specific prefix (but also submits to REBL)
(swap! editor/editor assoc-in [::editor/keymaps "dk.salza.liq.keymappings.normal" "c" "t" "x"] run-tests)
(swap! editor/editor assoc-in [::editor/keymaps "dk.salza.liq.keymappings.normal" "c" "t" "t"] run-test-at-point)

;; re-activate that keymap so changes take effect at startup
(editor/set-keymap "dk.salza.liq.keymappings.normal")

(in-ns 'user)
