(ns sicp.core
  (:require [clojure.test :refer [is are]]
            [clojure.pprint]
            [clojure.math.numeric-tower]
            [clojure.repl]
            [clojure.core.typed :refer [ann-form ann Int Num letfn> loop> fn> doseq> Vec Coll NonEmptySeq NonEmptyColl Option Seqable NonEmptySeqable EmptySeqable NonEmptyLazySeq] :as typed])
  (:import (clojure.lang ASeq LazySeq))
  (:gen-class))
(set! *warn-on-reflection* false)

(ann ^:no-check clojure.pprint/pprint [Any -> nil])
(typed/override-method clojure.lang.Numbers/remainder (Fn [Int Int -> Int]
                                                          [Num Num -> Num]))
(typed/override-method clojure.lang.Numbers/addP (Fn [Int Int -> Int]
                                                     [Num Num -> Num]))
(typed/override-method clojure.lang.Numbers/minusP (Fn [Int * -> Int]
                                                       [Num * -> Num]))
(typed/override-method clojure.lang.Numbers/multiplyP (Fn [Int Int -> Int]
                                                          [Num Num -> Num]))
(typed/override-method clojure.lang.Numbers/incP (Fn [Int -> Int]
                                                     [Num -> Num]))
(typed/override-method clojure.lang.Numbers/decP (Fn [Int -> Int]
                                                     [Num -> Num]))
(ann ^:no-check clojure.core/rem (Fn [Int Int -> Int]
                                     [Num Num -> Num]))
(ann ^:no-check clojure.core/mod (Fn [Int Int -> Int]
                                     [Num Num -> Num]))
; `(All [[a :< Num]] [a -> a])` is not true since `(type (-' Long/MIN_VALUE))` is BigInt.

(ann ^:no-check clojure.test/run-tests [(U clojure.lang.Namespace clojure.lang.Symbol) *
                                        -> '{:type clojure.lang.Keyword
                                             :pass Int
                                             :test Int
                                             :error Int
                                             :fail Int}])
(ann ^:no-check clojure.math.numeric-tower/ceil [Num -> Num])

(ann p_ (All [a] [a -> a]))
(defn p_
  "Pretty print and return a value"
  [x]
  (clojure.pprint/pprint x)
  x)

(ann twice (Fn [Int -> Int]
               [Num -> Num]))
