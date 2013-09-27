(ns sicp.core
  (:require [clojure.test :refer [is are]]
            [clojure.pprint]
            [clojure.math.numeric-tower]
            [clojure.repl]
            [clojure.core.typed :refer [ann-form ann Int Num letfn> loop> fn> Vec Coll NonEmptyColl] :as typed])
  (:gen-class))

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
(ann ^:no-check clojure.core/+' (All [[a :< Num]]
                          (Fn [-> Long]
                              [a -> a]
                              [Int Int * -> Int]
                              [Num Num * -> Num])))
; `(All [[a :< Num]] [a -> a])` is not true since `(type (-' Long/MIN_VALUE))` is BigInt.
(ann ^:no-check clojure.core/-' (Fn [Int * -> Int]
                         [Num * -> Num]))
(ann ^:no-check clojure.core/*' (All [[a :< Num]]
                          (Fn [-> Long]
                              [a -> a]
                              [Int Int * -> Int]
                              [Num Num * -> Num])))
(ann ^:no-check clojure.test/run-tests [clojure.lang.Namespace *
                             ->
                             (HMap :mandatory {:type clojure.lang.Keyword
                                               :pass Int
                                               :test Int
                                               :error Int
                                               :fail Int})])
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
              (is (thrown? java.lang.AssertionError (fib 0))))}

  [n]
  {:pre [(>= n 1)]}

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
              (odd? n) (fib-iter (+' (*' b q) (*' a (+' p q)))
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

(typed/def-alias Rat (I (Vec Int) (CountRange 2 2)))

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

(typed/def-alias Point2D (I (Vec Num) (CountRange 2 2)))

(ann x-point [Point2D -> Num])
(defn x-point [p]
  (first p))

(ann y-point [Point2D -> Num])
(defn y-point [p]
  (second p))

(ann print-point [Point2D -> nil])
(defn print-point [p]
  (println "(" (x-point p) ", " (y-point p) ")"))

(ann make-point [Num Num -> Point2D])
(defn make-point [x y]
  [x y])

(typed/def-alias Line2D (I (Vec Point2D) (CountRange 2 2)))

(ann start-segment [Line2D -> Point2D])
(defn start-segment [l]
  (first l))

(ann end-segment [Line2D -> Point2D])
(defn end-segment [l]
  (second l))

(ann make-segment [Point2D Point2D -> Line2D])
(defn make-segment [p q]
  [p q])

(ann midpoint-segment [Line2D -> Point2D])
(defn midpoint-segment
  "Q. 2.2"
  [l]
  (make-point (average (x-point (start-segment l))
                       (x-point (end-segment l)))
              (average (y-point (start-segment l))
                       (y-point (end-segment l)))))

(typed/def-alias Rectangle (HMap :mandatory {:origin Point2D
                                             :width Num
                                             :height Num
                                             :angle Num}))

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

(typed/def-alias Rectangle' (HMap :mandatory {:origin Point2D
                                              :diag Point2D
                                              :angle Num}))

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

(typed/def-alias Interval (I (Vec Num) (CountRange 2 2)))

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
  {:pre [(>= (*' (lower-bound x) (upper-bound x)) 0)]}
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

(ann last-pair (All [a] [(NonEmptyColl a) -> (NonEmptyColl a)]))
(defn last-pair
  "Q. 2.17"
  [coll]
  {:pre [(not (empty? coll))]}
  (let [next_ (next coll)]
       (if (empty? next_)
         coll
         (recur next_))))

(ann last_ (All [a] [(NonEmptyColl a) -> a]))
(defn last_
  {:test #(do (is (= (last_ [1 2 3]) 3)))}
  [coll]
  (let [rest_ (rest coll)]
    (if (empty? rest_)
      (first coll)
      (recur rest_))))

(ann append (All [a b] [(Coll a) (Coll b) -> (Coll (U a b))]))
(defn append
  {:test #(do (is (= (append [1 2] [3 4]) [1 2 3 4])))}
  [coll1 coll2]
  (if (empty? coll1)
    coll2
    (cons (first coll1) (append (rest coll1) coll2))))

(ann reverse_ (All [a] [(Coll a) -> (Coll a)]))
(defn reverse_
  "Q. 2.18"
  {:test #(do (is (= (reverse_ [1 2 3]) [3 2 1])))}
  [coll]
  (if (empty? coll)
    coll
    (append (reverse_ (rest coll)) [(first coll)])))

(ann reverse_' [(Coll Any) -> (Coll Any)])
(defn reverse_'
  {:test #(do (is (= (reverse_' [1 2 3]) [3 2 1])))}
  [coll]
  (if (empty? coll)
    []
    (conj (reverse_' (rest coll)) (first coll))))

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

(ann first-denomination' [(Coll Int) -> Int])
(defn first-denomination' [coin-values]
  (bigint (first coin-values)))

(ann except-first-denomination [(Coll Int) -> (Coll Int)])
(defn except-first-denomination [coin-values]
  (rest coin-values))

(ann no-more? [(Coll Int) -> Boolean])
(defn no-more? [coin-values]
  (empty? coin-values))

(ann cc' [Int (Coll Int) -> Int])
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

(ann filter_ (All [a] [[a -> Boolean] (Coll a) -> (Coll a)]))
(defn filter_ [f coll]
  (if (empty? coll)
    ()
    (let [v (first coll)]
      (if (f v)
        (cons v (filter_ f (rest coll)))
        (recur f (rest coll))))))

(ann same-parity [Int Int * -> (Coll Int)])
(defn same-parity
  "2.20"
  {:test #(do (is (= (same-parity 1 2 3 4 5 6 7) [1 3 5 7]))
              (is (= (same-parity 2 3 4 5 6 7) [2 4 6])))}
  [n & more]
  (if more
    (cons n
          (filter (if (even? n)
                    even?
                    odd?)
                  more))
    [n]))

(ann map_ (All [a b] [[a -> b] (Coll a) -> (Coll b)]))
(defn map_ [f coll]
  (if (empty? coll)
    ()
    (cons (f (first coll))
          (map_ f (rest coll)))))

(ann square-list (Fn [(Coll Int) -> (Coll Int)]
                     [(Coll Num) -> (Coll Num)]))
(defn square-list
  "Q. 2.21-1"
  [coll]
  (if (empty? coll)
    ()
    (cons (square (first coll)) (square-list (rest coll)))))

(ann square-list' (Fn [(Coll Int) -> (Coll Int)]
                      [(Coll Num) -> (Coll Num)]))
(defn square-list'
  "Q. 2.21-2"
  [coll]
  (map square coll))

(typed/tc-ignore
; Functions that use `coll?`

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
(defn deep-reverse
  "Q. 2.27"
  {:test #(do (is (= (deep-reverse [[1 2] [3 4]]) [[4 3] [2 1]])))}
  [coll]
  (if (empty? coll)
    coll
    (append (deep-reverse (rest coll))
            [(let [x (first coll)]
               (if (coll? x)
                 (deep-reverse x)
                 x))])))

(defn fringe
  "Q. 2.28"
  {:test #(do (is (= (fringe  [[1 2] [3 [4]]]) [1 2 3 4])))}
  [coll]
  (if (empty? coll)
    coll
    (let [x (first coll)]
      (append (if (coll? x)
                (fringe x)
                [x])
              (fringe (rest coll)))))))

(typed/def-alias BinaryMobil (Rec [this]
                                  (HMap :mandatory
                                        {:left (HMap :mandatory
                                                     {:length Num
                                                      :structure (U Num this)})
                                         :right (HMap :mandatory
                                                      {:length Num
                                                       :structure (U Num this)})})))

(typed/ann-record BinaryMobil' [left :- BinaryMobilBranch
                               right :- BinaryMobilBranch])
(defrecord BinaryMobil' [left right])

(typed/ann-record BinaryMobilBranch [length :- Num
                                     structure :- (U Num BinaryMobil')])
(defrecord BinaryMobilBranch [length structure])

(typed/tc-ignore
;; Type Error: Local binding left expected type
;; expected: (HMap :mandatory {:length Num, :structure Num})
;; actual:   (HMap :mandatory {:length Num, :structure (U Num BinaryMobil)})
;; (ann make-mobile' [(HMap :mandatory
;;                          {:length Num
;;                           :structure (U Num BinaryMobil)})
;;                    (HMap :mandatory
;;                          {:length Num
;;                           :structure (U Num BinaryMobil)})
;;                    -> BinaryMobil])
;; (defn make-mobile' [left right]
;;   {:left left
;;    :right right})

(defn make-mobile [left right]
  {:left left
   :right right})

(defn make-branch [length structure]
  {:length length
   :structure structure})
 )


(ann make-mobile' [BinaryMobilBranch BinaryMobilBranch
                  -> BinaryMobil'])
(defn make-mobile' [left right]
  (->BinaryMobil' left right))

(ann make-branch' [Num (U Num BinaryMobil')
                  -> BinaryMobilBranch])
(defn make-branch' [length structure]
  (->BinaryMobilBranch length structure))

(ann left-branch (Fn [BinaryMobil
                      -> (HMap :mandatory
                               {:length Num
                                :structure (U Num BinaryMobil)})]
                     [BinaryMobil' -> BinaryMobilBranch]))
(defn left-branch
  "Q. 2.29-a"
  [m]
  (:left m))

(ann right-branch (Fn [BinaryMobil
                      -> (HMap :mandatory
                               {:length Num
                                :structure (U Num BinaryMobil)})]
                     [BinaryMobil' -> BinaryMobilBranch]))
(defn right-branch
  "Q. 2.29-a"
  [m]
  (:right m))

(ann branch-length (Fn [(HMap :mandatory
                              {:length Num
                               :structure (U Num BinaryMobil)})
                        -> Num]
                       [BinaryMobilBranch -> Num]))
(defn branch-length
  "Q. 2.29-a"
  [b]
  (:length b))

(ann branch-structure (Fn [(HMap :mandatory
                                 {:length Num
                                  :structure (U Num BinaryMobil)})
                           -> (U Num BinaryMobil)]
                          [BinaryMobilBranch -> (U Num BinaryMobil')]))
(defn branch-structure
  "Q. 2.29-a"
  [b]
  (:structure b))

(typed/tc-ignore

(defn total-weight
  "Q. 2-29-b"
  [m]
  (if (coll? m)
    (+' (total-weight (branch-structure (left-branch m)))
        (total-weight (branch-structure (right-branch m))))
    m))

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
  (if (coll? m)
    (letfn [(branch-moment [b]
              (*' (branch-length b)
                  (total-weight (branch-structure b))))]
      (let [lb (left-branch m)
            ls (branch-structure lb)
            rb (right-branch m)
            rs (branch-structure rb)]
        (and (is-balanced ls)
             (is-balanced rs)
             (= (branch-moment lb)
                (branch-moment rb)))))
    true))

(defn square-tree
  "Q. 2.30-1"
  {:test #(do (is (= (square-tree [1 [2 [3]] 4]) [1 [4 [9]] 16])))}
  [t]
  (cond
   (not (coll? t)) (square t)
   (empty? t) []
   :else (cons (square-tree (first t))
               (square-tree (rest t)))))

(defn square-tree'
  "Q. 2.30-2"
  {:test #(do (is (= (square-tree' [1 [2 [3]] 4]) [1 [4 [9]] 16])))}
  [t]
  (map (fn [e] (if (coll? e)
                 (square-tree' e)
                 (square e)))
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

(ann subsets (All [a] [(Coll a) -> (Coll (Coll a))]))
(defn subsets
  "Q. 2.32"
  {:test #(do (is (= (set (subsets [1 2 3]))
                     #{[] [1] [2] [3] [1 2] [2 3] [1 3] [1 2 3]})))}
  [s]
  (if (empty? s)
    [[]]
    (let [more (subsets (rest s))]
      (append more (map_ (fn> [coll :- (Coll a)] ; XXX: anaphoric?
                              (cons (first s) coll))
                         more)))))
; (clojure.core.typed/check-ns 'sicp.core)(clojure.test/run-tests 'sicp.core)
(ann -main [String * -> nil])
(defn -main [& args]
  (clojure.test/run-tests)
  (println "ok"))
