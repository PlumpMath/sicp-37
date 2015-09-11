(ns sicp.ch-4
  (:require
   [clojure.test :refer [is are deftest]]
   [clojure.core.typed
    :refer [All
            Any
            ASeq
            CountRange
            ExactCount
            I
            IFn
            Int
            Kw
            NonEmptySeqable
            Num
            Option
            Pred
            Rec
            Seqable
            Symbol
            TFn
            U
            Val
            ann
            defalias
            letfn>
            ]
    :as typed
    ]
   [clojure.core.typed.unsafe
    :refer
    [
     ignore-with-unchecked-cast
     ]]
   [sicp.pair
    :refer [
            List
            any?
            car
            caadr
            cadr
            caddr
            cadddr
            cdadr
            cdr
            cddr
            cddr
            cdddr
            my-cons
            my-list
            pair?
            set-car!
            set-cdr!
            ]
    ]
   [clojure.math.numeric-tower
    :refer
    [
     abs
     gcd
     sqrt
     ]]
   [sicp.util
    :refer [
            p_
            pef
            ]]
   )
  (:import [sicp.pair Pair]))


(defalias Variable Symbol)
(defalias Expr (U Variable Any))
(defalias Env Any)
(defalias LookupEvalTable [Symbol -> (Option [Expr Env -> Expr])])
(defalias InsertEvalTable [Symbol [Expr Env -> Expr] -> Any])
(defalias EvalDispatchTable
  (IFn [(Val :lookup) -> LookupEvalTable]
       [(Val :insert!) -> InsertEvalTable]))


(declare _eval)


