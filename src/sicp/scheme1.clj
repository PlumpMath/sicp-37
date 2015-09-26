(ns sicp.scheme1
  (:require
   [clojure.test :refer [is are deftest]]
   [sicp.pair
    :refer [
            List
            any?
            car
            cdr
            caar
            cadr
            cdar
            cddr
            caaar
            caadr
            cadar
            caddr
            cdaar
            cdadr
            cddar
            cdddr
            caaaar
            caaadr
            caadar
            caaddr
            cadaar
            cadadr
            caddar
            cadddr
            cdaaar
            cdaadr
            cdadar
            cdaddr
            cddaar
            cddadr
            cdddar
            cddddr
            foldl
            foldr-tc
            length
            my-concat
            my-cons
            my-list
            my-map
            my-reverse
            pair?
            reverse-map
            set-car!
            set-cdr!
            ]
    ]
   [sicp.scheme-util
    :refer [
            error
            null
            _nil
            _true
            _false
            scheme-of
            list-cons
            ]]
   [sicp.util
    :refer [
            p_
            ]]
   )
  )

(declare procedure?
         procedure-parameters
         procedure-body
         primitive?)

(defn user-str
  {:test #(do
            (is (= (user-str (my-list 1 2)) "12")))}
  [objects]
  (letfn [(conv [o]
            (str
             (if (procedure? o)
               ['compound-procedure
                (procedure-parameters o)
                (procedure-body o)]
               o)))]
    (foldl (my-map conv) str "" objects)))

(defn str-frame [frame]
  (when-let [[k v] (first frame)]
    (str (when-not (primitive? v)
           (str k "\t" (user-str (my-list v)) "\n"))
         (str-frame (rest frame)))))

(declare current
         enclosing)
(defn str-env [env]
  (if env
    (str "^^^^\n"
         (str-frame (current env))
         (str-env (enclosing env)))
    "===="))

(defprotocol IEnv
  (current [env])
  (enclosing [env])
  )
(deftype Env [_current _enclosing]
 IEnv
 (current [self] _current)
 (enclosing [self] _enclosing)
 Object
 (toString [self] (str-env self))
)

(declare _eval
         _apply
         )

(defn make-frame
  "Assumption: no parallel access to environment"
  [vars vals]
  (let [m (java.util.HashMap.)]
    (loop [vars vars
           vals vals]
      (if (or (nil? vars) (nil? vals))
        m
        (do (.put m (car vars) (car vals))
            (recur (cdr vars) (cdr vals)))))))

(defn self-evaluating? [exp]
  (or (number? exp)
      (string? exp)
      (nil? exp)))

(def variable? symbol?)

(defn tagged-list? [tag exp]
  (and (pair? exp)
       (= (car exp) tag)))