(defn twice [x]
  (+' x x))

(ann square (Fn [Int -> Int]
                [Num -> Num]))
(defn square [x]
  (*' x x))

(ann cube (Fn [Int -> Int]
              [Num -> Num]))
(defn cube [x]
  (*' x (square x)))

(ann half [Num -> Num])
(defn half [x]
  (/ x 2))

(ann abs (Fn [Int -> Int]
             [Num -> Num]))
(defn abs [x]
  (if (neg? x)
    (* -1 x)
    x))

(ann third-root [Num -> Num])
(defn third-root
  [x]
  (letfn>
    [improved-guess :- [Num -> Num]
     (improved-guess [guess]
                     (/ (+' (/ x (square guess)) (twice guess)) 3))
     enough-precision? :- [Num -> Boolean]
     (enough-precision? [guess]
                        (< (abs (-' x (cube guess))) 0.0001))]
    (loop> [x :- Num x
            guess :- Num 1.0]
      (if (enough-precision? guess)
        guess
        (recur x (improved-guess guess))))))

(ann my-expt (Fn [Int Int -> Int]
                 [Int Int Int -> Int]
                 [Num Int -> Num]
                 [Num Int Num -> Num]))
(defn my-expt
  "Q 1.16"
  {:test #(do (are [b n result] (= (my-expt b n) result)
                   0 0 1
                   0 1 0
                   1 0 1
                   3 4 81)
              (is (thrown? java.lang.AssertionError (my-expt 3 -1))))}

  ([b n] (my-expt b n 1))
  ([b n ret]
     {:pre [(>= n 0)]}
     (cond
      (zero? n) ret
      (odd? n) (*' ret b (my-expt b (dec' n)))
      :else (recur (square b) (half n) ret))))

(ann my-* (Fn [Int Int -> Int]
              [Num Int -> Num]))
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
   (odd? b) (+' a (my-* a (dec' b)))
   :else (recur (twice a) (half b))))


(ann my-expt-with-my-* [Int Int -> Int])
(defn my-expt-with-my-*
  "Q 1.18"
  ([b n]
     {:pre [(>= n 0)]}

     (letfn> [square-with-my-* :- [Int -> Int]
              (square-with-my-* [n]
                                (my-* n n))]

       (cond
        (zero? n) 1
        (odd? n) (my-* b (my-expt-with-my-* b (dec' n)))
        :else (recur (square-with-my-* b) (half n)))))

  {:test #(do (are [b n result] (= (my-expt-with-my-* b n) result)
                   0 0 1
                   0 1 0
                   1 0 1
                   3 4 81)
              (is (thrown? java.lang.AssertionError (my-expt-with-my-* 3 -1))))})

(ann fib [Int -> Int])
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
              (is (thrown? java.lang.AssertionError (fib -1))))}

  [n]
  {:pre [(>= n 0)]}

  (letfn> [fib-iter :- [Int
                        Int
                        Int
                        Int
                        Int
                        ->
                        Int]
           (fib-iter [a b p q n]
                     {:pre [(>= n 0)]}

                     (loop> [a :- Int a
                             b :- Int b
                             p :- Int p
                             q :- Int q
                             n :- Int n]
                       (cond
                        (= n 0) b
                        (odd? n) (recur (+' (*' b q) (*' a (+' p q)))
                                        (+' (*' b p) (*' a q))
                                        p
                                        q
                                        (dec' n))
                        :else (recur a
                                     b
                                     (+' (*' p p) (*' q q))
                                     (+' (*' 2 p q) (*' q q))
                                     (long (/ n 2))))))]
    (fib-iter 1 0 0 1 n)))

(ann gcd [Int Int -> Int])
(defn gcd
  {:test #(do (are [m n result] (= (gcd m n) result)
                   45 15 15
                   3 8 1
                   46 22 2
                   -46 22 2
                   46 -22 2
                   -46 -22 2))}

  [m n]
  (let [abs-m (abs m)
        abs-n (abs n)
        small (if (< abs-m abs-n) abs-m abs-n)
        large (if (> abs-m abs-n) abs-m abs-n)]
    (if (= small 0)
      large
      (recur (rem large small) small))))

(ann smallest-divisor [Int -> Int])
(defn smallest-divisor
  {:test #(do (are [n result] (= (smallest-divisor n) result)
                   8 2
                   23 23))}

  [n]
  {:pre [(>= n 1)]}

  (letfn> [divides? :- [Int Int -> Boolean]
           (divides? [divisor n]
                     {:pre [(>= n divisor)]}
                     (zero? (rem n divisor)))

           find-divisor :- [Int Int -> Int]
           (find-divisor [n test-divisor]
                         (cond
                          (> (square test-divisor) n) n
                          (divides? test-divisor n) test-divisor
                          :else (find-divisor n (inc' test-divisor))))]

    (find-divisor n 2)))

(ann prime? [Int -> Boolean])
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

(ann sum-integers [Int Int -> Int])
(defn sum-integers [a b]
  (if (> a b)
    0
    (+' a (sum-integers (inc' a) b))))

(ann sum-cubes [Int Int -> Int])
(defn sum-cubes [a b]
  (if (> a b)
    0
    (+' (cube a) (sum-cubes (inc' a) b))))

(ann pi-sum [Int Int -> Num])
(defn pi-sum [a b]
  (if (> a b)
    0
    (+' (/ 1.0 (*' a (+' a 2))) (pi-sum (+' a 4) b))))

(ann sum' (Fn [[Int -> Num]
               Int
               [Int -> Int]
               Int
               ->
               Num]
              [[Int -> Num]
               Int
               [Int -> Int]
               Int
               Num
               ->
               Num]))
(defn sum'
  {:test #(do (is (= 55 (sum' identity 1 inc' 10))))}
  ([term a next b] (sum' term a next b 0))
  ([term a next b ret]
     (if (> a b)
       ret
       (recur term (next a) next b (+' ret (term a))))))

(ann sum-cubes' [Int Int -> Num])
(defn sum-cubes' [a b]
  (sum' cube a inc' b))

(ann num-identity (All [[a :< Num]]
                       (Fn [a -> a]
                           [Int -> Int]
                           [Num -> Num])))
(defn num-identity [x]
  x)

(ann sum-integers' [Int Int -> Num])
(defn sum-integers' [a b]
  (sum' identity a inc' b))

(ann pi-sum' [Int Int -> Num])
(defn pi-sum' [a b]
  (letfn> [pi-term :- [Int -> Num]
           (pi-term [x]
                    (/ 1.0 (*' x (+' x 2))))
           pi-next :- [Int -> Int]
           (pi-next [x]
                    (+' x 4))]
    (sum' pi-term a pi-next b)))

(ann product' (Fn [[Num -> Num]
                   Num
                   [Num -> Num]
                   Num
                   ->
                   Num]
                  [[Num -> Num]
                   Num
                   [Num -> Num]
                   Num
                   Num
                   ->
                   Num]))
(defn product'
  {:test #(do (is (= 3628800 (product' identity 1 inc' 10))))}
  ([term a next b] (product' term a next b 1))
  ([term a next b ret]
     (if (> a b)
       ret
       (recur term (next a) next b (*' ret (term a))))))


(ann accumulate' (All [a] [[a * -> a]
                           a
                           [Int -> a]
                           Int
                           [Int -> Int]
                           Int
                           ->
                           a]))
(defn accumulate'
  {:test #(do (is (= 3628800 (accumulate' *' 1 identity 1 inc' 10)))
              (is (= 55 (accumulate' +' 0 identity 1 inc' 10))))}
  [combiner null-value term a next b]
  (if (> a b)
    null-value
    (recur combiner (combiner null-value (term a)) term (next a) next b)))

(ann filtered-accumulate' (All [a] [[Int -> Boolean]
                                    [a a -> a]
                                    a
                                    [Int -> a]
                                    Int
                                    [Int -> Int]
                                    Int
                                    ->
                                    a]))
(defn filtered-accumulate'
  {:test #(do (is (= 3628800 (filtered-accumulate' pos? *' 1 identity -1 inc' 10)))
              (is (= 55 (filtered-accumulate' pos? +' 0 identity -1 inc' 10))))}
  [pred combiner null-value term a next b]
  (if (> a b)
    null-value
    (if (pred a)
      (recur pred combiner (combiner null-value (term a)) term (next a) next b)
      (recur pred combiner null-value term (next a) next b))))

(ann square-sum-of-primes [Int Int -> Int])
(defn square-sum-of-primes
  {:test #(do (is (= (+' 4 9 25) (square-sum-of-primes 2 5))))}
  [a b]
  (filtered-accumulate' prime? +' 0 square a inc' b))

(ann product-of-coprimes [Int Int -> Int])
(defn product-of-coprimes
  {:test #(do (is (= (*' 1 3 5 7) (product-of-coprimes 1 8))))}
  [a b]
  (letfn> [pos-and-coprime? :- [Int -> Boolean]
           (pos-and-coprime?
            [i]
            (and (pos? i) (= 1 (gcd i b))))]
    (filtered-accumulate' pos-and-coprime? *' 1 num-identity a inc' b)))

(ann close-enough? (Fn [Num Num -> Boolean]
                       [Num Num Num -> Boolean]))
(defn close-enough?
  ([x y] (close-enough? x y 0.001))
  ([x y delta]
     {:pre [(>= delta 0)]}
     (<= (abs (-' x y)) delta)))

(ann average [Num Num -> Num])
(defn average [x y]
  (/ (+' x y) 2))

(ann search [[Num -> Num]
             Num
             Num
             ->
             Num])
(defn search [f neg-point pos-point]
  (let [mid-point (average neg-point pos-point)]
    (if (close-enough? neg-point pos-point)
      mid-point
      (let [test-value (f mid-point)]
        (cond
         (pos? test-value) (search f neg-point mid-point)
         (neg? test-value) (search f mid-point pos-point)
         :else mid-point)))))

(ann half-interval-method [[Num -> Num]
                           Num
                           Num
                           ->
                           Num])
(defn half-interval-method
  {:test #(do (is (close-enough?
                   (half-interval-method (fn [x] (-' (square x) 2)) 1.0 5.0)
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

(ann tolerance double)
(def tolerance 0.00001)

(ann fixed-point [[Num -> Num] Num -> Num])
(defn fixed-point [f first-guess]
  (letfn> [try_ :- [Num -> Num]
           (try_ [guess]
                 (let [next (f guess)]
                   (if (close-enough? guess next tolerance)
                     next
                     (try_ next))))]
    (try_ first-guess)))

(ann fixed-point' [[Num -> Num] Num -> Num])
(defn fixed-point' [f first-guess]
  (letfn> [try_ :- [Num -> Num]
           (try_ [guess]
                 (let [next (f guess)]
                   (println next)
                   (if (close-enough? guess next tolerance)
                     next
                     (recur next))))]
    (try_ first-guess)))

(ann sqrt' [Num -> Num])
(defn sqrt' [x]
  {:pre [(>= x 0)]}
  (fixed-point
   (ann-form
    #(average % (/ x %))
    [Num -> Num])
   1.0))

(ann sqrt'' [Num -> Num])
(defn sqrt'' [x]
  {:pre [(>= x 0)]}
  (fixed-point'
   (ann-form
    #(average % (/ x %))
    [Num -> Num])
   1.0))

(ann golden-ratio Num)
(def golden-ratio
  (fixed-point
   (ann-form
    #(average % (+' 1 (/ 1 %)))
    [Num -> Num])
   1.0))

(ann cont-frac [[Int -> Num]
                [Int -> Num]
                Int
                ->
                Num])
(defn cont-frac
  "Q. 1.37-a"
  {:test #(do (is (= (/ 1 2)
                     (cont-frac (fn [x] x) (fn [x] x) 2))))}
  [n d k]
  {:pre [(>= k 1)]}
  (letfn> [recur_ :- [[Int -> Num]
                      [Int -> Num]
                      Int
                      Int
                      ->
                      Num]
           (recur_ [n d i k]
                   (if (< i k)
                     (/ (n i)
                        (+' (d i)
                            (recur_ n d (inc' i) k)))
                     (/ (n i)
                        (d i))))]
    (recur_ n d 1 k)))

(ann cont-frac' [[Int -> Num]
                 [Int -> Num]
                 Int
                 ->
                 Num])
(defn cont-frac'
  "Q. 1.37-b"
  {:test #(do (is (= (/ 1 2)
                     (cont-frac' (fn [x] x) (fn [x] x) 2))))}
  [n d k]
  {:pre [(>= k 1)]}
  (loop> [n :- [Int -> Num] n
          d :- [Int -> Num] d
          i :- Int k
          k :- Int k
          ret :- Num 0]
    (if (= i 0)
      ret
      (recur n d (dec' i) k (/ (n i)
                               (+' (d i)
                                   ret))))))

(ann approx-e [Int -> Num])
(defn approx-e
  "Q. 1.38"
  [k]
  {:pre [(>= k 1)]}
  (+' (cont-frac' (fn [_] 1)
                  (fn> [x :- Num]
                    (if (= (mod x 3) 2)
                      (*' 2
                          (+' (/ (-' x 2)
                                 3)
                              1))
                      1))
                  k)
      2))

(ann tan-cf [Num Int -> Num])
(defn tan-cf
  "Q. 1.39"
  [x k]
  (cont-frac' (ann-form #(if (= % 1) x (*' -1 (square x))) [Int -> Num])
              (ann-form #(-' (*' % 2) 1) [Int -> Int])
              k))

(ann average-damp [[Num -> Num] -> [Num -> Num]])
(defn average-damp [f]
  (fn [x] (average x (f x))))

(ann dx double)
(def dx 0.00001)

(ann deriv [[Num -> Num] -> [Num -> Num]])
(defn deriv [f]
  (fn [x] (/ (-' (f (+' x dx)) (f (-' x dx)))
             (twice dx))))

(ann newton-transform [[Num -> Num] -> [Num -> Num]])
(defn newton-transform [f]
  (fn [x] (-' x
              (/ (f x)
                 ((deriv f) x)))))

(ann newton-method [[Num -> Num] Num -> Num])
(defn newton-method [f guess]
  (fixed-point (newton-transform f) guess))

(ann sqrt''' [Num -> Num])
(defn sqrt''' [x]
  {:pre [(>= x 0)]}
  (newton-method (ann-form #(-' (square %) x) [Num -> Num])
                 1.0))

(ann fixed-point-of-transform [[Num -> Num]
                               [[Num -> Num] -> [Num -> Num]]
                               Num
                               ->
                               Num])
(defn fixed-point-of-transform [f transform guess]
  (fixed-point (transform f) guess))

(ann sqrt'''' [Num -> Num])
(defn sqrt'''' [x]
  {:pre [(>= x 0)]}
  (fixed-point-of-transform (ann-form #(/ x %) [Num -> Num])
                            average-damp
                            1.0))

(ann sqrt''''' [Num -> Num])
(defn sqrt''''' [x]
  {:pre [(>= x 0)]}
  (fixed-point-of-transform (ann-form #(-' (square %) x) [Num -> Num])
                            newton-transform
                            1.0))

(ann cubic [Num Num Num -> [Num -> Num]])
(defn cubic
  "Q. 1.40"
  {:test #(do (is (< (abs (-' (newton-method (cubic 2 3 -22) 1) 2)) tolerance)))}
  [a b c]
  (fn [x] (+' c (*' x (+' b (*' x (+' a (*' x (+' 1)))))))))

(ann double_ (All [a] [[a -> a] -> [a -> a]]))
(defn double_
  "Q. 1.41"
  [f]
  (fn [x] (f (f x))))

(ann compose (All [a b c] [[b -> c] [a -> b] -> [a -> c]]))
(defn compose
  "Q. 1.42"
  [f g] (fn [x] (f (g x))))

(ann repeated (All [a] (Fn [[a -> a] Int -> [a -> a]]
                           [[a -> a] Int [a -> a] -> [a -> a]])))
(defn repeated
  "Q. 1.43"
  {:test #(do (is (= 5 ((repeated inc' 5) 0))))}
  ([f n] (repeated f n identity))
  ([f n ret]
     {:pre [(>= n 0)]}
     (cond
      (zero? n) ret
      (zero? (mod n 2)) (recur (compose f f) (half n) ret)
      :else (recur f (dec' n) (fn [x] (f (ret x)))))))

(ann smooth (Fn [[Num -> Num] -> [Num -> Num]]
                [[Num -> Num] Num -> [Num -> Num]]))
(defn smooth
  "Q. 1.44-1"
  ([f] (smooth f 0.00001))
  ([f dx] (fn [x] (/ (+' (f (-' x dx))
                         (twice (f x))
                         (f (+' x dx)))
                     4))))

(ann smooth-n [[Num -> Num] Int -> [Num -> Num]])
(defn smooth-n
  "Q. 1.44-2"
  ([f n] ((repeated smooth n) f)))

(ann log2 [Num -> Num])
(defn log2 [x]
  (/ (Math/log (double x)) (Math/log 2.0)))

(ann nth-root [Num Int -> Num])
(defn nth-root
  "Q. 1.45"
  {:test #(do (is (< (-' (nth-root 32 5) 2) tolerance)))}
  [x n]
  (letfn> [damp :- [Int
                    ->
                    [[Num -> Num] -> [Num -> Num]]]
           (damp [n]
                 (repeated average-damp (dec' (bigint (clojure.math.numeric-tower/ceil (log2 n))))))
           basic :- [Num -> Num]
           (basic [guess]
                  (/ x
                     (Math/pow (double guess) (double (dec' n)))))]
    (fixed-point ((damp n) basic) 1)))

(ann iterative-improve [[Num -> Boolean] [Num -> Num] -> [Num -> Num]])
(defn iterative-improve
  "Q. 1.46-1"
  [is-good? update]
  (fn [x]
    (loop> [guess :- Num 1]
      (if (is-good? guess)
        guess
        (recur (update guess))))))

(ann sqrt'''''' [Num -> Num])
(defn sqrt''''''
  "Q. 1.46-2"
  {:test #(do (is (close-enough? (sqrt'''''' 4) 2 tolerance)))}
  [x]
  ((iterative-improve (fn> [guess :- Num] (close-enough? guess (/ x guess) tolerance))
                      (average-damp (fn> [guess :- Num] (/ x guess))))
   x))

(ann fixed-point'' [[Num -> Num] Num -> Num])
(defn fixed-point''
  "Q. 1.46-3"
  [f first-guess]
  (iterative-improve (fn> [guess :- Num] (close-enough? guess (f guess)))
                     f)
  first-guess)

(typed/def-alias Rat '[Int Int])

(ann numer [Rat -> Int])
(defn numer [x]
  (first x))

(ann denom [Rat -> Int])
(defn denom [x]
  (second x))

(ann make-rat [Int Int -> Rat])
(defn make-rat
  "Q. 2.1"
  {:test #(do (is (= (numer (make-rat -6 -10)) 3))
              (is (= (denom (make-rat -6 -10)) 5))
              (is (= (numer (make-rat 6 -10)) -3))
              (is (= (denom (make-rat 6 -10)) 5))
              (is (= (numer (make-rat -6 10)) -3))
              (is (= (denom (make-rat -6 10)) 5))
              (is (= (numer (make-rat 6 10)) 3))
              (is (= (denom (make-rat 6 10)) 5)))}
  [n d]
  (let [sig (if (neg? (*' n d)) -1 1)
        abs-n (abs n)
        abs-d (abs d)
        g (gcd abs-n abs-d)]
    [(bigint (*' sig (/ abs-n g)))
     (bigint (/ abs-d g))]))

(ann print-rat [Rat -> nil])
(defn print-rat [x]
  (println (numer x) "/" (denom x)))

(ann add-rat [Rat Rat -> Rat])
(defn add-rat [x y]
  (make-rat (+' (*' (numer x) (denom y))
                (*' (numer y) (denom x)))
            (*' (denom x) (denom y))))

(ann neg-rat [Rat -> Rat])
(defn neg-rat [x]
  (make-rat (*' -1 (numer x))
            (denom x)))

(ann sub-rat [Rat Rat -> Rat])
(defn sub-rat [x y]
  (add-rat x (neg-rat y)))

(ann mul-rat [Rat Rat -> Rat])
(defn mul-rat [x y]
  (make-rat (*' (numer x) (numer y))
            (*' (denom x) (denom y))))

(ann inv-rat [Rat -> Rat])
(defn inv-rat [x]
  (make-rat (denom x)
            (numer x)))

(ann div-rat [Rat Rat -> Rat])
(defn div-rat [x y]
  (mul-rat x
           (inv-rat y)))
(ann equal-rat? [Rat Rat -> Boolean])
(defn equal-rat? [x y]
  (= (*' (numer x) (denom y))
     (*' (denom x) (numer y))))

(typed/def-alias Point '[Num Num])

(ann x-point [Point -> Num])
(defn x-point [p]
  (first p))

(ann y-point [Point -> Num])
(defn y-point [p]
  (second p))

(ann print-point [Point -> nil])
(defn print-point [p]
  (println "(" (x-point p) ", " (y-point p) ")"))

(ann make-point [Num Num -> Point])
(defn make-point [x y]
  [x y])

(typed/def-alias Line '[Point Point])

(ann start-line [Line -> Point])
(defn start-line [l]
  (first l))

(ann end-line [Line -> Point])
(defn end-line [l]
  (second l))

(ann make-line [Point Point -> Line])
(defn make-line [p q]
  [p q])

(ann midpoint-line [Line -> Point])
(defn midpoint-line
  "Q. 2.2"
  [l]
  (make-point (average (x-point (start-line l))
                       (x-point (end-line l)))
              (average (y-point (start-line l))
                       (y-point (end-line l)))))

(typed/def-alias Rectangle '{:origin Point
                             :width Num
                             :height Num
                             :angle Num})

(ann width-rectangle [Rectangle -> Num])
(defn width-rectangle [r]
  (:width r))

(ann height-rectangle [Rectangle -> Num])
(defn height-rectangle [r]
  (:height r))

(ann perimeter-rectangle [Rectangle -> Num])
(defn perimeter-rectangle [r]
  (*' 2 (+' (width-rectangle r) (height-rectangle r))))

(ann area-rectangle [Rectangle -> Num])
(defn area-rectangle [r]
  (*' (width-rectangle r) (height-rectangle r)))

(typed/def-alias Rectangle' '{:origin Point
                              :diag Point
                              :angle Num})

(ann width-rectangle' [Rectangle' -> Num])
(defn width-rectangle' [r]
  (x-point (:diag r)))

(ann height-rectangle' [Rectangle' -> Num])
(defn height-rectangle' [r]
  (y-point (:diag r)))

(ann perimeter-rectangle' [Rectangle' -> Num])
(defn perimeter-rectangle' [r]
  (*' 2 (+' (width-rectangle' r) (height-rectangle' r))))

(ann area-rectangle' [Rectangle' -> Num])
(defn area-rectangle' [r]
  (*' (width-rectangle' r) (height-rectangle' r)))

(ann cons_ (All [a b]
                (Fn [a b -> [[a b -> a] -> a]]
                    [a b -> [[a b -> b] -> b]])))
(defn cons_ [x y]
  (fn [m] (m x y)))

(ann car (All [a b]
              [[[a b -> a] -> a] -> a]))
(defn car [z]
  (z (fn> [p :- a
           _ :- b] p)))

(ann cdr (All [a b]
              [[[a b -> b] -> b] -> b]))
(defn cdr [z]
  (z (fn> [_ :- a
           q :- b] q)))

(ann n-div [Int Int -> Int])
(defn n-div
  {:test #(do (is (= (n-div -12 2) 2))
              (is (= (n-div 8 -2) 3)))}
  [n div]
  (loop> [n :- Int (abs n)
          div :- Int (abs div)
          ret :- Int 0]
    (cond
     (zero? n) ret
     (zero? (rem n div)) (recur (bigint (/ n div)) div (inc' ret))
     :else ret)))

(ann car-n [Int -> Int])
(defn car-n [n]
  "Q. 2.5"
  (n-div n 2))

(ann cdr-n [Int -> Int])
(defn cdr-n [n]
  "Q. 2.5"
  (n-div n 3))

(ann cons-n [Int Int -> Int])
(defn cons-n
  "Q. 2.5"
  {:test #(do (is (= (car-n (cons-n 4 5)) 4))
              (is (= (cdr-n (cons-n 4 5)) 5)))}
  [m n]
  {:pre [(>= m 0)
         (>= n 0)]}
  (*' (my-expt 2 m)
      (my-expt 3 n)))

(typed/def-alias Church (All [a] [[a -> a] -> [a -> a]]))

(ann add-1 [Church -> Church])
(defn add-1 [n]
  (fn [f] (fn [x] (f ((n f) x)))))

(ann add-church [Church Church -> Church])
(defn add-church
  "Q. 2.6"
  [m n]
  (fn [f] (m f) (n f)))

(ann zero Church)
(def zero (fn [f] (ann-form (fn [x] x)
                            (All [a] [a -> a]))))

(ann one Church)
(def one (fn [f] (fn [x] (f x))))

(ann two Church)
(def two (fn [f] (fn [x] (f (f x)))))

(ann three Church)
(def three (fn [f] (fn [x] (f (f (f x))))))

(ann four Church)
(def four (add-church three one))

(typed/def-alias Interval '[Num Num])

(ann lower-bound [Interval -> Num])
(defn lower-bound
  "Q. 2.7"
  [i]
  (first i))

(ann upper-bound [Interval -> Num])
(defn upper-bound
  "Q. 2.7"
  [i]
  (second i))

(ann make-interval [Num Num -> Interval])
(defn make-interval [l u]
  (let [l_ (min l u)
        u_ (max l u)]
    [l_ u_]))

(ann make-center-width [Num Num -> Interval])
(defn make-center-width [c w]
  {:pre [(>= w 0)]}
  (make-interval (-' c w) (+' c w)))

(ann center [Interval -> Num])
(defn center [i]
  (average (lower-bound i) (upper-bound i)))

(ann width [Interval -> Num])
(defn width [i]
  (half (-' (upper-bound i) (lower-bound i))))

(ann equal-interval [Interval Interval -> Boolean])
(defn equal-interval [x y]
  (and (= (lower-bound x) (lower-bound y))
       (= (upper-bound x) (upper-bound y))))

(ann add-interval [Interval Interval -> Interval])
(defn add-interval [x y]
  (make-interval (+' (lower-bound x) (lower-bound y))
                 (+' (upper-bound x) (upper-bound y))))

(ann mul-interval [Interval Interval -> Interval])
(defn mul-interval [x y]
  (let [p1 (*' (lower-bound x) (lower-bound y))
        p2 (*' (lower-bound x) (upper-bound y))
        p3 (*' (upper-bound x) (lower-bound y))
        p4 (*' (upper-bound x) (upper-bound y))]
    (make-interval (min p1 p2 p3 p4)
                   (max p1 p2 p3 p4))))

(ann mul-interval' [Interval Interval -> Interval])
(defn mul-interval'
  "Q. 2.11"
  {:test #(do (are [lx ux ly uy lz uz]
                (equal-interval (mul-interval (make-interval lx ux)
                                              (make-interval ly uy))
                                (make-interval lz uz))
                1 2, 3 4, 3 8
                -1 2, 3 4, -4 8
                -2 -1, 3 4, -8 -3
                1 2, -3 4, -6 8
                -1 2, -3 4, -6 8
                -2 -1, -3 4, -8 6
                1 2, -4 -3, -8 -3
                -1 2, -4 -3, -8 4
                -2 -1, -4 -3, 3 8))}
  [x y]
  (let [lx (lower-bound x)
        ux (upper-bound x)
        ly (lower-bound y)
        uy (upper-bound y)]
    (cond
     (and (>= lx 0) (>= ly 0)) (make-interval (*' lx ly) (*' ux uy))
     (and (< ux 0) (< uy 0)) (make-interval (*' ux uy) (*' lx ly))
     (and (< ux 0) (>= ly 0)) (make-interval (*' lx uy) (max (*' lx ux) (*' ly uy)))
     (and (>= lx 0) (< uy 0)) (make-interval (*' ux ly) (max (*' lx ux) (*' ly uy)))
     (>= ly 0) (make-interval (*' lx uy) (*' ux uy))
     (< uy 0) (make-interval (*' ux ly) (*' lx ly))
     (>= lx 0) (make-interval (*' ux ly) (*' ux uy))
     (< ux 0) (make-interval (*' lx uy) (*' lx ly))
     :else (make-interval (min (*' lx uy) (*' ux ly)) (max (*' lx ly) (*' ux uy))))))

(ann inv-interval [Interval -> Interval])
(defn inv-interval [x]
  {:pre [(or (< (upper-bound x) 0)
             (> (lower-bound x) 0))]}
  (make-interval (/ 1 (upper-bound x))
                 (/ 1 (lower-bound x))))

(ann div-interval [Interval Interval -> Interval])
(defn div-interval [x y]
  (mul-interval x (inv-interval y)))

(ann neg-interval [Interval -> Interval])
(defn neg-interval [x]
  (make-interval (-' (upper-bound x))
                 (-' (lower-bound x))))

(ann sub-interval [Interval Interval -> Interval])
(defn sub-interval [x y]
  (add-interval x (neg-interval y)))

(ann make-center-parcent [Num Num -> Interval])
(defn make-center-parcent
  "Q. 2.12"
  [c p]
  (make-center-width c (*' (/ p 100) c)))

(ann parcent [Interval -> Num])
(defn parcent
  "Q. 2.12"
  {:test #(do (is (= (parcent (mul-interval (make-center-parcent 100 1)
                                            (make-center-parcent 200 1)))
                     (*' 100
                         (/ (half (-' (*' 202 101) (*' 198 99)))
                            (half (+' (*' 202 101) (*' 198 99))))))))}
  [i]
  (*' 100 (abs (/ (width i) (center i)))))

(ann approx-relative-error-of-mul [Num Num -> Num])
(defn approx-relative-error-of-mul
  "Q. 2.13"
  [re1 re2]
  (+' re1 re2))

"
# Q. 2.14

Assume relative errors, $e_{1}$ and $e_{2}$, are small.
Relative error of `par1` is $(e_{1} + e_{2}) + (e_{1} + e_{2}) = 2(e_{1} + e_{2})$.
Relative error of `par2` is $0 + (0 + e_{1}) + (0 + e_{2}) = e_{1} + e_{2}$.
"

"
# Q. 2.15

Each appearance of a variable `x` in a formula is mistakenly considered to be a new independent error.
"

"
# Q. 2.16

Skip...

```
(typed/def-alias Interval (HMap :mandatory {:l Num :u Num :id Int}))
(typed/def-alias IntervalOp [(U Interval IntervalOpRet)
                             (U Interval IntervalOpRet)
                             -> IntervalOpRet])
```
"

(ann last-pair (All [a] [(NonEmptySeqable a) -> (NonEmptySeqable a)]))
(defn last-pair
  "Q. 2.17"
  [coll]
  {:pre [(seq coll)]}
  (if-let [next_ (next coll)]
    (recur next_)
    coll))

(ann last_ (All [a] [(NonEmptySeqable a) -> a]))
(defn last_
  {:test #(do (is (= (last_ [1 2 3]) 3)))}
  [coll]
  (if-let [rest_ (seq (rest coll))]
    (recur rest_)
    (first coll)))

(ann append (All [a b] [(Seqable a) (Seqable b) -> (LazySeq (U a b))]))
(defn append
  {:test #(do (is (= (append [1 2] [3 4]) [1 2 3 4])))}
  [coll1 coll2]
  (lazy-seq
   (if-let [s (seq coll1)]
     (cons (first s) (append (rest s) coll2))
     coll2)))

(ann reduce_ (All [a b] (Fn [[a b -> b] b (Seqable a) -> b]
                            ; core.typed does not infer `b` = `(Seqable (U a b))`
                            [[a (Seqable (U a b))
                              -> (Seqable (U a b))]
                             (Seqable (U a b))
                             (Seqable a)
                             -> (Seqable (U a b))]
                            ; core.typed does not infer `a` = `b` = `(Seqable a)`
                            [[(Seqable a) (Seqable a)
                              -> (Seqable a)] (Seqable a)
                              (Seqable (Seqable a))
                              -> (Seqable a)])))

(defn reduce_
  {:test #(do (is (= (reduce_ +' 0 [1 2]) 3))
              (is (= (reduce_ / 1 [2 3]) 2/3)))}
  [f zero coll]
  (if-let [s (seq coll)]
    (f (first s)
       (reduce_ f zero (rest s)))
    zero))

(ann append_ (All [a b] [(Seqable a) (Seqable b)
                         -> (LazySeq (U a b))]))

(defn append_
  {:test #(do (is (= (append_ [1 2] [3 4]) [1 2 3 4]))
              (is (= (append_ [] [3 4]) [3 4]))
              (is (= (append_ [1 2] []) [1 2]))
              (is (= (append_ [1 2] [[[3]]]) [1 2 [[3]]])))}
  [coll1 coll2]
  (lazy-seq
   (reduce_ cons coll2 coll1)))

(ann append_' (All [a b] [(Seqable a) (Seqable b)
                          -> (LazySeq (U a b))]))
(defn append_'
  {:test #(do (is (= (append_' [1 2] [3 4]) [1 2 3 4])))}
  [coll1 coll2]
  (lazy-seq
   (if-let [s (seq coll1)]
     (cons (first s)
           (append_' (rest s) coll2))
     coll2)))

(ann reverse_ (All [a] [(Seqable a) -> (LazySeq a)]))
(defn reverse_
  "Q. 2.18"
  {:test #(do (is (= (reverse_ [1 2 3]) [3 2 1])))}
  [coll]
  (if-let [s (seq coll)]
              (append_ (reverse_ (rest s)) [(first s)])
              (lazy-seq coll)))

(ann first-denomination [Int -> Int])
(defn first-denomination [kinds-of-coins]
  (cond
   (= kinds-of-coins 1) 1
   (= kinds-of-coins 2) 5
   (= kinds-of-coins 3) 10
   (= kinds-of-coins 4) 25
   (= kinds-of-coins 5) 50
   :else (throw (Exception. (str "kinds-of-coins are out of range: " kinds-of-coins)))))

(ann cc [Int Int -> Int])
(defn cc [amount kinds-of-coins]
  (cond
   (zero? amount) 1
   (or (< amount 0) (zero? kinds-of-coins)) 0
   :else (+' (cc amount
                 (dec' kinds-of-coins))
             (cc (-' amount
                     (first-denomination kinds-of-coins))
                 kinds-of-coins))))

(ann count-change [Int -> Int])
(defn count-change [amount]
  (cc amount 5))

(ann us-coins (NonEmptyColl Int))
(def us-coins [50 25 10 5 1])

(ann uk-coins (NonEmptyColl Int))
(def uk-coins [10000 5000 2000 1000 500 200 100 50])

(ann first-denomination' [(Seqable Int) -> Int])
(defn first-denomination' [coin-values]
  (bigint (first coin-values)))

(ann except-first-denomination [(Seqable Int) -> (LazySeq Int)])
(defn except-first-denomination [coin-values]
  (lazy-seq
   (rest coin-values)))

(ann no-more? [(Seqable Int) -> Boolean])
(defn no-more? [coin-values]
  (empty? coin-values))

(ann cc' [Int (Seqable Int) -> Int])
(defn cc'
  "Q. 2.19"
  {:test #(do (is (= (cc' 100 us-coins) 292)))}
  [amount coin-values]
  (cond
   (zero? amount) 1
   (or (< amount 0) (no-more? coin-values)) 0
   :else (+ (cc' amount
                 (rest coin-values))
            (cc' (-' amount
                     (first-denomination' coin-values))
                 coin-values))))

(ann filter_ (All [a] [[a -> Boolean] (Seqable a)
                       -> (LazySeq a)]))
(defn filter_ [f coll]
  (lazy-seq
   (if-let [s (seq coll)]
     (let [x (first s)
           xs (rest s)]
       (if (f x)
         (cons x (filter_ f xs))
         (filter_ f xs)))
     coll)))

(ann same-parity [Int Int * -> (LazySeq Int)])
(defn same-parity
  "2.20"
  {:test #(do (is (= (same-parity 1 2 3 4 5 6 7) [1 3 5 7]))
              (is (= (same-parity 2 3 4 5 6 7) [2 4 6])))}
  [n & more]
  (lazy-seq
   (if more
     (cons n
           (filter_ (if (even? n)
                      even?
                      odd?)
                    more))
     [n])))


(ann map_ (All [a b] [[a -> b] (Seqable a) -> (LazySeq b)]))
(defn map_
  "Q. 2.33-1"
  {:test #(do (is (= (map_ square [1 2 3]) [1 4 9])))}
  [f coll]
  (lazy-seq
   (reduce_ (fn> [x :- a
                  y :- (Seqable b)]
              (cons (f x) y))
            []
            coll)))

(ann map_' (All [a b] [[a -> b] (Seqable a)
                       -> (LazySeq b)]))
(defn map_' [f coll]
  (lazy-seq
   (if-let [s (seq coll)]
     (cons (f (first s))
           (map_' f (rest s))))))

(ann square-list (Fn [(Seqable Int) -> (LazySeq Int)]
                     [(Seqable Num) -> (LazySeq Num)]))
(defn square-list
  "Q. 2.21-1"
  [coll]
  (lazy-seq
   (if-let [s (seq coll)]
     (cons (square (first s)) (square-list (rest s)))
     [])))

(ann square-list' (Fn [(Seqable Int) -> (LazySeq Int)]
                      [(Seqable Num) -> (LazySeq Num)]))
(defn square-list'
  "Q. 2.21-2"
  [coll]
  (map square coll))

(ann count-leaves [Any -> Int])
(defn count-leaves
  {:test #(do (is (= (count-leaves [[1 2] 3 4]) 4)))}
  [t]
  (cond
   (not (coll? t)) 1
   (empty? t) 0
   :else (+' (count-leaves (first t))
             (count-leaves (rest t)))))

"
# Q. 2.26

(1 2 3 4 5 6)
((1 2 3) 4 5 6)
((1 2 3) (4 5 6))
"

(ann deep-reverse [(Seqable Any) -> (LazySeq Any)])
(defn deep-reverse
  "Q. 2.27"
  {:test #(do (is (= (deep-reverse [[1 2] [3 4]]) [[4 3] [2 1]])))}
  [coll]
  (lazy-seq
   (if-let [s (seq coll)]
     (append (deep-reverse (rest coll))
             [(let [x (first coll)]
                (if (coll? x)
                  (deep-reverse x)
                  x))])
     coll)))

(typed/def-alias Tree (TFn [[a :variance :covariant]] (Rec [this] (typed/Coll (U a this)))))

;(ann fringe [(Coll Any) -> (Coll Any)])
;(ann fringe (All [a] [(Tree a) -> (Coll a)]))
(ann fringe [(Tree Any) -> (Coll Any)])
(defn fringe
  "Q. 2.28"
  {:test #(do (is (= (fringe  [[1 2] [3 [4]]]) [1 2 3 4])))}
  [coll]
  (if (empty? coll)
    []
    (let [x (first coll)]
      (append (if (coll? x)
                (fringe x)
                [x])
              (fringe (rest coll))))))

(typed/def-alias BinaryMobile '{:left BinaryMobileBranch
                                :right BinaryMobileBranch})
(typed/def-alias BinaryMobileBranch '{:length Num
                                      :structure BinaryMobileStructure})
(typed/def-alias BinaryMobileStructure (U Num BinaryMobile))

(typed/ann-record BinaryMobile' [left :- BinaryMobileBranch'
                                 right :- BinaryMobileBranch'])
(defrecord BinaryMobile' [left right])

(typed/ann-record BinaryMobileBranch' [length :- Num
                                       structure :- (U Num BinaryMobile')])
(defrecord BinaryMobileBranch' [length structure])
(typed/def-alias BinaryMobileStructure' (U Num BinaryMobile'))

(ann make-mobile [BinaryMobileBranch BinaryMobileBranch
                  -> BinaryMobile])
(defn make-mobile [left right]
  {:left left
   :right right})

(ann make-branch [Num BinaryMobileStructure -> BinaryMobileBranch])
(defn make-branch [length structure]
  {:length length
   :structure structure})

(ann make-mobile' [BinaryMobileBranch' BinaryMobileBranch'
                   -> BinaryMobile'])
(defn make-mobile' [left right]
  (->BinaryMobile' left right))

(ann make-branch' [Num BinaryMobileStructure'
                   -> BinaryMobileBranch'])
(defn make-branch' [length structure]
  (->BinaryMobileBranch' length structure))

(ann left-branch (Fn [BinaryMobile -> BinaryMobileBranch]
                     [BinaryMobile' -> BinaryMobileBranch']))
(defn left-branch
  "Q. 2.29-a"
  [m]
  (:left m))

(ann right-branch (Fn [BinaryMobile -> BinaryMobileBranch]
                      [BinaryMobile' -> BinaryMobileBranch']))
(defn right-branch
  "Q. 2.29-a"
  [m]
  (:right m))

(ann branch-length (Fn [BinaryMobileBranch -> Num]
                       [BinaryMobileBranch' -> Num]))
(defn branch-length
  "Q. 2.29-a"
  [b]
  (:length b))

(ann branch-structure (Fn [BinaryMobileBranch -> BinaryMobileStructure]
                          [BinaryMobileBranch' -> BinaryMobileStructure']))
(defn branch-structure
  "Q. 2.29-a"
  [b]
  (:structure b))

(ann total-weight (Fn [BinaryMobileStructure -> Num]
                      [BinaryMobileStructure' -> Num]))
(defn total-weight
  "Q. 2-29-b"
  [m]
  (if (number? m) ; (U (I (Coll Any) Num) BinaryMobile) /= BinaryMobile
    m
    (+' (total-weight (branch-structure (left-branch m)))
        (total-weight (branch-structure (right-branch m))))))

(ann is-balanced (Fn [BinaryMobileStructure -> Boolean]
                     [BinaryMobileStructure' -> Boolean]))
(defn is-balanced
  "Q. 2-29-c"
  {:test #(do (is (is-balanced (make-mobile
                                (make-branch 3 1)
                                (make-branch 1
                                             (make-mobile
                                              (make-branch 1 2)
                                              (make-branch 2 1))))))
              (is (is-balanced (make-mobile'
                                (make-branch' 3 1)
                                (make-branch' 1
                                              (make-mobile'
                                               (make-branch' 1 2)
                                               (make-branch' 2 1)))))))}
  [m]
  (if (number? m)
    true
    (letfn> [branch-moment :- (Fn [BinaryMobileBranch -> Num]
                                  [BinaryMobileBranch' -> Num])
             (branch-moment [b]
                            (*' (branch-length b)
                                (total-weight (branch-structure b))))]
      (let [lb (left-branch m)
            ls (branch-structure lb)
            rb (right-branch m)
            rs (branch-structure rb)]
        (and (is-balanced ls)
             (is-balanced rs)
             (= (branch-moment lb)
                (branch-moment rb)))))))

(typed/tc-ignore

; Recursive map seems to be not typable...

(typed/def-alias IntTree (Coll (U Int IntTree)))
(typed/def-alias NumTree (Coll (U Num NumTree)))

(ann square-tree [(Coll Num) -> (Coll Num)])
(defn square-tree
  "Q. 2.30-1"
  {:test #(do (is (= (square-tree [1 [2 [3]] 4]) [1 [4 [9]] 16])))}
  [t]
  (if (empty? t)
    []
    (cons (let [t1 (first t)]
            (if (number? t1)
              (square t1)
              (square-tree t1)))
          (square-tree (rest t)))))

(ann square-tree' (Fn [IntTree -> IntTree]
                      [NumTree -> NumTree]))
(defn square-tree'
  "Q. 2.30-2"
  {:test #(do (is (= (square-tree' [1 [2 [3]] 4]) [1 [4 [9]] 16])))}
  [t]
  (map_ (ann-form (fn [e] (if (number? e)
                            (square e)
                            (square-tree' e)))
                  (Fn [Int -> Int]
                      [IntTree -> IntTree]
                      [Num -> Num]
                      [NumTree -> NumTree]))
       t))

(defn tree-map
  "Q. 2.31"
  {:test #(do (is (= (tree-map square [1 [2 [3]] 4]) [1 [4 [9]] 16])))}
  [f coll]
  (map (fn [x] (if (coll? x)
                 (tree-map f x)
                 (f x)))
       coll))
)

(ann never-nil (All [a] [(Option a) -> a]))
(defn never-nil [x]
  (assert (not (nil? x)))
  x)

(ann subsets (All [a] [(Seqable a)
                       -> (LazySeq (LazySeq a))]))
(defn subsets
  "Q. 2.32"
  {:test #(do (is (= (set (subsets [1 2 3]))
                     #{[] [1] [2] [3] [1 2] [2 3] [1 3] [1 2 3]})))}
  [coll]
  (lazy-seq
   (if-let [s (seq coll)]
     (let [s1 (first s)
           subs (subsets (rest s))]
       (append_ subs
                (map_ (fn> [sub :- (Seqable a)] ; XXX: anaphoric?
                        (lazy-seq (cons s1 sub)))
                      subs)))
     [(lazy-seq coll)])))

(ann accumulate (All [a b] (Fn [[a b -> b] b (Seqable a) -> b]
                               [[a (Seqable (U a b)) -> (LazySeq (U a b))] (LazySeq (U a b)) (Seqable a) ; core.typed does not infer`b` = `(Seqable (U a b))`
                                -> (LazySeq (U a b))]
                               [[a (LazySeq (U a b)) -> (LazySeq (U a b))] (LazySeq (U a b)) (Seqable a) ; core.typed does not infer`b` = `(LazySeq (U a b))`
                                -> (LazySeq (U a b))])))

(defn accumulate
  {:test #(do (is (= (accumulate +' 0 [1 2]) 3)))}
  [f zero coll]
  (if-let [s (seq coll)]
    (f (first s)
       (accumulate f zero (rest s)))
    zero))

(ann enumerate-interval [Int Int -> (LazySeq Int)])
(defn enumerate-interval [low high]
  (lazy-seq
   (if (> low high)
     []
     (cons low (enumerate-interval (inc low)
                                   high)))))

(ann range_ [Int Int -> (LazySeq Int)])
(defn range_ [low high]
  (lazy-seq
   (if (> low high)
     []
     (cons low (range_ (inc low) high)))))

(typed/tc-ignore

(ann sum-odd-squares [(Rec [this] (Coll (U Int this))) -> Int])
(defn sum-odd-squares
  [tree]
  (accumulate +'
              0
              (map_ square
                    (filter_ odd?
                             (fringe tree)))))
)

(ann flip (All [a b c] [[a b -> c]
                        -> [b a -> c]]))
(defn flip [f]
  (fn> [y :- b
        x :- a] (f x y)))

(ann even-fib [Int -> (LazySeq Int)])
(defn even-fib [n]
  (->> (range_ 0 n)
       (map_ fib)
       (filter_ even?)))

(ann append_2_33 (All [a b] [(Seqable a) (Seqable b) -> (LazySeq (U a b))]))

(defn append_2_33
  "Q. 2.33-2"
  {:test #(do (is (= (append_2_33 [1 2] [3 4]) [1 2 3 4]))
              (is (= (append_2_33 [] [3 4]) [3 4]))
              (is (= (append_2_33 [1 2] []) [1 2]))
              (is (= (append_2_33 [1 2] [[[3]]]) [1 2 [[3]]])))}
  [coll1 coll2]
  (lazy-seq
   (reduce_ cons coll2 coll1)))

(ann map_2_33 (All [a b] [[a -> b] (Seqable a) -> (LazySeq b)]))
(defn map_2_33
  "Q. 2.33-1"
  {:test #(do (is (= (map_2_33 square [1 2 3]) [1 4 9])))}
  [f coll]
  (lazy-seq
   (reduce_ (fn> [x :- a
                  y :- (Seqable b)]
              (append_2_33 [(f x)] y))
            []
            coll)))

(ann length_2_33 [(Seqable Any) -> Int])
(defn length_2_33
  "Q. 2.33-3"
  {:test #(do (is (= (length_2_33 []) 0))
              (is (= (length_2_33 [1]) 1))
              (is (= (length_2_33 [1 2]) 2))
              (is (= (length_2_33 [[] 2 [[]]]) 3)))}
  [coll]
  (reduce_ (fn> [_ :- Any
                 sum :- Int] (inc sum)) 0 coll))

(ann horner-eval [Num (NonEmptyColl Num) -> Num])
(defn horner-eval
  "Q. 2.34"
  {:test #(do (is (= (horner-eval 2 [1 3 0 5 0 1]) 79)))}
  [x coefficient-seqence]
  (accumulate (fn> [this-term :- Num
                    higher-terms :- Num]
                (+' this-term
                    (*' x
                        higher-terms)))
              0
              coefficient-seqence))

(ann count-leaves' [(Coll Any) -> Int])
(defn count-leaves'
  "Q. 2.35"
  {:test #(do (is (= (count-leaves' [[1 2] 3 4]) 4)))}
  [tree]
  (accumulate (fn> [x :- Any
                    sum :- Int]
                (if (coll? x)
                  (+' sum
                      (count-leaves' x))
                  (inc' sum)))
              0
              tree))

(typed/tc-ignore

(ann accumulate-n (All [a b] (Fn [[a b -> b] b (Coll (Coll a)) -> (Coll b)])))
(defn accumulate-n
  "Q. 2.36"
  {:test #(do (is (= (accumulate-n +' 0 [[1 2 3]
                                         [4 5 6]
                                         [7 8 9]
                                         [10 11 12]])
                     [22 26 30])))}
  [op init colls]
  {:pre [(apply = (map count colls))]}
  (if (empty? colls)
    []
    (if (some empty? colls)
      []
      (cons (accumulate op init (map_ first colls))
        (accumulate-n op init (map_ rest colls))))))
)

(ann dot-product (Fn [(Seqable Int) (Seqable Int) -> Int]
                     [(Seqable Num) (Seqable Num) -> Num]))
(defn dot-product
  {:test #(do (is (= (dot-product [1 2] [3 4]) 11)))}
  [v w]
  (reduce_ +' 0 (map *' v w)))

(ann matrix-*-vector [(Seqable (Seqable Num)) (Seqable Num)
                      -> (LazySeq Num)])
(defn matrix-*-vector
  "Q. 3.27"
  {:test #(do (is (= (matrix-*-vector [[1 2]
                                       [3 4]] [5 6])
                     [17 39])))}
  [m v]
  (map_ (fn> [row :- (Seqable Num)] (dot-product row v))
        m))

(ann transpose (All [a] [(NonEmptySeqable (Seqable a)) -> (LazySeq (LazySeq a))]))
(defn transpose
  "Q. 3.27"
  {:test #(do (is (= (transpose [[1 2]
                                 [3 4]])
                     [[1 3]
                      [2 4]]))
              (is (= (transpose [[3 4]])
                     [[3]
                      [4]])))}
  [m]
  (lazy-seq
   (accumulate (fn> [column :- (Seqable a)
                       rows :- (LazySeq (LazySeq a))]
                   (map (fn> [x :- a
                               row :- (LazySeq a)]
                           (lazy-seq (cons x row)))
                         column
                         rows))
                 (lazy-seq (repeat (count (first m))
                                   (ann-form (lazy-seq [])
                                             (LazySeq a))))
                 m)))

(ann matrix-*-matrix [(NonEmptySeqable (Seqable Num)) (NonEmptySeqable (Seqable Num))
                      -> (LazySeq (LazySeq Num))])
(defn matrix-*-matrix
  "Q. 3.27"
  {:test #(do (is (= (matrix-*-matrix [[1]
                                       [2]]
                                      [[3 4]])
                     [[3 4]
                      [6 8]])))}
  [m n]
  (let [cols (transpose n)]
    (map (fn> [row :- (Seqable Num)]
           (map (fn> [column :- (Seqable Num)]
                  (dot-product row column))
                cols))
         m)))

"
# Q. 2.38
(assert (= (op a b) (op b a)))
"

(ann fold-left (All [a b] (Fn [[a b -> b] b (Seqable a) -> b]
                              [[a (Seqable (U a b))
                                -> (Seqable (U a b))]
                               (Seqable (U a b))
                               (Seqable a)
                               -> (Seqable (U a b))])))
(defn fold-left [op initial coll]
  {:test #(do (is (= (fold-left / 1 [2 3]) 1/6)))}
  (if-let [s (seq coll)]
    (recur op (op (first s) initial) (rest s))
    initial))

(ann reverse_2_39_1 (All [a] [(Seqable a) -> (Seqable a)]))
(defn reverse_2_39_1
  "Q. 2.39"
  {:test #(do (is (= (reverse_2_39_1 [1 2 3]) [3 2 1])))}
  [coll]
  (reduce_ (fn> [x :- a
                 ret :- (Seqable a)] (append_ ret [x]))
           []
           coll))

(ann reverse_2_39_2 (All [a] [(Seqable a) -> (Seqable a)]))
(defn reverse_2_39_2
  "Q. 2.39"
  {:test #(do (is (= (reverse_2_39_2 [1 2 3]) [3 2 1])))}
  [coll]
  (fold-left cons [] coll))

(ann flat-map (All [a b] [[a -> (Seqable b)] (Seqable a) -> (Seqable b)]))
(defn flat-map [f coll]
  (reduce_ append_ [] (map_ f coll)))

(ann prime-sum? ['[Int Int] -> Boolean])
(defn prime-sum? [[l r]]
  (prime? (+' l r)))

(ann make-pair-sum (Fn ['[Int Int] -> '[Int Int Int]]
                       ['[Num Num] -> '[Num Num Num]]))
(defn make-pair-sum [[l r]]
  [l r (+' l r)])

(ann prime-sum-pairs [Int -> (Seqable '[Int Int Int])])
(defn prime-sum-pairs
  {:test #(do (is (= (prime-sum-pairs 3)
                     [[1 2 3] [2 3 5]])))}
  [n]
  (map make-pair-sum
       (filter prime-sum?
               (flat-map (fn> [j :- Int]
                           (map_ (fn> [i :- Int]
                                   [i j])
                                 (range_ 1 (dec j))))
                         (range_ 1 n)))))

(ann permutations (All [a] [(Seqable a) -> (Seqable (Seqable a))]))
(defn permutations
  {:test #(do (is (= (permutations [1 2])
                     [[1 2] [2 1]])))}
  [coll]
  (if-let [set (seq coll)]
    (flat-map (fn> [s1 :- a]
                (map (fn> [set-1 :- (Seqable a)] (cons s1 set-1))
                     (permutations (remove (fn> [x :- a] (= x s1))
                                           set))))
              set)
    [coll]))

(ann unique-pairs [Int -> (Seqable '[Int Int])])
(defn unique-pairs
  "Q. 2.40"
  [n]
  (flat-map (fn> [j :- Int]
              (map (fn> [i :- Int]
                     [i j])
                   (range 1 j)))
            (range 1 (inc n))))

(ann prime-sum-pairs' [Int -> (Seqable '[Int Int Int])])
(defn prime-sum-pairs'
  "Q. 2.40"
  {:test #(do (is (= (prime-sum-pairs' 3)
                     [[1 2 3] [2 3 5]])))}
  [n]
  (map make-pair-sum
       (filter prime-sum? (unique-pairs n))))

(ann unique-triples [Int -> (Seqable '[Int Int Int])])
(defn unique-triples
  [n]
  (flat-map (fn> [k :- Int]
              (map (fn> [[i j] :- '[Int Int]]
                     [i j k])
                   (unique-pairs (dec k))))
            (range 1 (inc n))))

(ann sum_ (Fn [(Seqable Int) -> Int]
              [(Seqable Num) -> Num]))
(defn sum_ [coll]
  (reduce_ +' 0 coll))

(ann filter-sum-triples [Int Int -> (Seqable '[Int Int Int])])
(defn filter-sum-triples
  "Q. 2.41"
  {:test #(do (is (= (filter-sum-triples 5 8)
                     [[1 3 4]
                      [1 2 5]])))}
  [n, s]
  (filter (fn> [triple :- '[Int Int Int]]
            (= (sum_ triple) s))
          (unique-triples n)))

(ann combination2 (All [a b] [(Seqable a) (Seqable b) -> (Seqable '[a b])]))
(defn combination2
  {:test #(do (is (= (combination2 [1 2] [3 4])
                     [[1 3]
                      [1 4]
                      [2 3]
                      [2 4]])))}
  [coll1 coll2]
  (flat-map (fn> [x :- a]
              (map (fn> [y :- b]
                     [x y])
                   coll2))
            coll1))

(typed/def-alias QueenPosition '[Int Int])

(ann queens [Int -> (Seqable (Seqable QueenPosition))])
(defn queens
  "Q. 2.42"
  {:test #(do (is (= (queens 4)
                     [[[3 4] [1 3] [4 2] [2 1]]
                      [[2 4] [4 3] [1 2] [3 1]]])))}
  [board-size]
  (let [empty-board []]
    (letfn> [safe? :- [(Seqable QueenPosition) -> Boolean]
             (safe? [positions]
                    (if-let [s (seq positions)]
                      (let [[i1 j1] (first s)]
                        (every? (fn> [[i2 j2] :- QueenPosition]
                                  (not
                                   (or
                                    (= i1 i2)
                                    (= j1 j2)
                                    (= (abs (- i1 i2))
                                       (abs (- j1 j2))))))
                                (rest s)))
                      true))

             adjoin-position :- [Int Int (Seqable QueenPosition)
                                 -> (Seqable QueenPosition)]
             (adjoin-position [i j qs]
                              (cons [i j] qs))

             queen-cols :- [Int -> (Seqable (Seqable QueenPosition))]
             (queen-cols [k]
                         (if (zero? k)
                           [empty-board]
                           (filter
                            safe?
                            (flat-map
                             (fn> [rest-of-queens :- (Seqable QueenPosition)]
                               (map (fn> [new-row :- Int]
                                      (adjoin-position new-row k rest-of-queens))
                                    (range 1 (inc board-size))))
                             (queen-cols (dec k))))))]
      (queen-cols board-size))))

"
# Q. 2.43
P_{k} = T_{k} + P_{k - 1}
P_{n} = Σ_{k = 1}^{n} T_{k}

Q_{k} = T_{k} + n*Q_{k - 1}
Q_{n} = Σ_{k = 1}^{n} n^{n - k}*T_{k}

n^n
"

(typed/def-alias Vector '[Num Num])

(ann make-vect [Num Num -> Vector])
(defn make-vect
  "Q. 2.46"
  [x y]
  [x y])

(ann xcor-vect [Vector -> Num])
(defn xcor-vect
  "Q. 2.46"
  [[x y]]
  x)

(ann ycor-vect [Vector -> Num])
(defn ycor-vect
  "Q. 2.46"
  [[x y]]
  y)

(ann add-vect [Vector Vector -> Vector])
(defn add-vect
  "Q. 2.46"
  [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(ann sub-vect [Vector Vector -> Vector])
(defn sub-vect
  "Q. 2.46"
  [[x1 y1] [x2 y2]]
  [(- x1 x2) (- y1 y2)])

(ann scale-vect [Num Vector -> Vector])
(defn scale-vect
  "Q. 2.46"
  [s [x y]]
  [(* s x) (* s y)])

(typed/def-alias Frame '[Vector Vector Vector])

(ann make-frame [Vector Vector Vector -> Frame])
(defn make-frame [origin edge1 edge2]
  [origin edge1 edge2])

(ann origin-frame [Frame -> Vector])
(defn origin-frame
  "Q. 2.47"
  [[origin _ _]]
  origin)

(ann edge1-frame [Frame -> Vector])
(defn edge1-frame
  "Q. 2.47"
  [[_ edge1 _]]
  edge1)

(ann edge2-frame [Frame -> Vector])
(defn edge2-frame
  "Q. 2.47"
  [[_ _ edge2]]
  edge2)

(typed/def-alias Frame' '{:origin Vector
                          :edge1 Vector
                          :edge2 Vector})

(ann make-frame' [Vector Vector Vector -> Frame'])
(defn make-frame' [origin edge1 edge2]
  {:origin origin
   :edge1 edge1
   :edge2 edge2})

(ann origin-frame' [Frame' -> Vector])
(defn origin-frame'
  "Q. 2.47"
  [frame]
  (:origin frame))

(ann edge1-frame' [Frame' -> Vector])
(defn edge1-frame'
  "Q. 2.47"
  [{edge1 :edge1}]
  edge1)

(ann edge2-frame' [Frame' -> Vector])
(defn edge2-frame'
  "Q. 2.47"
  [frame]
  (:edge2 frame))

(ann frame-coord-map [Frame -> [Vector -> Vector]])
(defn frame-coord-map [frame]
  (fn [v]
    (add-vect (origin-frame frame)
              (add-vect (scale-vect (xcor-vect v)
                                    (edge1-frame frame))
                        (scale-vect (ycor-vect v)
                                    (edge2-frame frame))))))

(typed/def-alias Segment '[Vector Vector])

(ann make-segment [Vector Vector -> Segment])
(defn make-segment
  "Q. 2.48"
  [start end]
  [start end])

(ann start-segment [Segment -> Vector])
(defn start-segment
  "Q. 2.48"
  [[start _]]
  start)

(ann end-segment [Segment -> Vector])
(defn end-segment
  "Q. 2.48"
  [[_ end]]
  end)

(ann draw-line [Vector Vector -> nil])
(defn draw-line
  "GMT psxy -JX15c -R0/1/0/1 -m -P < output_of_draw_line.xy >| fig.ps"
  [start end]
  (println (xcor-vect start) (ycor-vect start))
  (println (xcor-vect end) (ycor-vect end))
  (println ">"))

(typed/def-alias Painter [Frame -> nil])

(ann segments->painter [(Seqable Segment) -> Painter])
(defn segments->painter [segment-list]
  (fn> [frame :- Frame]
    (doseq> [segment :- Segment segment-list]
      (draw-line
         ((frame-coord-map frame) (start-segment segment))
         ((frame-coord-map frame) (end-segment segment))))))

(ann transform-painter [Painter Vector Vector Vector -> Painter])
(defn transform-painter [painter origin corner1 corner2]
  (fn> [frame :- Frame]
    (let [m (frame-coord-map frame)]
      (let [new-origin (m origin)]
        (painter
         (make-frame new-origin
                     (sub-vect (m corner1) new-origin)
                     (sub-vect (m corner2) new-origin)))))))

(ann flip-vert [Painter -> Painter])
(defn flip-vert [painter]
  (transform-painter painter
                     (make-vect 0.0 1.0)
                     (make-vect 1.0 1.0)
                     (make-vect 0.0 0.0)))

(ann shrink-to-upper-right [Painter -> Painter])
(defn shrink-to-upper-right [painter]
  (transform-painter painter
                     (make-vect 0.5 0.5)
                     (make-vect 1.0 0.5)
                     (make-vect 0.5 1.0)))

(ann rotate90 [Painter -> Painter])
(defn rotate90 [painter]
  (transform-painter painter
                     (make-vect 1.0 0.0)
                     (make-vect 1.0 1.0)
                     (make-vect 0.0 0.0)))

(ann squash-inwards [Painter -> Painter])
(defn squash-inwards [painter]
  (transform-painter painter
                     (make-vect 0.0 0.0)
                     (make-vect 0.65 0.35)
                     (make-vect 0.35 0.65)))

(ann beside [Painter Painter -> Painter])
(defn beside [left right]
  (let [split-point (make-vect 0.5 0.0)]
    (let [l (transform-painter left
                               (make-vect 0.0 0.0)
                               split-point
                               (make-vect 0.0 1.0))
          r (transform-painter right
                               split-point
                               (make-vect 1.0 0.0)
                               (make-vect 0.5 1.0))]
      (fn> [frame :- Frame]
        (l frame)
        (r frame)))))

(ann each-cons (All [a] [Int (Seqable a) -> (LazySeq (LazySeq a))]))
(defn each-cons
  {:test #(do (is (= (each-cons 2 [0 1 2 3]) [[0 1] [1 2] [2 3]])))}
  [n coll]
  {:pre [(>= n 0)]}
  (letfn> [this :- [(Seqable a) -> (LazySeq (LazySeq a))]
           (this [coll]
                 (lazy-seq
                  (let [head (take n coll)]
                    (if (= (count head) n)
                      (cons head (this (rest coll)))
                      (lazy-seq [])))))]
    (this coll)))

(ann vects->segments [(Seqable Vector) -> (LazySeq Segment)])
(defn vects->segments [vects]
  (map (fn> [[start end] :- (LazySeq Vector)]
         {:pre [(not (nil? start)) (not (nil? end))]}
         (make-segment start end))
       (each-cons 2 vects)))

(ann outer-frame-painter Painter)
(def outer-frame-painter
  "Q. 2.49-a"
  (-> [(make-vect 0.0 0.0)
       (make-vect 1.0 0.0)
       (make-vect 1.0 1.0)
       (make-vect 0.0 1.0)
       (make-vect 0.0 0.0)]
      vects->segments
      segments->painter))

(ann x-painter Painter)
(def x-painter
  "Q. 2.49-b"
  (segments->painter [(make-segment (make-vect 0.0 0.0) (make-vect 1.0 1.0))
                      (make-segment (make-vect 1.0 0.0) (make-vect 0.0 1.0))]))

(ann diamond-painter Painter)
(def diamond-painter
  "Q. 2.49-c"
  (-> [(make-vect 0.5 0.0)
       (make-vect 1.0 0.5)
       (make-vect 0.5 1.0)
       (make-vect 0.0 0.5)
       (make-vect 0.5 0.0)]
      vects->segments
      segments->painter))

(ann wave Painter)
(def wave
  "Q. 2.49-d"
  (segments->painter [(make-segment (make-vect 0.1 0.1) (make-vect 0.2 0.3))
                      (make-segment (make-vect 0.2 0.3) (make-vect 0.8 0.9))
                      (make-segment (make-vect 0.1 0.1) (make-vect 0.5 0.9))
                      (make-segment (make-vect 0.1 0.1) (make-vect 0.1 0.5))]))

(ann merge-painter [Painter Painter -> Painter])
(defn merge-painter [p1 p2]
  (fn> [frame :- Frame]
    (p1 frame)
    (p2 frame)))

(ann wave' Painter)
(def wave'
  "Q. 2.52-a"
  (merge-painter wave
                 (segments->painter [(make-segment (make-vect 0.1 0.5) (make-vect 0.9 0.5))])))

(ann flip-horiz [Painter -> Painter])
(defn flip-horiz
  "Q. 2.50"
  [painter]
  (transform-painter painter
                     (make-vect 1.0 0.0)
                     (make-vect 0.0 0.0)
                     (make-vect 1.0 1.0)))

(ann rotate180 [Painter -> Painter])
(defn rotate180
  "Q. 2.50"
  [painter]
  ((double_ rotate90) painter))

(ann rotate270 [Painter -> Painter])
(defn rotate270
  "Q. 2.50"
  [painter]
  (rotate180 (rotate90 painter)))

(ann below [Painter Painter -> Painter])
(defn below
  "Q. 2.51"
  [bottom top]
  (let [split-point (make-vect 0.0 0.5)]
    (let [b (transform-painter bottom
                               (make-vect 0.0 0.0)
                               (make-vect 1.0 0.0)
                               split-point)
          t (transform-painter top
                               split-point
                               (make-vect 1.0 0.5)
                               (make-vect 0.0 1.0))]
      (fn> [frame :- Frame]
        (b frame)
        (t frame)))))

(ann below' [Painter Painter -> Painter])
(defn below'
    "Q. 2.51"
    [bottom top]
    (rotate90 (beside (rotate270 bottom)
                      (rotate270 top))))

(ann wave2 Painter)
(def wave2 (beside wave (flip-vert wave)))

(ann wave4 Painter)
(def wave4 (below wave2 wave2))

(ann flipped-pairs [Painter -> Painter])
(defn flipped-pairs [painter]
  (let [painter2 (beside painter (flip-vert painter))]
    (below painter2 painter2)))

(ann wave4' Painter)
(def wave4' (flipped-pairs wave))

(ann right-split [Painter Int -> Painter])
(defn right-split [painter n]
  (if (zero? n)
    painter
    (let [smaller (right-split painter (dec n))]
      (beside painter (below smaller smaller)))))

(ann up-split [Painter Int -> Painter])
(defn up-split
  "Q. 2.44"
  [painter n]
  (if (zero? n)
    painter
    (let [smaller (up-split painter (dec n))]
      (below painter (beside smaller smaller)))))

(ann corner-split [Painter Int -> Painter])
(defn corner-split [painter n]
  (if (zero? n)
    painter
    (let [up (up-split painter (dec n))
          right (right-split painter (dec n))]
      (let [top-left (beside up up)
            bottom-right (below right right)
            corner (corner-split painter (dec n))]
        (beside (below painter top-left)
                (below bottom-right corner))))))

(ann corner-split' [Painter Int -> Painter])
(defn corner-split'
  "Q. 2.52-b"
  [painter n]
  (if (zero? n)
    painter
    (let [top-left (up-split painter (dec n))
          bottom-right (right-split painter (dec n))
          corner (corner-split' painter (dec n))]
      (beside (below painter top-left)
              (below bottom-right corner)))))

(ann square-of-four [[Painter -> Painter]
                     [Painter -> Painter]
                     [Painter -> Painter]
                     [Painter -> Painter]
                     -> [Painter -> Painter]])
(defn square-of-four [tl tr bl br]
  (fn> [painter :- Painter]
    (let [top (beside (tl painter) (tr painter))
          bottom (beside (bl painter) (br painter))]
      (below bottom top))))

(ann flipped-pairs' [Painter -> Painter])
(defn flipped-pairs' [painter]
  (let [combine4 (square-of-four identity flip-vert
                                 identity flip-vert)]
    (combine4 painter)))

(ann square-limit [Painter Int -> Painter])
(defn square-limit [painter n]
  (let [combine4 (square-of-four flip-horiz identity
                                 rotate180 flip-vert)]
    (combine4 (corner-split painter n))))

(ann square-limit' [Painter Int -> Painter])
(defn square-limit'
  "Q. 2.52-c"
  [painter n]
  (let [combine4 (square-of-four identity flip-horiz
                                 flip-vert rotate180)]
    (combine4 (corner-split painter n))))

(ann split [[Painter Painter -> Painter] [Painter Painter -> Painter]
            -> [Painter Int -> Painter]])
(defn split
  "Q. 2.45"
  [p1 p2]
  (letfn> [ret :- [Painter Int -> Painter]
           (ret [painter n]
                (if (zero? n)
                  painter
                  (let [smaller (ret painter (dec n))]
                    (p1 painter (p2 smaller smaller)))))]
    ret))
(ann -main [String * -> nil])
(defn -main [& args]
  (clojure.test/run-tests 'sicp.core)
  (println "ok"))
