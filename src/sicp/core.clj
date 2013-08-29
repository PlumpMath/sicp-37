(ns sicp.core
  (:require [clojure.test :refer [is are run-tests]])
  (:require [clojure.pprint])
  (:require [clojure.math.numeric-tower])
  (:require [clojure.repl]))

(defn p_
  "Pretty print and return a value"
  [x]
  (clojure.pprint/pprint x)
  x)

(defn twice [x]
  (+' x x))

(defn square [x]
  (*' x x))

(defn cube [x]
  (*' x (square x)))

(defn half [x]
  (/ x 2))

(defn third-root
  [x]
  (letfn
      [(improved-guess
         [guess]
         (/ (+' (/ x (square guess)) (twice guess)) 3))
       (enough-precision?
         [guess]
         (< (Math/abs (- x (cube guess))) 0.0001))]
    (loop [x x guess 1.0]
      (if (enough-precision? guess)
        guess
        (recur x (improved-guess guess))))))

(defn my-expt
  "Q 1.16"
  {:test #(do (are [b n result] (= (my-expt b n) result)
                   0 0 1
                   0 1 0
                   1 0 1
                   3 4 81)
              (is (thrown? java.lang.AssertionError (my-expt 3 -1))))}

  [b n]
  {:pre [(>= n 0)]}

  (loop [b b n n a 1]
    (cond
     (zero? n) a
     (odd? n) (*' a b (my-expt b (dec n)))
     :else (recur (square b) (half n) a))))

(defn my-*
  "Q. 1.17"
  {:test #(do (are [a b result] (= (my-* a b) result)
                   0 0 0
                   1 0 0
                   0 1 0
                   1 1 1
                   1 2 2
                   2 1 2
                   3 4 12
                   -3 4 -12)
              (is (thrown? java.lang.AssertionError (my-* 1 -1))))}

  [a b]
  {:pre [(>= b 0)]}

  (cond
   (zero? b) 0
   (odd? b) (+' a (my-* a (dec b)))
   :else (recur (twice a) (half b))))


(defn my-expt-with-my-*
  "Q 1.18"
  ([b n]
     {:pre [(>= n 0)]}

     (letfn
         [(square-with-my-* [n]
            (my-* n n))]

       (cond
        (zero? n) 1
        (odd? n) (my-* b (my-expt-with-my-* b (dec n)))
        :else (recur (square-with-my-* b) (half n)))))

  {:test #(do (are [b n result] (= (my-expt-with-my-* b n) result)
                   0 0 1
                   0 1 0
                   1 0 1
                   3 4 81)
              (is (thrown? java.lang.AssertionError (my-expt-with-my-* 3 -1))))})

(defn fib
  "Q 1.19"
  {:test #(do (are [n result] (= (fib n) result)
                   1 1
                   2 1
                   3 2
                   4 3
                   5 5
                   6 8
                   7 13
                   8 21
                   9 34
                   10 55)
              (is (thrown? java.lang.AssertionError (fib 0))))}

  [n]
  {:pre [(>= n 1)]}

  (letfn [(fib-iter
            [a b p q n]
            {:pre [(>= n 0)]}

            (loop [a a b b p p q q n n]
              (cond
               (= n 0) b
               (odd? n) (fib-iter (+' (*' b q) (*' a (+' p q)))
                                  (+' (*' b p) (*' a q))
                                  p
                                  q
                                  (dec n))
               :else (recur a
                            b
                            (+' (*' p p) (*' q q))
                            (+' (*' 2 p q) (*' q q))
                            (/ n 2)))))]
    (fib-iter 1 0 0 1 n)))

(defn gcd
  {:test #(do (are [m n result] (= (gcd m n) result)
                   45 15 15
                   3 8 1
                   46 22 2))}

  [m n]

  (let [[n m] (sort [m n])]
    (if (= n 0)
      m
      (recur n (rem m n)))))


(defn smallest-divisor
  {:test #(do (are [n result] (= (smallest-divisor n) result)
                    8 2
                    23 23))}

  [n]
  {:pre [(>= n 1)]}

  (letfn [(divides? [divisor n]
            {:pre [(>= n divisor)]}
            (zero? (rem n divisor)))

          (find-divisor [n test-divisor]
            (cond
             (> (square test-divisor) n) n
             (divides? test-divisor n) test-divisor
             :else (find-divisor n (inc test-divisor))))]

    (find-divisor n 2)))

(defn prime?
  {:test #(do (are [n] (prime? n)
                   2
                   3
                   5
                   7
                   11
                   13))}

  [n]
  {:pre [(>= n 2)]}

  (= n (smallest-divisor n)))