(ann ^:no-check apply-primitive-procedure [Any * -> Any])
(defn apply-primitive-procedure [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check compound-procedure? [Any * -> Any])
(defn compound-procedure? [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check define-variable! [Any * -> Any])
(defn define-variable! [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check extend-environment [Any * -> Any])
(defn extend-environment [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check first-operand [Any * -> Any])
(defn first-operand [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check lookup-variable-value [Any * -> Any])
(defn lookup-variable-value [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check make-procedure [Any * -> Any])
(defn make-procedure [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check primitive-procedure? [Any * -> Any])
(defn primitive-procedure? [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check procedure-body [Any * -> Any])
(defn procedure-body [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check procedure-environment [Any * -> Any])
(defn procedure-environment [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check procedure-parameters [Any * -> Any])
(defn procedure-parameters [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check set-variable-value! [Any * -> Any])
(defn set-variable-value! [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check my-true? [Any * -> Any])
(defn my-true? [& args]
  (throw (Exception. (str "NotImplemented"))))


(ann ^:no-check make-if [Any * -> Any])
(defn make-if [predicate consequent alternative]
  (list 'if predicate consequent alternative))


(ann ^:no-check make-lambda [Any * -> Any])
(defn make-lambda [parameters body]
  (cons 'lambda (cons parameters body)))


(ann ^:no-check tagged-list? [Any * -> Any])
(defn tagged-list? [exp tag]
  (and (sequential? exp)
       (= (first exp) tag)))


(ann ^:no-check cond-actions [Any * -> Any])
(def cond-actions rest)


(ann ^:no-check cond-predicate [Any * -> Any])
(def cond-predicate first)


(ann ^:no-check cond-clauses [Any * -> Any])
(def cond-clauses rest)


(ann ^:no-check cond-else-clause? [Any * -> Any])
(defn cond-else-clause? [clause]
  (= (cond-predicate clause) 'else))


(ann ^:no-check make-begin [Any * -> Any])
(defn make-begin [s]
  (cons 'begin s))


(ann ^:no-check first-exp [Any * -> Any])
(def first-exp first)


(ann ^:no-check last-exp? [Any * -> Any])
(defn last-exp? [s]
  (nil? (next s)))


(ann ^:no-check sequence->exp [Any * -> Any])
(defn sequence->exp [s]
  (when-let [s (seq s)]
    (if (last-exp? s)
      (first-exp s)
      (make-begin s))))


(ann ^:no-check expand-clauses [Any * -> Any])
(defn expand-clauses [clauses]
  (if-let [clauses (seq clauses)]
    (let [head (first clauses)
          more (rest clauses)]
      (if (cond-else-clause? head)
        (if (seq more)
          (throw (Exception. (str "else clause is not last -- cond->if: " clauses)))
          (sequence->exp (cond-actions head)))
        (make-if (cond-predicate head)
                 (sequence->exp (cond-actions head))
                 (expand-clauses more))))
    'false))


(ann ^:no-check cond->if [Any * -> Any])
(defn cond->if
  {:test #(do
            (is (= (cond->if '(cond ((ok) 1)
                                    ((bad) 2)
                                    (else 3)))
                   '(if (ok) 1
                        (if (bad) 2
                            3)))))}
  [exp]
  (expand-clauses (cond-clauses exp)))


(ann ^:no-check cond? [Any * -> Any])
(defn cond? [exp]
  (tagged-list? exp 'cond))


(ann ^:no-check rest-operands [Any * -> Any])
(def rest-operands rest)


(ann ^:no-check first-operand [Any * -> Any])
(def first-operand first)


(ann ^:no-check no-operands? [Any * -> Any])
(defn no-operands? [ops]
  (nil? (seq ops)))


(ann ^:no-check operands [Any * -> Any])
(def operands rest)


(ann ^:no-check operator [Any * -> Any])
(def operator first)


(ann ^:no-check application? [Any * -> Any])
(def application? sequential?)


(ann ^:no-check rest-exps [Any * -> Any])
(def rest-exps rest)


(ann ^:no-check begin-actions [Any * -> Any])
(def begin-actions rest)


(ann ^:no-check begin? [Any * -> Any])
(defn begin? [exp]
  (tagged-list? exp 'begin))


(ann ^:no-check if-alternative [Any * -> Any])
(defn if-alternative [exp]
  (if (next (rest (rest exp)))
    (nth exp 3)
    'false))


(ann ^:no-check if-consequent [Any * -> Any])
(defn if-consequent [exp]
  (nth exp 2))


(ann ^:no-check if-predicate [Any * -> Any])
(def if-predicate second)


(ann ^:no-check if? [Any * -> Any])
(defn if? [exp]
  (tagged-list? exp 'if))


(ann ^:no-check lambda-body [Any * -> Any])
(defn lambda-body [exp]
  (rest (rest exp)))


(ann ^:no-check lambda-parameters [Any * -> Any])
(def lambda-parameters second)


(ann ^:no-check lambda? [Any * -> Any])
(defn lambda? [exp]
  (tagged-list? exp 'lambda))


(ann ^:no-check definition-value [Any * -> Any])
(defn definition-value [exp]
  (if (symbol? (second exp))
    (nth exp 2)
    (make-lambda (second (rest exp))
                 (rest (rest exp)))))


(ann ^:no-check definition-variable [Any * -> Any])
(defn definition-variable [exp]
  (if (symbol? (second exp))
    (second exp)
    (first (second exp))))


(ann ^:no-check definition? [Any * -> Any])
(defn definition? [exp]
  (tagged-list? exp 'define))


(ann ^:no-check assignment-value [Any * -> Any])
(defn assignment-value [exp]
  (nth exp 2))


(ann ^:no-check assignment-variable [Any * -> Any])
(def assignment-variable second)


(ann ^:no-check assignment? [Any * -> Any])
(defn assignment? [exp]
  (tagged-list? exp 'set!))


(ann ^:no-check text-of-quotation [Any * -> Any])
(def text-of-quotation second)


(ann ^:no-check quoted? [Any * -> Any])
(defn quoted? [exp]
  (tagged-list? exp 'quote))


(ann variable? (Pred Variable))
(def variable? symbol?)


(ann ^:no-check self-evaluating? [Any * -> Any])
(defn self-evaluating? [exp]
  (or (number? exp)
      (string? exp)))


(ann ^:no-check eval-definition [Any * -> Any])
(defn eval-definition [exp env]
  (define-variable! (definition-variable exp)
                    (_eval (definition-value exp) env)
                    env))


(ann ^:no-check eval-assignment [Any * -> Any])
(defn eval-assignment [exp env]
  (set-variable-value! (assignment-variable exp)
                       (_eval (definition-value exp) env)
                       env))


(ann ^:no-check eval-sequence [Any * -> Any])
(defn eval-sequence [exps env]
  (cond (last-exp? exps) (_eval (first-exp exps) env)
        :else (do (_eval (first-exp exps) env)
                  (eval-sequence (rest-exps exps) env))))


(ann ^:no-check eval-if [Any * -> Any])
(defn eval-if [exp env]
  (if (my-true? (_eval (if-predicate exp) env))
    (_eval (if-consequent exp) env)
    (_eval (if-alternative exp) env)))


(ann ^:no-check list-of-values [Any * -> Any])
(defn list-of-values [exps env]
  (if (no-operands? exps)
    ()
    (cons (_eval (first-operand exps) env)
          (list-of-values (rest-operands exps) env))))


(ann ^:no-check list-of-values-4-1-lr [Any * -> Any])
(defn list-of-values-4-1-lr
  "Q. 4.1"
  [exps env]
  (if (no-operands? exps)
    ()
    (let [l (_eval (first-operand exps) env)
          r (list-of-values-4-1-lr (rest-operands exps) env)]
      (cons l r))))


(ann lookup (All [k v q] [(Option (Seqable '[k v])) q -> (Option v)]))
(defn lookup
  ([table key]
   (when-let [table (seq table)]
     (let [[k v] (first table)]
       (if (= k key)
         v
         (recur (rest table) key))))))


(ann insert (All [k v] [(Option (Seqable '[k v])) k v -> (ASeq '[k v])]))
(defn insert [table key value]
  (if-let [table (seq table)]
    (let [[ k v :as kv] (first table)]
      (if (= k key)
        (cons [k value] (rest table))
        (cons kv (insert (rest table) key value))))
    (cons [key value] nil)))


(ann ^:no-check make-table [-> EvalDispatchTable]) ; if I use `cond`, this ^:no-check is not needed
(defn make-table
  {:test #(let [table (make-table)]
            (is (nil? ((table :lookup) :a)))
            ((table :insert!) 'a 1)
            (is (= ((table :lookup) 'a) 1)))}
  []
  (let [local-table (typed/atom :- (Option (ASeq '[Symbol [Expr Env -> Expr]])) nil)]
    (letfn> [dispatch :- EvalDispatchTable
             (dispatch [m]
                       (case m

                         :lookup
                         (typed/fn
                           [t :- Symbol]
                           (lookup @local-table t))

                         :insert!
                         (typed/fn [t :- Symbol f :- [Expr Env -> Expr]]
                           (swap! local-table insert t f))

                         (throw (Exception. (str "unknown operation for table: " m)))))]
      dispatch)))


(ann ^:no-check list-of-values-4-1-rl [Any * -> Any])
(defn list-of-values-4-1-rl
  "Q. 4.1"
  [exps env]
  (if (no-operands? exps)
    ()
    (let [r (list-of-values-4-1-rl (rest-operands exps) env)
          l (_eval (first-operand exps) env)]
      (cons l r))))


(ann ^:no-check _apply [Any * -> Any])
(defn _apply [procedure arguments]
  (cond
    (primitive-procedure? procedure) (apply-primitive-procedure procedure arguments)
    (compound-procedure? procedure) (eval-sequence
                                     (procedure-body procedure)
                                     (extend-environment
                                      (procedure-parameters procedure)
                                      arguments
                                      (procedure-environment procedure)))
    :else (throw (Exception. (str "unknown procedure type -- _apply: " procedure)))))


(let [eval-dispatch-table (make-table)]
  (ann lookup-eval-table LookupEvalTable)
  (def lookup-eval-table (eval-dispatch-table :lookup))

  (ann insert-eval-table! InsertEvalTable)
  (def insert-eval-table! (eval-dispatch-table :insert!))
  )


(ann ^:no-check tag-of [Expr -> Symbol])
(defn tag-of
  {:test #(do (are [exp tag] (= (tag-of exp) tag)
                1 'self-evaluating
                "str" 'self-evaluating
                'var 'variable
                '(if cond then else) 'if)
              (is (thrown? Exception (tag-of :kw))))}
  [exp]
  (cond
    (sequential? exp) (or (first exp) 'application)
    (variable? exp) 'variable
    (self-evaluating? exp) 'self-evaluating
    :else (throw (Exception. (str "unknown expression type -- tag-of: " exp)))))


(ann ^:no-check _eval [Any * -> Any])
(defn _eval
  "Q. 4.3"
  {:test #(do (are [exp env val] (= (_eval exp env) val)
                1 nil 1
                "str" nil "str"
                '(quote (a b)) nil '(a b)))}
  [exp env]
  (if-let [impl (lookup-eval-table (tag-of exp))]
    (impl exp env)
    (throw (Exception. (str "unknown expression type -- _eval: " exp)))))


(insert-eval-table! 'self-evaluating
                    (typed/fn [exp :- Expr env :- Env]
                      exp))
(insert-eval-table! 'variable
                    (typed/fn [exp :- Expr env :- Env]
                      (lookup-variable-value exp env)))
(insert-eval-table! 'quote
                    (typed/fn [exp :- Expr env :- Env]
                      (text-of-quotation exp)))
(insert-eval-table! 'set!
                    (typed/fn [exp :- Expr env :- Env]
                      (eval-assignment exp env)))
(insert-eval-table! 'define
                    (typed/fn [exp :- Expr env :- Env]
                      (eval-definition exp env)))
(insert-eval-table! 'if
                    (typed/fn [exp :- Expr env :- Env]
                      (eval-if exp env)))
(insert-eval-table! 'lambda
                    (typed/fn [exp :- Expr env :- Env]
                      (make-procedure (lambda-parameters exp)
                                      (lambda-body exp)
                                      env)))
(insert-eval-table! 'begin
                    (typed/fn [exp :- Expr env :- Env]
                      (eval-sequence (begin-actions exp) env)))
(insert-eval-table! 'cond
                    (typed/fn [exp :- Expr env :- Env]
                      (_eval (cond->if exp) env)))
(insert-eval-table! 'application
                    (typed/fn [exp :- Expr env :- Env]
                      (_apply (_eval (operator exp) env)
                              (list-of-values (operands exp) env))))


;; Q. 4.2-a (define x 3) -> application


(ann ^:no-check operands-4-2-b [Any * -> Any])
(defn operands-4-2-b [exp]
  (rest (rest exp)))


(ann ^:no-check operator-4-2-b [Any * -> Any])
(defn operator-4-2-b [exp]
  (first (rest exp)))


(ann ^:no-check application?-4-2-b [Any * -> Any])
(defn application?-4-2-b [exp]
  (tagged-list? exp 'call))


(ann ^:no-check _eval-4-2-b [Any * -> Any])
(defn _eval-4-2-b
  "Q. 4.2-b"
  [exp env]
  (cond
    (application?-4-2-b exp) (_apply (_eval (operator-4-2-b exp) env)
                                     (list-of-values (operands-4-2-b exp) env))
    (self-evaluating? exp) exp
    (variable? exp) (lookup-variable-value exp env)
    (quoted? exp) (text-of-quotation exp)
    (assignment? exp) (eval-assignment exp env)
    (definition? exp) (eval-definition exp env)
    (if? exp) (eval-if exp env)
    (lambda? exp) (make-procedure (lambda-parameters exp)
                                  (lambda-body exp)
                                  env)
    (begin? exp) (eval-sequence (begin-actions exp) env)
    (cond? exp) (recur (cond->if exp) env)
    :else (throw (Exception. (str "unknown expression type -- _eval-4-2-b: " exp)))))
