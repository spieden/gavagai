(ns gavagai.core-test
  (:use [clojure.test])
  (:require [gavagai.core :as g]))

(g/register-converters {:exclude [:class]}
 ["java.util.Date" :add {:string str}]
 ["java.awt.Color" :only [:green #"r.d" :blue] :add {:string str} :lazy? false])

(g/register-converters {:exclude [:class]}
 ["java.awt.Button" :translate-arrays? true :lazy? false]
 ["java.awt.Button$AccessibleAWTButton" :exclude [#"loc.le"] :lazy? false])

(deftest basic-tests
  (testing "basic translations tests"
    (let [dt (java.util.Date.)]
      (g/with-translator-ns gavagai.core-test
        (let [tdt (g/translate dt)]
          (is (instance? lazymap.core.LazyPersistentMap tdt))
          (is (= (.getMinutes dt) (:minutes tdt)))
          (is (nil? (:class tdt)) )
          (is (= (.toString dt) (:string tdt)))))))
  (testing "testing non lazy translation and only"
    (let [c (java.awt.Color. 10 10 10)]
      (g/with-translator-ns gavagai.core-test
        (let [tc (g/translate c)]
          (is (instance? clojure.lang.PersistentArrayMap tc))
          (is (= #{:green :red :blue :string} (into #{} (keys tc))))))))
  (testing "testing recursivity, max-depth and array translations"
    (let [b (java.awt.Button. "test")]
      (g/with-translator-ns gavagai.core-test
        (let [tb (g/translate b {:max-depth 3})]
          (is (nil? (:locale tb)))
          (is (instance? java.awt.Button$AccessibleAWTButton
                         (get-in tb [:accessible-context :accessible-action :accessible-action])))
          (is (vector? (:action-listeners tb))))))))