(defn sum-integers [a b]
  (if (> a b)
    0
    (+ a (sum-integers (inc a) b))))

(defn sum-cubes [a b]
  (if (> a b)
    0
    (+ (cube a) (sum-cubes (inc a) b))))

(defn pi-sum [a b]
  (if (> a b)
    0
    (+ (/ 1.0 (* a (+ a 2))) (pi-sum (+ a 4) b))))

(defn sum'
  {:test #(do (is (= 55 (sum' identity 1 inc 10))))}
  [term a next b]
  (loop [term term
         a a
         next next
         b b
         ret 0]
    (if (> a b)
      ret
      (recur term (next a) next b (+ ret (term a))))))

(defn sum-cubes' [a b]
  (sum' cube a inc b))

(defn sum-integers' [a b]
  (sum' identity a inc b))

(defn pi-sum' [a b]
  (letfn [(pi-term [x]
            (/ 1.0 (* x (+ x 2))))
          (pi-next [x]
            (+ x 4))]
    (sum' pi-term a pi-next b)))

(defn product'
  {:test #(do (is (= 3628800 (product' identity 1 inc 10))))}
  [term a next b]
  (loop [term term
         a a
         next next
         b b
         ret 1]
    (if (> a b)
      ret
      (recur term (next a) next b (* ret (term a))))))

(defn accumulate'
  {:test #(do (is (= 3628800 (accumulate' * 1 identity 1 inc 10)))
              (is (= 55 (accumulate' + 0 identity 1 inc 10))))}
  [combiner null-value term a next b]
  (loop [combiner combiner
         null-value null-value
         term term
         a a
         next next
         b b
         ret null-value]
    (if (> a b)
      ret
      (recur combiner null-value term (next a) next b (combiner ret (term a))))))

(defn filtered-accumulate'
  {:test #(do (is (= 3628800 (filtered-accumulate' pos? * 1 identity -1 inc 10)))
              (is (= 55 (filtered-accumulate' pos? + 0 identity -1 inc 10))))}
  [cond? combiner null-value term a next b]
  (loop [cond? cond?
         combiner combiner
         null-value null-value
         term term
         a a
         next next
         b b
         ret null-value]
    (if (> a b)
      ret
      (if (cond? a)
        (recur cond? combiner null-value term (next a) next b (combiner ret (term a)))
        (combiner ret (filtered-accumulate' cond? combiner null-value term (next a) next b))))))

(defn square-sum-of-primes
  {:test #(do (is (= (+ 4 9 25) (square-sum-of-primes 2 5))))}
  [a b]
  (filtered-accumulate' prime? + 0 square a inc b))

(defn product-of-coprimes
  {:test #(do (is (= (* 1 3 5 7) (product-of-coprimes 1 8))))}
  [a b]
  (letfn [(pos-and-coprime?
            [i]
            (and (pos? i) (= 1 (gcd i b))))]
    (filtered-accumulate' pos-and-coprime? * 1 identity a inc b)))

(defn close-enough? [x y]
  (< (Math/abs (- x y)) 0.001))

(defn average [x y]
  (/ (+ x y) 2))

(defn search [f neg-point pos-point]
  (let [mid-point (average neg-point pos-point)]
    (if (close-enough? neg-point pos-point)
      mid-point
      (let [test-value (f mid-point)]
        (cond
         (pos? test-value) (search f neg-point mid-point)
         (neg? test-value) (search f mid-point pos-point)
         :else mid-point)))))

(defn half-interval-method
  {:test #(do (is (close-enough?
                   (half-interval-method (fn [x] (- (square x) 2)) 1.0 5.0)
                   (Math/sqrt 2))))}
  [f a b]
  (let [a-value (f a)
        b-value (f b)]
    (cond
     (zero? a-value) a
     (zero? b-value) b
     (and (neg? a-value) (pos? b-value)) (search f a b)
     (and (neg? b-value) (pos? a-value)) (search f b a)
     :else (throw (Exception. (str "Values are not of opposite sign: " a " " b))))))

(def tolerance 0.00001)
(defn fixed-point [f first-guess]
  (letfn [(close-enough? [v1 v2]
            (< (Math/abs (- v1 v2)) tolerance))
          (try_ [guess]
            (let [next (f guess)]
              (if (close-enough? guess next)
                next
                (try_ next))))]
    (try_ first-guess)))

(defn sqrt' [x]
  (fixed-point #(average % (/ x %)) 1.0))

(def golden-ratio (fixed-point #(average % (+ 1 (/ 1 %))) 1.0))

(run-tests)