(defmacro make-pred [sym]
  `(defn ~(symbol (str sym "?")) [exp#]
     (tagged-list? (quote ~sym) exp#)))

(make-pred quote)
(make-pred set!)
(make-pred define)
(make-pred if)
(make-pred lambda)
(make-pred begin)
(make-pred cond)
(make-pred let)
(make-pred let*)
(make-pred letrec)
(make-pred and)
(make-pred or)
(make-pred procedure)
(make-pred primitive)

(defn user-print [objects]
  (print (user-str objects)))

(defn print-env [env]
  ; use str-env to print nil properly
  (println (str-env env)))

(defn _print-env [env]
  (print-env env)
  env)

(def my-false? false?)
(defn my-true? [x]
  (not (my-false? x)))

(defn make-begin [s]
  (my-cons 'begin s))

(defn sequence->exp [s]
  (if (pair? (cdr s))
    (make-begin s)
    (car s)))

(defn make-let
  ([pairs body] (list-cons 'let pairs body))
  ([name pairs body] (list-cons 'let name pairs body)))

(defn make-lambda [params body]
  (list-cons 'lambda params body))

(defn make-if
  ([pred then] (my-list 'if pred then))
  ([pred then else] (my-list 'if pred then else)))

(declare definition-variable
         definition-value)
(defn scan-out-defines [body]
  (let [b (foldl (fn [acc exp]
                   (if (define? exp)
                     (my-cons (my-cons exp (car acc))
                              (cdr acc))
                     (my-cons (car acc)
                              (my-cons exp (cdr acc)))))
                 (my-cons nil nil)
                 body)
        defs (car b)]
    (my-concat
     (reverse-map (fn [d] (my-list 'define
                                   (definition-variable d)
                                   (my-list 'quote '*unassigned*)))
                  defs)
     (reverse-map (fn [d] (my-list 'set!
                                   (definition-variable d)
                                   (definition-value d)))
                  defs)
     (my-reverse (cdr b)))))

(defn make-procedure [params body env]
    (my-list 'procedure params (scan-out-defines body) env))

(defn cond->if
  {:test #(do
            (are [in out] (= (cond->if (scheme-of in)) (scheme-of out))
              '(cond (else 1)) 1
              '(cond ((a) b)) '(if (a) b false)
              '(cond ((a) b)
                     ((c) d))
              '(if (a)
                 b
                 (if (c)
                   d
                   false))
              '(cond ((a)))
              '(let ((v (a))
                     (more (lambda () false)))
                 (if v v (more)))
              '(cond (a b)
                     ((c))
                     (e => f)
                     (else g))
              '(if a
                 b
                 (let ((v (c))
                       (more (lambda ()
                                     (let ((v e)
                                           (more (lambda () g)))
                                       (if v
                                         (f v)
                                         (more))))))
                   (if v
                     v
                     (more))))
              ))}
  [exp]
  (letfn [(expand [exp]
            (if (nil? exp)
              _false
              (let [head (car exp)
                    more (cdr exp)]
                (if (= (car head) 'else)
                  (if (nil? more)
                    (sequence->exp (cdr head))
                    (error (str "else clauses is not last -- cond->if: " exp)))
                  (let [pred (car head)
                        actions (cdr head)]
                    (if (nil? actions)
                      (make-let (my-list (my-list 'v pred)
                                         (my-list 'more (make-lambda
                                                         null
                                                         (my-list
                                                          (expand more)))))
                                (my-list
                                 (make-if 'v 'v (my-list 'more))))
                      (if (= (car actions) '=>)
                        (if (= (length actions) 2)
                          (make-let (my-list (my-list 'v pred)
                                             (my-list 'more (make-lambda
                                                             null
                                                             (my-list (expand more)))))
                                    (my-list
                                     (make-if 'v
                                              (my-list (cadr actions) 'v)
                                              (my-list 'more))))
                          (error (str "too few/many actions for => -- cond->if: " exp)))
                        (make-if pred
                                 (sequence->exp actions)
                                 (expand more)))))))))]
    (expand (cdr exp))))

(def if-predicate cadr)
(def if-consequent caddr)
(def if-alternative cadddr)

(defn let->combination
  {:test #(do
            (are [in out] (= (let->combination (scheme-of in))
                             (scheme-of out))
              '(let ((a b)) a)
              '((lambda (a) a) b)))}
  [exp]
  (let [pairs (cadr exp)
        body (cddr exp)]
    (let [vars (my-map car pairs)
          vals (my-map cadr pairs)]
      (my-cons (make-lambda vars body) vals))))

(defn let*->nested-lets [exp]
  (let [pairs (cadr exp)
        body (cddr exp)]
    (letfn [(expand [pairs]
              (let [head (car pairs)
                    more (cdr pairs)]
                (if (nil? more)
                  (make-let (my-list head) body)
                  (make-let (my-list head)
                            (my-list (expand more))))))]
      (if (nil? pairs)
        (make-let null body)
        (expand pairs)))))

(defn expand-letrec
  {:test #(do
            (are [in out] (= (expand-letrec (scheme-of in)) (scheme-of out))
              '(letrec ((a b)) a)
              '(let ((a (quote *unassigned*)))
                 (set! a b)
                 a)
              '(letrec ((fact
                         (lambda (n)
                                 (if (= n 1)
                                   1
                                   (* n (fact (- n 1)))))))
                       (fact 10))
              '(let ((fact (quote *unassigned*)))
                 (set! fact (lambda (n)
                                    (if (= n 1)
                                      1
                                      (* n (fact (- n 1))))))
                 (fact 10))))}
  [exp]
  (let [pairs (cadr exp)
        body (cddr exp)]
    (if (nil? pairs)
      (error (str "No decl nor body provided -- letrec: " exp))
      (make-let (my-map (fn [p]
                          (my-list (car p) (my-list 'quote '*unassigned*)))
                        pairs)
                (my-concat
                 (my-map (fn [p] (my-list 'set! (car p) (cadr p))) pairs)
                 body)))))

(defn define-variable! [var val env]
  (let [^java.util.HashMap f (current env)]
    (.put f var val)
    var))

(defn definition-variable [exp]
  (let [var (cadr exp)]
    (if (symbol? var)
      var
      (car var))))

(defn definition-value [exp]
  (let [var (cadr exp)]
    (if (symbol? var)
      (caddr exp)
      (make-lambda (cdr var)
                   (cddr exp)))))

(defn lookup-env [var env]
  (when env
    (let [^java.util.HashMap f (current env)]
      (if (.containsKey f var)
        f
        (recur var (enclosing env))))))

(defn set-variable-value! [var val env]
  (if-let [^java.util.HashMap m (lookup-env var env)]
    (.put m var val)
    (error (str "Unbound variable -- set!: " var))))

(defn lookup-variable-value
  [var env]
  (if-let [^java.util.HashMap m (lookup-env var env)]
    (let [ret (.get m var)]
      (if (= ret '*unassigned*)
        (error (str "Unassigned var -- lookup-variable-value: " var))
        ret))
    (error (str "Unbound variable: " var))))

(def primitive-implementation cadr)
(defn apply-primitive-procedure [proc args]
  (letfn [(list-of [p]
            (reverse
             (loop [p p
                    ret nil]
               (if (nil? p)
                 ret
                 (recur (cdr p)
                        (cons (car p) ret))))))]
    (apply (primitive-implementation proc) (list-of args))))

(defn eval-sequence [exps env]
  (if (nil? exps)
    (error (str "Empty exps -- eval-sequence"))
    (loop [head (car exps)
           more (cdr exps)]
      (if (nil? more)
        (_eval head env)
        (do (_eval head env)
            (recur (car more)
                   (cdr more)))))))

(def procedure-parameters cadr)
(def procedure-body caddr)
(def procedure-environment cadddr)

(defn extend-environment [vars vals base-env]
  (if (= (length vars) (length vals))
    (Env. (make-frame vars vals) base-env)
    (if (< (length vars) (length vals))
      (error (str "Too many arguments supplied: " vars vals))
      (error (str "Too few arguments supplied: " vars vals)))))

(def primitive-procedures
  [
   ['apply _apply]
   ['null? nil?]
   ['pair? pair?]
   ['car car]
   ['cdr cdr]
   ['cons my-cons]
   ['list my-list]
   ['+ +]
   ['- -]
   ['* *]
   ['/ /]
   ['= =]
   ['< <]
   ['> >]
   ['eq? =] ; todo:
   ['false? false?]
   ['true? true?]
   ['symbol? symbol?]
   ['number? number?]
   ['string? string?]
   ['print user-print]
   ['str user-str]
   ['error error]
   ])

(defn primitive-procedure-names []
  (apply my-list (map first primitive-procedures)))

(defn primitive-procedure-objects []
  (apply my-list (map (fn [p] (my-list 'primitive (second p)))
                      primitive-procedures)))

(defn make-emtpy-environment [] nil)

(defn setup-environment []
  (let [initial-env (extend-environment (primitive-procedure-names)
                                        (primitive-procedure-objects)
                                        (make-emtpy-environment))]
    (define-variable! _true true initial-env)
    (define-variable! _false false initial-env)
    initial-env))

(defn _apply [proc args]
  (cond
    (primitive? proc) (apply-primitive-procedure proc args)
    (procedure? proc) (eval-sequence
                       (procedure-body proc)
                       (extend-environment
                        (procedure-parameters proc)
                        args
                        (procedure-environment proc)))
    :else (error (str "Unknown procedure type -- _apply: " (type proc) " :: " proc))))

(defn _eval
  [exp env]
  ;(user-print (my-list exp "\n"))
  ;(print-env env)
  (cond
    (= exp '%print-env) (do (print-env env))
    (self-evaluating? exp) exp
    (variable? exp) (lookup-variable-value exp env)
    (pair? exp)
    (case (car exp)
      quote (cadr exp)
      set! (do
             (set-variable-value!
              (cadr exp)
              (_eval (caddr exp) env)
              env))
      define (define-variable!
               (definition-variable exp)
               (_eval (definition-value exp) env)
               env)
      if (if (my-true? (_eval (if-predicate exp) env))
           (recur (if-consequent exp) env)
           (recur (if-alternative exp) env))
      lambda (make-procedure (cadr exp)
                             (cddr exp)
                             env)
      begin (eval-sequence (cdr exp) env)
      cond (recur (cond->if exp) env)
      let (recur (let->combination exp) env)
      let* (recur (let*->nested-lets exp) env)
      letrec (recur (expand-letrec exp) env)
      and (if-let [args (cdr exp)]
            (loop [head (car args)
                   more (cdr args)]
              (let [v (_eval head env)]
                (if (nil? more)
                  (if (my-true? v)
                    v
                    false)
                  (if (my-true? v)
                    (recur (car more)
                           (cdr more))
                    false))))
            true)
      or (if-let [args (cdr exp)]
           (loop [head (car args)
                  more (cdr args)]
             (let [v (_eval head env)]
               (if (nil? more)
                 (if (my-true? v)
                   v
                   false)
                 (if (my-true? v)
                   v
                   (recur (car more)
                          (cdr more))))))
           false)
      (_apply (_eval (car exp) env)
              (my-map #(_eval % env) (cdr exp))))
    :else (error (str "Unknown expression type -- _eval: " (type exp) " :: " exp))))

(def input-prompt ";;; M-Eval input:")
(def output-prompt ";;; M-Eval value:")

(def prompt-for-input println)
(def announce-output println)

(defn driver-loop []
  (let [env (setup-environment)]
    (loop []
      (prompt-for-input input-prompt)
      (let [input (scheme-of (read))]
        (println (str "input: " (type input) " :: " input))
        (if (= input 'quit)
          :done
          (let [output (_eval input env)]
            (announce-output output-prompt)
            (println (str "output: " (type output)))
            (user-print (my-list output "\n"))
            (recur)))))))

(deftest test-_eval
  (are [exp val] (= (_eval (scheme-of exp) (setup-environment)) val)
    1 1
    "str" "str"
    '(quote (a b)) (my-list 'a 'b)
    '(define f 1) 'f
    '(define (f x) x) 'f
    '(begin 1 2) 2
    '(begin (define (f x) x) (f 8)) 8
    '(begin (define (f x y) (+ x y)) (f 8 9)) 17
    '(if true 1 2) 1
    '(if false 1 2) 2
    '(if (null? ()) 1 2) 1
    '(if (null? true) 1 2) 2
    '(or) false
    '(or false 1 2) 1
    '(and) true
    '(and false 1 2) false
    '(and 1 2) 2
    '(and 1 2 false) false
    '(begin (define (append x y)
              (if (null? x)
                y
                (cons (car x)
                      (append (cdr x)
                              y))))
            (append (list 1 2 3) (list 4 5 6)))
    (my-list 1 2 3 4 5 6)
    '(begin (define (factorial n)
              (define (impl k ret)
                (if (= k 1)
                  ret
                  (impl (- k 1) (* k ret))))
              (impl n 1))
            (factorial 5))
    120
    '(letrec ((fact
               (lambda (n)
                       (if (= n 1)
                         1
                         (* n (fact (- n 1)))))))
             (fact 10))
    3628800
    '((lambda (n) ; Q. 4.21-a
              ((lambda (fib)
                       (fib fib n))
               (lambda (fb n)
                       (if (< n 2)
                         1
                         (+ (fb fb (- n 1))
                            (fb fb (- n 2)))))))
      6)
    13
    '(cond (false 0) (1)) 1
    '(car '(1 2 3)) 1
    ))
