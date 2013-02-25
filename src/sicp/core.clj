(ns sicp.core
  (:require [clojure.test :refer [is are run-tests]])
  (:require [clojure.pprint]))

(defn p
  "Pretty print and return a value"
  [x]
  (clojure.pprint/pprint x)
  x)

(defn twice [x]
  (+ x x))

(defn square [x]
  (* x x))

(defn cube [x]
  (* x (square x)))

(defn half [x]
  (/ x 2))

(defn third-root
  [x]
  (letfn
      [(improved-guess
         [guess]
         (/ (+ (/ x (square guess)) (twice guess)) 3))
       (enough-precision?
         [guess]
         (< (Math/abs (- x (cube guess))) 0.0001))]
    (loop [x x guess 1.0]
      (if (enough-precision? guess)
        guess
        (recur x (improved-guess guess))))))

(defn fast-expt2
  "Q 1.16"
  {:test #(do (are [b n result] (= (fast-expt2 b n) result)
                   0 0 1
                   0 1 0
                   1 0 1
                   3 4 81)
              (is (thrown? java.lang.AssertionError (fast-expt2 3 -1))))}

  [b n]
  {:pre [(>= n 0)]}

  (loop [b b n n a 1]
    (cond
     (zero? n) a
     (odd? n) (* a b (fast-expt2 b (dec n)))
     :else (recur (square b) (half n) a))))

(defn *-op
  {:test #(do (are [a b result] (= (*-op a b) result)
                   0 0 0
                   1 0 0
                   0 1 0
                   1 1 1
                   1 2 2
                   2 1 2
                   3 4 12
                   -3 4 -12)
              (is (thrown? java.lang.AssertionError (*-op 1 -1))))}

  [a b]
  {:pre [(>= b 0)]}
  (if (= b 0)
    0
    (+ a (*-op a (dec b)))))

(run-tests)
